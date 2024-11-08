package com.example.awesomechat.repository.firebase

import androidx.lifecycle.MutableLiveData
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
    val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val userCurrent: MutableLiveData<FirebaseUser>
) : InteractAuthentication {
    override val emailCurrent: String = auth.currentUser?.email.toString()
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
        val user = User(name, email)
        db.collection("User")
            .add(user)
    }
}
@Module
@InstallIn(ViewModelComponent::class)
abstract class AuthenticationModule {
    @Binds
    abstract fun bindInteractAuthentication(authenticationRepository: AuthenticationRepository): InteractAuthentication
}




