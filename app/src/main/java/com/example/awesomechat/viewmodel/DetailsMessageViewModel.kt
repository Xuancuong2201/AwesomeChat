package com.example.awesomechat.viewmodel

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.DetailMessage
import kotlinx.coroutines.launch

class DetailsMessageViewModel(private val interactMessage: InteractMessage) : ViewModel() {
    var name = MutableLiveData<String>()
    var imageUrl = MutableLiveData<String>()
    var content = MutableLiveData<String>()
    val listDetailsMessage = MutableLiveData<List<DetailMessage>>()
    val listImageLiveData = MutableLiveData<List<String>>()
    val stateButton: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    fun getDetailsMessage(recipient: String) {
        viewModelScope.launch {
            listDetailsMessage.value = interactMessage.getDetailsMessage(recipient)
        }
    }

    fun sentMessage(recipient: String) {
        viewModelScope.launch {
            interactMessage.sentMessage(recipient, content.value.toString())
        }
    }

    suspend fun sentImage(recipient: String) {
        Log.w("Test Detais ViewModel",listImageLiveData.value!!.size.toString())
        if(listImageLiveData.value!=null){
            if(listImageLiveData.value!!.size==1){
                viewModelScope.launch {
                    interactMessage.sentImage(recipient, listImageLiveData.value!!.firstOrNull()!!.toUri())
                }.join()
            }
            else{
                viewModelScope.launch {
                    interactMessage.sentMultiImage(recipient, listImageLiveData.value!!)
                }.join()
            }
        }
    }

    fun changeStateButton() {
        stateButton.postValue(!stateButton.value!!)
    }

    fun selectImage(uri: String) {
        val currentList = listImageLiveData.value?.toMutableList() ?: mutableListOf()
        if (!currentList.contains(uri)) {
            currentList.add(uri)
            listImageLiveData.postValue(currentList)
        }
        else{
            currentList.remove(uri)
            listImageLiveData.postValue(currentList)
        }
    }

    fun clearListImage(){
        listImageLiveData.value= emptyList()
    }

    fun createConversation(recipient: String){
        viewModelScope.launch {
            interactMessage.createConversation(recipient)
        }
    }
}