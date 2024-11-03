package com.example.awesomechat.viewmodel

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    init {
        viewModelScope.launch {
            async {
                val user = DataStoreManager.getSavedInformationUser(context)
                numberPhone.postValue(user.numberphone)
                email.postValue(user.email)
                name.postValue(user.name)
                birthDay.postValue(user.birthday)
                url.postValue(user.url)
            }.await()
        }
    }
    fun updateRecordUser() {
        viewModelScope.launch {
            async {
                interact.updaterRecordUser(url.value.toString().toUri(), email.value.toString(), name.value.toString(), numberPhone.value.toString(), birthDay.value.toString())
            }.await()
            val user = User(url.value.toString(), email.value.toString(), name.value.toString(), numberPhone.value.toString(), birthDay.value.toString())
            url.postValue(user.url)
            name.postValue(user.name)
            DataStoreManager.saveInformationUser(user, context)
        }
    }

}