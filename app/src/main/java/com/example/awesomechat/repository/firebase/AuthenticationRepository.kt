package com.example.awesomechat.repository.firebase

import android.util.Log
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
    val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    override var emailCurrent: String
) : InteractAuthentication {

    override fun checkUserCurrent(): Boolean {
        return auth.currentUser != null
    }

    override fun registerAccount(
        name: String,
        email: String,
        password: String,
        callBack: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                emailCurrent=email
                callBack(true)
                addUser(name, email)
            } else
                callBack(false)
        }
    }

    override fun login(email: String, password: String, callBack: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                emailCurrent=email
                fetchTokenAndUpdate()
                callBack(true)
            } else {
                callBack(false)
            }
        }
    }

    override fun addUser(name: String, email: String) {
        Log.w("Test",emailCurrent)
        CoroutineScope(Dispatchers.IO).launch {
            db.collection(InfoFieldQuery.COLLECTION_USER)
                .add(
                    User(
                        name = name,
                        email = email,
                        url = InfoFieldQuery.URL_DEFAULT
                    )
                ).await()
            fetchTokenAndUpdate()
        }
    }

    override suspend fun signOut() {
        withContext(Dispatchers.IO) {
            updateToken("")
            auth.signOut()
        }
    }

    override fun fetchTokenAndUpdate() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { token ->
                    CoroutineScope(Dispatchers.IO).launch {
                        updateToken(token)
                    }
                }
            } else {
                Log.w("FirebaseMessaging", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    private suspend fun fetchIdUser(): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (emailCurrent == "null") {
                    return@withContext null
                }
                val idUser =
                    db.collection(InfoFieldQuery.COLLECTION_USER)
                        .whereEqualTo(InfoFieldQuery.KEY_EMAIL, emailCurrent).get()
                        .await().documents.firstNotNullOfOrNull { it.id }
                idUser
            } catch (exception: Exception) {
                null
            }
        }
    }

    override suspend fun updateToken(token: String) {
        return withContext(Dispatchers.IO) {
            val idUser = fetchIdUser()
            if (idUser != null) {
                db.collection(InfoFieldQuery.COLLECTION_USER).document(idUser)
                    .update(InfoFieldQuery.KEY_TOKEN, token).await()
            }
        }
    }
}






