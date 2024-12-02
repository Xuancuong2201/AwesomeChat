package com.example.awesomechat.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class LoginViewmodel @Inject constructor(
    private val interactAu: InteractAuthentication,
    private val interactUser: InteractUser,
    @ApplicationContext val context: Context
) : ViewModel() {
    val email by lazy { MutableLiveData<String>() }
    val password by lazy { MutableLiveData<String>() }
    val result by lazy { MutableLiveData(false) }
    val stateLogin: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    fun login() {
        interactAu.login(email.value.toString(), password.value.toString()) {
            if (it) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val user = interactUser.getRecordUser(email.value.toString())
                        DataStoreManager.saveInformationUser(user!!, context)
                        stateLogin.postValue(true)
                    }
                }
            } else
                stateLogin.postValue(false)
        }
    }

    fun checkEnable() {
        result.value = !(email.value.isNullOrEmpty() || password.value.isNullOrEmpty())
    }
}