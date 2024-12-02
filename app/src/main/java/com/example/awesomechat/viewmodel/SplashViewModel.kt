package com.example.awesomechat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.awesomechat.interact.InteractAuthentication
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val interactAu: InteractAuthentication) : ViewModel() {
    val stateLogin: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(interactAu.checkUserCurrent()) }
    fun updateToken() {
        interactAu.fetchTokenAndUpdate()
    }
}