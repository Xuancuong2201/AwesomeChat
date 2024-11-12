package com.example.awesomechat.repository.firebase

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.Conversation
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.model.Messages
import com.example.awesomechat.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject


class MessageRepository @Inject constructor(
    auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: StorageReference
) : InteractMessage {
    override val emailCurrent: String = auth.currentUser!!.email.toString()

    override suspend fun getListMessage(): Flow<List<Messages>> = callbackFlow {
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipientList = mutableListOf<String>()
        val messageList = mutableListOf<Messages>()
        val listenerRegistration = db.collection(InfoFieldQuery.COLLECTION_CONVERSATION)
            .whereArrayContains(InfoFieldQuery.KEY_USER, idUser)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val idMessages = snapshot?.mapNotNull { it.id }

                if (idMessages.isNullOrEmpty()) {
                    trySend(emptyList()).isSuccess
                }
                val arrayUserList = snapshot?.mapNotNull { id -> id.get("user") }
                if (arrayUserList.isNullOrEmpty()) {
                   trySend(emptyList())
                }
                else{
                    val filteredUsers = arrayUserList.filterIsInstance<List<String>>()
                    for (list in filteredUsers) {
                        idRecipientList.add(list.firstOrNull { it != idUser }!!)
                    }
                    idMessages?.forEachIndexed { index, idMess ->
                        db.collection(InfoFieldQuery.COLLECTION_MESSAGES).document(idMess)
                            .collection(InfoFieldQuery.KEY_MESSAGE)
                            .addSnapshotListener { snapshotMess, _ ->
                                if (snapshotMess != null && !snapshotMess.isEmpty) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val latestMessage =
                                                db.collection(InfoFieldQuery.COLLECTION_MESSAGES)
                                                    .document(idMess)
                                                    .collection(InfoFieldQuery.KEY_MESSAGE)
                                                    .orderBy(
                                                        InfoFieldQuery.KEY_TIME,
                                                        Query.Direction.DESCENDING
                                                    )
                                                    .limit(1).get().await()
                                                    .firstOrNull()
                                                    ?.toObject(DetailMessage::class.java)

                                            val quantity =
                                                db.collection(InfoFieldQuery.COLLECTION_MESSAGES)
                                                    .document(idMess)
                                                    .collection(InfoFieldQuery.KEY_MESSAGE)
                                                    .whereEqualTo(InfoFieldQuery.KEY_STATUS, false)
                                                    .whereEqualTo(
                                                        InfoFieldQuery.KEY_SENT_BY,
                                                        idRecipientList[index]
                                                    )
                                                    .get().await().size()

                                            val user =
                                                db.collection(InfoFieldQuery.COLLECTION_USER)
                                                    .document(idRecipientList[index])
                                                    .get().await()
                                                    .toObject(User::class.java)

                                            if (latestMessage != null && user != null) {
                                                val messages = Messages(
                                                    url = user.url,
                                                    email = user.email,
                                                    name = user.name,
                                                    currentMessage = latestMessage.content,
                                                    time = latestMessage.time,
                                                    quantity = quantity,
                                                    status = latestMessage.status,
                                                    type = latestMessage.type
                                                )
                                                withContext(Dispatchers.Main) {
                                                    updateOrAddMessage(messageList, messages)
                                                    trySend(messageList).isSuccess
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.w("Error firebase", "Error fetching messages", e)
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        awaitClose { listenerRegistration.remove() }
    }
    private fun updateOrAddMessage(messageList: MutableList<Messages>,messages: Messages){
        val existingMessage = messageList.find { it.email == messages.email }
        if (existingMessage != null) {
            val index = messageList.indexOf(existingMessage)
            messageList[index] = messages
        } else {
            messageList.add(messages)
        }
    }
    override suspend fun sentMessage(recipient: String, content: String) {
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient) ?: return
        val message = DetailMessage(content, idUser, false, Timestamp.now().toDate(), InfoFieldQuery.TYPE_MESS)
        db.collection(InfoFieldQuery.COLLECTION_MESSAGES).document(getIdMess(idUser, idRecipient)).collection(InfoFieldQuery.KEY_MESSAGE)
            .add(message).await()
    }

    override suspend fun sentMultiImage(recipient: String, listImage: List<String>) {
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient) ?: return
        val list: MutableList<String> = mutableListOf()
        for (image in listImage) {
            val newUri = storage.child(UUID.randomUUID().toString()).putFile(image.toUri())
                .await().metadata?.reference?.downloadUrl?.await()
            list.add(newUri.toString())
        }
        val message =
            DetailMessage("", idUser, false, Timestamp.now().toDate(), InfoFieldQuery.TYPE_MULTI, list)
        db.collection(InfoFieldQuery.COLLECTION_MESSAGES).document(getIdMess(idUser, idRecipient)).collection(InfoFieldQuery.KEY_MESSAGE)
            .add(message).await()
    }

    override suspend fun createConversation(idUser: String, idRecipient: String): String {
        val id = UUID.randomUUID().toString()
        val conversation = Conversation(id, InfoFieldQuery.TYPE_DOUBLE, arrayListOf(idUser, idRecipient))
        db.collection(InfoFieldQuery.COLLECTION_CONVERSATION).document(id).set(conversation).await()
        return id
    }

    override suspend fun sentImage(recipient: String, uri: Uri) {
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient) ?: return
        val newUri = uri.let {
            storage.child(UUID.randomUUID().toString()).putFile(it)
                .await().metadata?.reference?.downloadUrl?.await()?.toString() ?: ""
        }
        val message = DetailMessage(newUri, idUser, false, Timestamp.now().toDate(), InfoFieldQuery.TYPE_IMAGE)
        db.collection(InfoFieldQuery.COLLECTION_MESSAGES).document(getIdMess(idUser, idRecipient)).collection(InfoFieldQuery.KEY_MESSAGE)
            .add(message).await()
    }

    override suspend fun changeStatusMessage(recipient: String) {
        try {
            val idUser = fetchIdUser(emailCurrent)!!
            val idRecipient = fetchIdUser(recipient)!!
            val idMess = getIdMess(idUser, idRecipient)
            val messageQuerySnapshot =
                db.collection(InfoFieldQuery.COLLECTION_MESSAGES).document(idMess).collection(InfoFieldQuery.KEY_MESSAGE)
            messageQuerySnapshot.whereEqualTo(InfoFieldQuery.KEY_SENT_BY, idRecipient)
                .whereEqualTo(InfoFieldQuery.KEY_STATUS, false).get().addOnSuccessListener {
                    it.documents.mapNotNull { document ->
                        db.collection(InfoFieldQuery.COLLECTION_MESSAGES).document(idMess).collection(InfoFieldQuery.KEY_MESSAGE)
                            .document(document.id).update(InfoFieldQuery.KEY_STATUS, true)
                    }
                }.await()
        } catch (exception: Exception) {
            Log.w("firestore", "Error getting documents: ", exception)
        }
    }

    override suspend fun fetchIdUser(email: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val idUser =
                    db.collection(InfoFieldQuery.COLLECTION_USER).whereEqualTo(InfoFieldQuery.KEY_EMAIL, email).get()
                        .await().documents.firstNotNullOfOrNull { it.id }
                idUser
            } catch (exception: Exception) {
                null
            }
        }
    }

    override suspend fun listenerChangeDetailsMessage(recipient: String): Flow<List<DetailMessage>> =
        callbackFlow {
            val idUser = fetchIdUser(emailCurrent)!!
            val idRecipient = fetchIdUser(recipient) ?: return@callbackFlow
            val idMess = checkIdMess(idRecipient, idUser)
            if (idMess != null) {
                val docRef = db.collection(InfoFieldQuery.COLLECTION_MESSAGES).document(idMess).collection(InfoFieldQuery.KEY_MESSAGE)
                val listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    val messagesList =
                        snapshot?.toObjects(DetailMessage::class.java) ?: emptyList()
                    messagesList.map { it.status=true }
                    trySend(messagesList)
                }
                awaitClose { listenerRegistration.remove() }
            }
            close()
        }

    private suspend fun getIdMess(idUser: String, idRecipient: String): String {
        val idMessQuerySnapshot =
            db.collection(InfoFieldQuery.COLLECTION_CONVERSATION).whereArrayContains(InfoFieldQuery.KEY_USER, idUser)
                .whereEqualTo(InfoFieldQuery.KEY_TYPE, InfoFieldQuery.TYPE_DOUBLE).get().await()
        val idMess = idMessQuerySnapshot.documents.filter { document ->
            val users = document.get(InfoFieldQuery.KEY_USER) as? List<String> ?: emptyList()
            listOf(idUser, idRecipient).all { it in users }
        }.map { it.id }
        return if (idMess.isEmpty())
            createConversation(idUser, idRecipient)
        else
            idMess[0]
    }

    private suspend fun checkIdMess(idUser: String, idRecipient: String): String? {
        val idMessQuerySnapshot =
            db.collection(InfoFieldQuery.COLLECTION_CONVERSATION).whereArrayContains(InfoFieldQuery.KEY_USER, idUser)
                .whereEqualTo(InfoFieldQuery.KEY_TYPE, InfoFieldQuery.TYPE_DOUBLE).get().await()
        val idMess = idMessQuerySnapshot.documents.filter { document ->
        val users = document.get(InfoFieldQuery.KEY_USER) as? List<String> ?: emptyList()
            listOf(idUser, idRecipient).all { it in users }
        }.map { it.id }
        return if (idMess.isNotEmpty()) idMess[0] else null
    }
}

