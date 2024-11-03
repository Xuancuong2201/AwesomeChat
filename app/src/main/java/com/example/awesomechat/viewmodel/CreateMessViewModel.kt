package com.example.awesomechat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.User
import kotlinx.coroutines.launch

class CreateMessViewModel(private val interactMessage: InteractMessage) : ViewModel() {
    val user: MutableLiveData<User> by lazy { MutableLiveData<User>() }
    val position : MutableLiveData<Int> by lazy { MutableLiveData<Int>(-1) }

}