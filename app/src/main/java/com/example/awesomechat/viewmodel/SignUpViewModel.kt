package com.example.awesomechat.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.awesomechat.repository.AuthenticationRepository
import com.google.firebase.auth.FirebaseUser
import java.util.regex.Pattern


class SignUpViewModel(val repository: AuthenticationRepository ) : ViewModel() {
    val account :MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val email :MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val password :MutableLiveData<String> by lazy { MutableLiveData<String>() }

    public fun register(){
        repository.registerAccount(account.value.toString(),email.value.toString(),password.value.toString())
        Log.e("Test",account.value.toString())
        Log.e("Test",email.value.toString())
        Log.e("Test",password.value.toString())
    }
    public fun signOut(){
        repository.signOut()
    }
    fun containsNumber(input:String) : Boolean{
        val regex = Regex("[0-9]")
        return regex.containsMatchIn(input)
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        return emailPattern.matcher(email).matches()
    }

    fun isValidPassword(password:String): Boolean {
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }

        return hasLowerCase && hasUpperCase && hasDigit
    }
}