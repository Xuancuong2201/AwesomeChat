package com.example.awesomechat.repository


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthenticationRepository(private val context: Context) {
    private val userCurrent: MutableLiveData<FirebaseUser> = MutableLiveData<FirebaseUser>()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    init {
        checkCurrentUser()
    }
    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            userCurrent.postValue(auth.currentUser)
        }
    }
    public fun getUserCurrent(): MutableLiveData<FirebaseUser> {
        return userCurrent
    }
    public fun registerAccount(name:String,email:String,password:String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
            if (it.isSuccessful) {
                userCurrent.postValue(auth.currentUser)
            }
            else
                Toast.makeText(context.applicationContext, it.exception?.message,Toast.LENGTH_SHORT).show()
        }
    }
    public fun login(email:String,password:String){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
            if(it.isSuccessful){
                userCurrent.postValue(auth.currentUser)
            }
            else
                Toast.makeText(context.applicationContext, it.exception?.message,Toast.LENGTH_SHORT).show()
        }
    }
    public fun signOut(){
        auth.signOut()
    }
}