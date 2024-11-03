package com.example.awesomechat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CreateMessViewModel @Inject constructor() : ViewModel() {
    val user: MutableLiveData<User> by lazy { MutableLiveData<User>() }
    val position : MutableLiveData<Int> by lazy { MutableLiveData<Int>(-1) }

}