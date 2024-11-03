package com.example.awesomechat.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.Messages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(private val interactMessage: InteractMessage) : ViewModel() {
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