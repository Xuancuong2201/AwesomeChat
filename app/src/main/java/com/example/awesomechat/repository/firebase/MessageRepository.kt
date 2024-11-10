package com.example.awesomechat.repository.firebase

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
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
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
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
        val listenerRegistration =db.collection("Conversation").whereArrayContains("user", idUser)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val idMessages = snapshot?.mapNotNull { it.id }

                if (idMessages.isNullOrEmpty()){
                    trySend(emptyList()).isSuccess
                }
                val arrayUserList = snapshot?.mapNotNull { id -> id.get("user") as List<String> }
                for (list in arrayUserList!!) {
                    idRecipientList.add(list.firstOrNull { it != idUser }!!)
                }
                idMessages?.forEachIndexed { index, idMess ->
                    db.collection("Messages").document(idMess).collection("Message")
                        .addSnapshotListener { snapshotMess, eMess ->
                            if (eMess != null) {
                                return@addSnapshotListener
                            }
                            if (snapshotMess != null && !snapshotMess.isEmpty) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val latestMessage =
                                            db.collection("Messages").document(idMess)
                                                .collection("Message")
                                                .orderBy("time", Query.Direction.DESCENDING)
                                                .limit(1).get().await()
                                                .firstOrNull()?.toObject(DetailMessage::class.java)

                                        val quantity = db.collection("Messages").document(idMess)
                                            .collection("Message")
                                            .whereEqualTo("status", false)
                                            .whereEqualTo("sentby", idRecipientList[index])
                                            .get().await().size()

                                        val user =
                                            db.collection("User").document(idRecipientList[index])
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
                                            Log.w("Test", messages.currentMessage.toString() +"quantity :"+ messages.quantity.toString() + " status:" + messages.status.toString())
                                            withContext(Dispatchers.Main) {
                                                updateOrAddMessage(messageList,messages)
                                                trySend(messageList).isSuccess
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.w("Firestore", "Error fetching messages", e)
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
        val message = DetailMessage(content, idUser, false, Timestamp.now().toDate(), "mess")
        db.collection("Messages").document(getIdMess(idUser, idRecipient)).collection("Message")
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
            DetailMessage("", idUser, false, Timestamp.now().toDate(), "multi image", list)
        db.collection("Messages").document(getIdMess(idUser, idRecipient)).collection("Message")
            .add(message).await()
    }

    override suspend fun createConversation(idUser: String, idRecipient: String): String {
        val id = UUID.randomUUID().toString()
        val conversation = Conversation(id, "double", arrayListOf(idUser, idRecipient))
        db.collection("Conversation").document(id).set(conversation).await()
        return id
    }

    override suspend fun sentImage(recipient: String, uri: Uri) {
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient) ?: return
        val newUri = uri.let {
            storage.child(UUID.randomUUID().toString()).putFile(it)
                .await().metadata?.reference?.downloadUrl?.await()?.toString() ?: ""
        }
        val message = DetailMessage(newUri, idUser, false, Timestamp.now().toDate(), "image")
        db.collection("Messages").document(getIdMess(idUser, idRecipient)).collection("Message")
            .add(message).await()
    }

    override suspend fun changeStatusMessage(recipient: String) {
        try {
            val idUser = fetchIdUser(emailCurrent)!!
            val idRecipient = fetchIdUser(recipient)!!
            val idMess = getIdMess(idUser, idRecipient)
            val messageQuerySnapshot =
                db.collection("Messages").document(idMess).collection("Message")
            messageQuerySnapshot.whereEqualTo("sentby", idRecipient)
                .whereEqualTo("status", false).get().addOnSuccessListener {
                    it.documents.mapNotNull { document ->
                        db.collection("Messages").document(idMess).collection("Message")
                            .document(document.id).update("status", true)
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
                    db.collection("User").whereEqualTo("email", email).get()
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
                val docRef = db.collection("Messages").document(idMess).collection("Message")
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
            db.collection("Conversation").whereArrayContains("user", idUser)
                .whereEqualTo("type", "double").get().await()
        val idMess = idMessQuerySnapshot.documents.filter { document ->
            val users = document.get("user") as? List<String> ?: emptyList()
            listOf(idUser, idRecipient).all { it in users }
        }.map { it.id }
        return if (idMess.isEmpty())
            createConversation(idUser, idRecipient)
        else
            idMess[0]
    }

    private suspend fun checkIdMess(idUser: String, idRecipient: String): String? {
        val idMessQuerySnapshot =
            db.collection("Conversation").whereArrayContains("user", idUser)
                .whereEqualTo("type", "double").get().await()
        val idMess = idMessQuerySnapshot.documents.filter { document ->
            val users = document.get("user") as? List<String> ?: emptyList()
            listOf(idUser, idRecipient).all { it in users }
        }.map { it.id }
        return if (idMess.isNotEmpty()) idMess[0] else null
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MessageModule {
    @Binds
    abstract fun bindInteractMessage(messageRepository: MessageRepository): InteractMessage
}