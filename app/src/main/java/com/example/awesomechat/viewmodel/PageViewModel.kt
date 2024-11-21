package com.example.awesomechat.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PageViewModel @Inject constructor(
    private val interact: InteractAuthentication,
    @ApplicationContext val context: Context
) : ViewModel() {
    val currentUserEmail by lazy { MutableLiveData<String>(interact.emailCurrent) }
    val name by lazy { MutableLiveData<String>() }
    val url by lazy { MutableLiveData<String>() }

    init {
        viewModelScope.launch {
            val result = async { DataStoreManager.getSavedInformationUser(context) }.await()
            name.postValue(result.name)
            url.postValue(result.url)
        }
    }

    fun getUrlAndName() {
        viewModelScope.launch {
            val result = async { DataStoreManager.getSavedInformationUser(context) }.await()
            url.postValue(result.url)
            name.postValue(result.name)
            currentUserEmail.postValue(result.email)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            interact.signOut()
        }
    }
}