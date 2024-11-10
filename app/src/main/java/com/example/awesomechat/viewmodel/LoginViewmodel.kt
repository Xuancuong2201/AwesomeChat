package com.example.awesomechat.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class LoginViewmodel @Inject constructor( val interactAu: InteractAuthentication,val interactUser: InteractUser,@ApplicationContext val context: Context) : ViewModel() {
    val email by lazy { MutableLiveData<String>() }
    val password by lazy { MutableLiveData<String>() }
    val result by lazy { MutableLiveData(false) }
    val stateLogin: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    fun login() {
        interactAu.login(email.value.toString(), password.value.toString()) {
            if (it) {
                viewModelScope.launch {
                    val user = async { interactUser.getRecordUser(email.value.toString()) }.await()
                    async { DataStoreManager.saveInformationUser(user!!, context) }.await()
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