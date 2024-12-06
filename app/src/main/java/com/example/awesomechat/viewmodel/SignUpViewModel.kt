package com.example.awesomechat.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.model.User
import com.example.awesomechat.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val interact: InteractAuthentication,
    @ApplicationContext val context: Context
) : ViewModel() {
    val account by lazy { MutableLiveData<String>() }
    val email by lazy { MutableLiveData<String>() }
    val password by lazy { MutableLiveData<String>() }
    val isChecked by lazy { MutableLiveData(false) }
    val stateRegister by lazy { MutableLiveData<Boolean>() }
    val result: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        listOf(email, password, account, isChecked).forEach { source ->
            addSource(source) { value = checkEnable() }
        }
    }
    fun register() {
        interact.registerAccount(
            account.value.toString(),
            email.value.toString(),
            password.value.toString()
        ) {
            if (it) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val user = User(
                            url = InfoFieldQuery.URL_DEFAULT,
                            name = account.value.toString(),
                            email = email.value.toString()
                        )
                        DataStoreManager.saveInformationUser(user, context)
                        stateRegister.postValue(true)
                    }
                }
            } else
                stateRegister.postValue(false)
        }
    }

    private fun checkEnable(): Boolean {
        return !(account.value.isNullOrEmpty() || email.value.isNullOrEmpty() || password.value.isNullOrEmpty() || isChecked.value == false)
    }

}