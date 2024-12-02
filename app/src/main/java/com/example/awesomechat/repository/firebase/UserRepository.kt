package com.example.awesomechat.repository.firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(
    val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: StorageReference
) : InteractUser {
    override val emailCurrent: String = auth.currentUser?.email.toString()
    override fun updaterRecordUser(user: User, callBack: (Boolean) -> Unit) {
        // Tìm document của user bằng email
        db.collection(InfoFieldQuery.COLLECTION_USER)
            .whereEqualTo(InfoFieldQuery.KEY_EMAIL, user.email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    val documentSnapshot = task.result.documents[0]
                    val documentId = documentSnapshot.id
                    val updatedData = mapOf(
                        "birthday" to user.birthday,
                        "email" to user.email,
                        "name" to user.name,
                        "numberphone" to user.numberphone
                    )
                    db.collection(InfoFieldQuery.COLLECTION_USER).document(documentId)
                        .update(updatedData)
                        .addOnSuccessListener {
                            user.url.let { uri ->
                                storage.child(user.email).putFile(uri.toUri())
                                    .addOnSuccessListener { uploadTask ->
                                        uploadTask.metadata?.reference?.downloadUrl
                                            ?.addOnSuccessListener { url ->
                                                db.collection(InfoFieldQuery.COLLECTION_USER)
                                                    .document(documentId)
                                                    .update(InfoFieldQuery.KEY_URL, url.toString())
                                                    .addOnSuccessListener {
                                                        callBack(true)
                                                    }
                                                    .addOnFailureListener {
                                                        callBack(false)
                                                    }
                                            }
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener {
                callBack(false)
            }
    }

    override suspend fun getRecordUser(email: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val document = db.collection(InfoFieldQuery.COLLECTION_USER)
                    .whereEqualTo(InfoFieldQuery.KEY_EMAIL, email).get().await()
                if (document.isEmpty) null
                else document.first().toObject(User::class.java)
            } catch (exception: Exception) {
                null
            }
        }
    }


    override fun signOut() {
        auth.signOut()
    }
}
