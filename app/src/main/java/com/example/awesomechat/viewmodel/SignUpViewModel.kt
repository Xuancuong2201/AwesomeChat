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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val interact: InteractAuthentication,
    val interactUser: InteractUser,
    @ApplicationContext val context: Context
) : ViewModel() {
    val account by lazy { MutableLiveData<String>() }
    val email by lazy { MutableLiveData<String>() }
    val password by lazy { MutableLiveData<String>() }
    val isChecked: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }
    val stateRegister: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val result = MutableLiveData(false)
    fun register() {
        interact.registerAccount(
            account.value.toString(),
            email.value.toString(),
            password.value.toString()
        ) {
            if (it) {
                viewModelScope.launch {
                    val user = async { interactUser.getRecordUser(email.value.toString()) }.await()
                    async { DataStoreManager.saveInformationUser(user!!, context) }.await()
                    async { stateRegister.postValue(true) }.await()
                }
            } else
                stateRegister.postValue(false)
        }
    }

    fun checkEnable() {
        result.value =
            !(account.value.isNullOrEmpty() || email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || isChecked.value == false)
    }

}