package com.example.awesomechat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.awesomechat.interact.InteractAuthentication
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(private val interact: InteractAuthentication) : ViewModel() {
    val account by lazy { MutableLiveData<String>() }
    val email by lazy { MutableLiveData<String>() }
    val password by lazy { MutableLiveData<String>() }
    val isChecked: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }
    val stateRegister: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val result = MutableLiveData(false)
    fun register() {
        interact.registerAccount(account.value.toString(), email.value.toString(), password.value.toString()) {
            stateRegister.value = it
        }
    }
    fun checkEnable() {
        result.value = !(account.value.isNullOrEmpty() || email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || isChecked.value == false)
    }

}