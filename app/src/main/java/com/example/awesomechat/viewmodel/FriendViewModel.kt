package com.example.awesomechat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendViewModel @Inject constructor(private val interactFriend: InteractFriend) : ViewModel() {
    private val currentUserEmail = interactFriend.emailCurrent
    val requestList by lazy {   MutableLiveData<List<User>>()}
    val invitationList by lazy {   MutableLiveData<List<User>>()}
    val allFriendList by lazy {   MutableLiveData<List<User>>()}
    val quantityRequest  by lazy { MutableLiveData<Int>() }
    val friendList: MutableLiveData<List<User>> = MutableLiveData<List<User>>()

    init {
        viewModelScope.launch {
            val users = interactFriend.getRequestFriend(currentUserEmail, "request")
            users.forEach{ it.state="request" }
            requestList.postValue(users)
        }
        viewModelScope.launch {
            val users = interactFriend.getInvitationFriend(currentUserEmail, "request")
            users.forEach{ it.state="invitation" }
            invitationList.postValue(users)
        }
        viewModelScope.launch{
            val  user=interactFriend.getAllUser(currentUserEmail)
            allFriendList.postValue(user)
        }
        viewModelScope.launch {
            val users = interactFriend.getStateFriend(currentUserEmail, "friend")
            users.forEach{ it.state="friend" }
            friendList.postValue(users)
        }
        viewModelScope.launch {
            val quantity = interactFriend.getQuantityRequestFriend()
            quantityRequest.postValue(quantity)
        }
    }

     suspend fun refuseInvitationFriend(sideA:String){
        interactFriend.refuseInvitationFriend(sideA)
    }
    suspend fun cancelRequestFriend(sideB:String){
        interactFriend.cancelRequestFriend(sideB)
    }
    suspend fun sendRequestFriend(sideB:String){
        interactFriend.sendRequestFriend(sideB)
    }
    suspend fun acceptInvitationFriend(sideA:String){
        interactFriend.acceptInvitationFriend(sideA)
    }
}
