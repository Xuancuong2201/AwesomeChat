package com.example.awesomechat.repository.firebase

import android.net.Uri
import android.util.Log
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository : InteractUser {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance().getReference("Images")
    override val emailCurrent: String = auth.currentUser?.email.toString()

    override fun updaterRecordUser(imageUri: Uri?, email: String, name: String, numberPhone: String, birthDay: String)
    {
        db.collection("User").whereEqualTo("email", email).get()
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty) {
                    val documentSnapshot = it.result.documents[0]
                    val documentId = documentSnapshot.id
                    db.collection("User")
                        .document(documentId)
                        .set(
                            mapOf(
                                "url" to imageUri,
                                "birthday" to birthDay,
                                "email" to email,
                                "name" to name,
                                "numberphone" to numberPhone
                            )
                        )
                    imageUri?.let { uri ->
                        storage.child(email).putFile(uri)
                            .addOnSuccessListener { task ->
                                task.metadata!!.reference!!.downloadUrl
                                    .addOnSuccessListener { url ->
                                        db.collection("User").document(documentId)
                                            .update("url", url.toString())
                                    }
                            }
                    }
                }
            }
    }

    override suspend fun getRecordUser(email: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val document = db.collection("User").whereEqualTo("email", email).get().await()
                if (document.isEmpty) null
                else document.first().toObject(User::class.java)
            } catch (exception: Exception) {
                Log.w("Test", "Error getting documents: ", exception)
                null
            }
        }
    }

    override suspend fun getUrlUser(email: String): String {
        val document = db.collection("User").whereEqualTo("email", email).get().await()
        val user:User = document.first().toObject(User::class.java)
        return user.url
    }

    override fun signOut() {
        auth.signOut()
    }
}