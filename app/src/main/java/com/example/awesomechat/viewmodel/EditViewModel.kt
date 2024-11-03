package com.example.awesomechat.viewmodel

import android.app.Application

import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.model.User
import com.example.awesomechat.util.DataStoreManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditViewModel(private val interact: InteractUser,val application: Application) : ViewModel() {
    val email = MutableLiveData<String>()
    val numberPhone = MutableLiveData<String>()
    val birthDay = MutableLiveData<String>()
    val url = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    init {
        viewModelScope.launch {
           async {   val user= DataStoreManager.getSavedInformationUser(application)
            name.postValue(user.name)
            url.postValue(user.url)
            birthDay.postValue(user.birthday)
            numberPhone.postValue(user.numberphone)
            email.postValue(user.email)}.await()
        }
    }
    fun updateRecordUser() {
        viewModelScope.launch{
            async {  interact.updaterRecordUser(url.value.toString().toUri(), email.value.toString(), name.value.toString(), numberPhone.value.toString(), birthDay.value.toString())}.await()
            val user = User(url.value.toString(), email.value.toString(), name.value.toString(), numberPhone.value.toString(), birthDay.value.toString())
            url.postValue(user.url)
            name.postValue(user.name)
            DataStoreManager.saveInformationUser(user,application) }
    }

}