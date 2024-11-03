package com.example.awesomechat.viewmodel

import android.app.Application

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.util.DataStoreManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class LoginViewmodel(private val interactAu: InteractAuthentication,private val interactUser: InteractUser,private val application: Application) : ViewModel() {
    val email by lazy { MutableLiveData<String>() }
    val password by lazy { MutableLiveData<String>() }
    val result by lazy { MutableLiveData(false) }
    val stateLogin: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    init {
        if(interactAu.checkUserCurrent()){
            stateLogin.postValue(true)
        }
    }
    fun login() {
        interactAu.login(email.value.toString(), password.value.toString()) {
            if (it) {
                viewModelScope.launch {
                    val user = async { interactUser.getRecordUser(email.value.toString()) }.await()
                    async { DataStoreManager.saveInformationUser(user!!, application) }.await()
                    async { stateLogin.postValue(true) }.await()
                }
            } else
                stateLogin.postValue(false)
        }
    }
    fun checkEnable() {
        result.value = !( email.value.isNullOrEmpty() || password.value.isNullOrEmpty())
    }

}