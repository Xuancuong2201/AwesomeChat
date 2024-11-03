package com.example.awesomechat.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.Messages
import com.example.awesomechat.model.User
import kotlinx.coroutines.launch

class ChatViewModel(private val interactMessage: InteractMessage) : ViewModel() {
    val messageList: MutableLiveData<List<Messages>> by lazy {   MutableLiveData<List<Messages>>() }
    init {
        viewModelScope.launch {
            val message = interactMessage.getListMessage()
            messageList.postValue(message)
        }
    }
    fun changeStatus(email:String){
        viewModelScope.launch {
            interactMessage.changeStatusMessage(email)
        }
    }
}