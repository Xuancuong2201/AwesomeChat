package com.example.awesomechat.repository.firebase

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.Conversation
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.model.Messages
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject


class MessageRepository @Inject constructor(private val auth: FirebaseAuth, private val db: FirebaseFirestore,private val storage :StorageReference) : InteractMessage {
    override val emailCurrent: String = auth.currentUser!!.email.toString()
    override suspend fun getListMessage(): List<Messages> {
        return withContext(Dispatchers.IO) {
            val messagesList = mutableListOf<Messages>()
            try {
                val idUser = fetchIdUser(emailCurrent)!!
                val idMessQuerySnapshot = db.collection("Conversation")
                    .whereArrayContains("user", idUser)
                    .get().await()
                val documentJobs = idMessQuerySnapshot.documents.map { document ->
                    async {
                        val message = Messages()
                        val messageId = document.getString("idMessages") ?: return@async null

                        val latestMessage = db.collection("Messages")
                            .document(messageId)
                            .collection("Message")
                            .orderBy("time", Query.Direction.DESCENDING)
                            .limit(1)
                            .get().await().documents.firstOrNull()

                        latestMessage?.let {
                            message.apply {
                                currentMessage = it.getString("content")
                                time = it.getTimestamp("time")!!.toDate()
                                status = it.getBoolean("status")!!
                            }
                        }

                        (document.get("user") as? List<*>)?.forEach { userId ->
                            if (idUser != userId.toString()) {
                                val userInfo = db.collection("User").document(userId.toString()).get().await()
                                message.apply {
                                    url = userInfo.getString("url")
                                    name = userInfo.getString("name")
                                    email = userInfo.getString("email")
                                }

                                val unreadCount = db.collection("Messages")
                                    .document(messageId)
                                    .collection("Message")
                                    .whereEqualTo("status", false)
                                    .whereEqualTo("sentby", userId.toString())
                                    .get().await()
                                message.quantity = unreadCount.documents.size
                                messagesList.add(message)
                            }
                        }
                    }
                }
                documentJobs.awaitAll()
            } catch (exception: Exception) {
                Log.w("firestore", "Error getting documents: ", exception)
            }
            messagesList
        }
    }

    override suspend fun sentMessage(recipient: String, content: String) {
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient)
        if (idRecipient != null) {
            val idMessQuerySnapshot =
                db.collection("Conversation").whereArrayContains("user", idRecipient)
                    .whereEqualTo("type", "double").get().await()
            val idMess = idMessQuerySnapshot.documents[0].getString("idMessages")
            val message = DetailMessage(content, idUser, false, Timestamp.now().toDate(), "mess")
            db.collection("Messages").document(idMess!!).collection("Message").add(message)
        }
    }

    override suspend fun sentMultiImage(recipient: String, listImage: List<String>){
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient) ?: return
        val list : MutableList<String> = mutableListOf()
        for (image in listImage){
            val newUri =storage.child(UUID.randomUUID().toString()).putFile(image.toUri()).await().metadata?.reference?.downloadUrl?.await()
            list.add(newUri.toString())
        }
        val idMess = db.collection("Conversation")
            .whereArrayContains("user", idRecipient)
            .whereEqualTo("type", "double")
            .get()
            .await()
            .documents.firstOrNull()?.getString("idMessages") ?: return
        val message = DetailMessage("", idUser, false, Timestamp.now().toDate(), "multi image",list)
        db.collection("Messages").document(idMess).collection("Message").add(message).await()
    }

    override suspend fun createConversation(recipient: String) {
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient)!!
        val id= UUID.randomUUID().toString()
        val conversation = Conversation(id,"double", arrayListOf(idUser,idRecipient))
        db.collection("Conversation").document(id).set(conversation).await()
    }

    override suspend fun sentImage(recipient: String, uri:Uri){
        val idUser = fetchIdUser(emailCurrent)!!
        val idRecipient = fetchIdUser(recipient) ?: return
        val newUri = uri.let {
            storage.child(UUID.randomUUID().toString()).putFile(it).await().metadata?.reference?.downloadUrl?.await()?.toString() ?: ""
        }
        val idMess = db.collection("Conversation")
            .whereArrayContains("user", idRecipient)
            .whereEqualTo("type", "double")
            .get()
            .await()
            .documents.firstOrNull()?.getString("idMessages") ?: return
        val message = DetailMessage(newUri, idUser, false, Timestamp.now().toDate(), "image")
        db.collection("Messages").document(idMess).collection("Message").add(message).await()
    }

    override suspend fun getDetailsMessage(recipient: String): List<DetailMessage> {
        return withContext(Dispatchers.IO) {
            val messagesList: MutableList<DetailMessage> = mutableListOf()
            try {
                val idUser = fetchIdUser(emailCurrent)!!
                val idRecipient = fetchIdUser(recipient) ?: return@withContext emptyList()

                val idMess = db.collection("Conversation")
                    .whereArrayContains("user", idRecipient)
                    .whereEqualTo("type", "double")
                    .get()
                    .await()
                    .documents.firstOrNull()?.getString("idMessages") ?: return@withContext emptyList()
                db.collection("Messages").document(idMess).collection("Message")
                    .orderBy("time", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener {
                        for (item in it.documents) {
                            val sender = if (item.getString("sentby") == idUser) "user" else "recipient"
                            val detailMessage = DetailMessage(
                                content = item.getString("content") ?: "",
                                sentby = sender,
                                status = item.getBoolean("status") ?: false,
                                time = item.getTimestamp("time")?.toDate() ?: Date(),
                                type = item.getString("type") ?: "",
                                multiImage = item.get("multiImage") as? MutableList<String> ?: mutableListOf()
                            )
                            messagesList.add(detailMessage)
                        }
                    }
                    .await()
            } catch (exception: Exception) {
                Log.w("firestore", "Error getting documents: ", exception)
            }
            return@withContext messagesList
        }
    }

    override suspend fun changeStatusMessage(emailRecipient: String) {
        try {
            val idRecipient = fetchIdUser(emailRecipient)!!
            val listId = arrayListOf<String>()
            val idMessQuerySnapshot =
                db.collection("Conversation").whereArrayContains("user", idRecipient).get().await()
            if (idMessQuerySnapshot.documents.size > 0) {
                val idMess = idMessQuerySnapshot.documents[0].getString("idMessages")
                val messageQuerySnapshot = db.collection("Messages").document(idMess!!).collection("Message")
                messageQuerySnapshot.whereEqualTo("sentby",idRecipient).whereEqualTo("status",false).get().addOnSuccessListener {
                    it.documents.mapNotNull { document->
                        listId.add(document.id)
                    }
                }.await()
                for (id in listId){
                    messageQuerySnapshot.document(id).update("status",true)
                }
            }
        } catch (exception: Exception) {
            Log.w("firestore", "Error getting documents: ", exception)
        }
    }

     private suspend fun fetchIdUser(email: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val idUserQuerySnapshot =
                    db.collection("User").whereEqualTo("email", email).get().await()
                idUserQuerySnapshot.documents.firstOrNull()?.id
            } catch (exception: Exception) {
                Log.w("firestore", "Error getting documents: ", exception)
                null
            }
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MessageModule{
    @Binds
    abstract fun bindInteractMessage(messageRepository: MessageRepository): InteractMessage
}