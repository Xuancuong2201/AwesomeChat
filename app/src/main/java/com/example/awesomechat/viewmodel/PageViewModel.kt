package com.example.awesomechat.viewmodel

import android.app.Application

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.util.DataStoreManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class PageViewModel(private val interact: InteractAuthentication, private val application: Application) : ViewModel() {
    val currentUserEmail = MutableLiveData(interact.emailCurrent)
    val name = MutableLiveData<String>()
    val url = MutableLiveData<String>()
    init {
        viewModelScope.launch {
            val result=async {  DataStoreManager.getSavedInformationUser(application)}.await()
            name.postValue(result.name)
            url.postValue(result.url)
        }
    }
    fun getUrlAndName(){
        viewModelScope.launch {
            val result = async { DataStoreManager.getSavedInformationUser(application) }.await()
            url.postValue(result.url)
            name.postValue(result.name)
            currentUserEmail.postValue(result.email)
        }

    }
    fun logOut() {
        interact.signOut()
    }
}