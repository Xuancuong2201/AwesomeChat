package com.example.awesomechat.viewmodel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


@HiltViewModel
class FriendViewModel @Inject constructor(private val interactFriend: InteractFriend) :
    ViewModel() {
    val requestList by lazy { MutableLiveData<List<User>>() }
    val friendList by lazy { MutableLiveData<List<User>>() }
    val invitationList by lazy { MutableLiveData<List<User>>() }
    val quantityRequest by lazy { MutableLiveData<Int>() }
    val userRemainList by lazy { MutableLiveData<List<User>>() }
    val allUserList by lazy {  MediatorLiveData<List<User>>() }
    init {
        viewModelScope.launch {
                interactFriend.getInvitationFriend().collect { users ->
                    val invitationFriend = users.map { item ->
                        item.copy(state = "invitation")
                    }
                    invitationList.postValue(invitationFriend)
                    quantityRequest.postValue(invitationFriend.size)
                }
        }
        viewModelScope.launch {
            interactFriend.getRequestFriend().collect { users ->
                val requestFriend = users.map {
                    it.copy(state = "request")
                }
                requestList.postValue(requestFriend)
            }
        }
        viewModelScope.launch {
            interactFriend.getRemainUser().collect { users ->
                val remainList = users.map { user ->
                    user.copy(state = "user")
                }
                userRemainList.postValue(remainList)
            }
        }
        viewModelScope.launch {
            interactFriend.getStateFriend().collect { users ->
                val listFriend = users.map {
                    it.copy(state = "friend")
                }
                friendList.postValue(listFriend)
            }
        }
        allUserList.addSource(friendList) { users ->
            allUserList.value = handleAllUser(users,requestList.value , userRemainList.value)
        }
        allUserList.addSource(requestList) { users ->
            allUserList.value = handleAllUser(friendList.value,users, userRemainList.value)
        }
        allUserList.addSource(userRemainList) { users ->
            allUserList.value = handleAllUser(friendList.value,requestList.value ,users)
        }
    }
    private fun handleAllUser(users1: List<User>?, users2: List<User>?, users3:List<User>?): List<User> {
        val combinedList = mutableListOf<User>()
        users1?.let { combinedList.addAll(it) }
        users2?.let { combinedList.addAll(it) }
        users3?.let { combinedList.addAll(it) }
        return combinedList.distinct()
    }

    fun combineValues(): MutableLiveData<List<User>> {
        return allUserList
    }
    fun refuseInvitationFriend(sideA: String) {
        viewModelScope.launch {
            interactFriend.refuseInvitationFriend(sideA)
        }
    }
    fun cancelRequestFriend(sideB: String) {
        viewModelScope.launch {
            interactFriend.cancelRequestFriend(sideB)
        }
    }

    fun sendRequestFriend(sideB: String) {
        viewModelScope.launch {
            interactFriend.sendRequestFriend(sideB)
        }
    }

    fun acceptInvitationFriend(sideA: String) {
        viewModelScope.launch {
                interactFriend.acceptInvitationFriend(sideA)
            }
    }
}
