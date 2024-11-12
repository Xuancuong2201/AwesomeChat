package com.example.awesomechat.repository.firebase

import androidx.lifecycle.MutableLiveData
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
    val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val userCurrent: MutableLiveData<FirebaseUser>,
    override val emailCurrent: String
) : InteractAuthentication {

    override fun checkUserCurrent(): Boolean {
        if (auth.currentUser != null) {
            userCurrent.postValue(auth.currentUser)
            return true
        } else return false
    }

    override fun registerAccount(
        name: String,
        email: String,
        password: String,
        callBack: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                userCurrent.postValue(auth.currentUser)
                callBack(true)
                addUser(name, email)
            } else
                callBack(false)
        }
    }

    override fun login(email: String, password: String, callBack: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                userCurrent.postValue(auth.currentUser)
                callBack(true)
            } else
                callBack(false)
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun addUser(name: String, email: String) {
        db.collection(InfoFieldQuery.COLLECTION_USER)
            .add(
                User(
                    name = name,
                    email = email,
                    url = InfoFieldQuery.URL_DEFAULT
                )
            )
    }
}






