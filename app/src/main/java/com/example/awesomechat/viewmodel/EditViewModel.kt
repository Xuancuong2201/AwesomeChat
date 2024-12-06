package com.example.awesomechat.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.model.User
import com.example.awesomechat.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
    class EditViewModel @Inject constructor(private val interact: InteractUser, @ApplicationContext val context: Context
) : ViewModel() {
    val email by lazy {MutableLiveData<String>()}
    val name by lazy {MutableLiveData<String>()}
    val url by lazy {MutableLiveData<String>()}
    val birthDay by lazy {MutableLiveData<String>()}
    val numberPhone by lazy {MutableLiveData<String>()}
    val result=  MutableLiveData(false)
    init {
        viewModelScope.launch {
            async {
                val user = DataStoreManager.getSavedInformationUser(context)
                numberPhone.postValue(user.numberphone)
                email.postValue(user.email)
                name.postValue(user.name)
                birthDay.postValue(user.birthday)
                url.postValue(user.url)
                result.postValue(false)
            }.await()
        }
    }
    fun updateRecordUser() {
        viewModelScope.launch {
            val user = User(url.value.toString(), name.value.toString(), email.value.toString(), numberPhone.value.toString(), birthDay.value.toString())
            async {
                interact.updaterRecordUser(user) {
                    result.postValue(it)
                }
            }.await()
            DataStoreManager.saveInformationUser(user, context)
        }
    }

    fun checkEnable() {
        result.value = !( name.value.isNullOrEmpty() || birthDay.value.isNullOrEmpty() || numberPhone.value.isNullOrEmpty()
                || InteractData.containsNumber(name.value.toString()) ||!InteractData.isValidDateFormat(birthDay.value.toString())
                ||InteractData.isNumberPhone(numberPhone.value.toString()))
    }
}
