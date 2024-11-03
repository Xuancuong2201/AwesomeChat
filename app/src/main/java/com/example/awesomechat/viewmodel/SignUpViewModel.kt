package com.example.awesomechat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.awesomechat.interact.InteractAuthentication
class SignUpViewModel(private val interact: InteractAuthentication) : ViewModel() {
    val account = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val result = MutableLiveData(false)
    val isChecked: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }
    val stateRegister: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    fun register() {
        interact.registerAccount(account.value.toString(), email.value.toString(), password.value.toString()) {
            stateRegister.value = it
        }
    }
    fun checkEnable() {
        result.value = !(account.value.isNullOrEmpty() || email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || isChecked.value == false)
    }

}