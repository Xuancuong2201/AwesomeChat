package com.example.awesomechat.viewmodel

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.model.User
import com.example.awesomechat.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendViewModel @Inject constructor(
    private val interactFriend: InteractFriend,
    @ApplicationContext val context: Context
) :
    ViewModel() {
    private lateinit var user: User
    val requestList by lazy { MutableLiveData<List<User>>() }
    val friendList by lazy { MutableLiveData<List<User>>() }
    val invitationList by lazy { MutableLiveData<List<User>>() }
    val quantityRequest by lazy { MutableLiveData<Int>() }
    private val userRemainList by lazy { MutableLiveData<List<User>>() }
    private val allUserList by lazy { MediatorLiveData<List<User>>() }

    init {
        viewModelScope.launch {
            user = DataStoreManager.getSavedInformationUser(context)
        }
        viewModelScope.launch {
            interactFriend.getInvitationFriend().collect { users ->
                val invitationFriend = users.map { item ->
                    item.copy(state = InfoFieldQuery.STATE_INVITATION)
                }
                invitationList.postValue(invitationFriend)
                quantityRequest.postValue(invitationFriend.size)
            }
        }
        viewModelScope.launch {
            interactFriend.getRequestFriend().collect { users ->
                val requestFriend = users.map {
                    it.copy(state = InfoFieldQuery.STATE_REQUEST)
                }
                requestList.postValue(requestFriend)
            }
        }
        viewModelScope.launch {
            interactFriend.getRemainUser().collect { users ->
                val remainList = users.map { user ->
                    user.copy(state = InfoFieldQuery.STATE_USER)
                }
                userRemainList.postValue(remainList)
            }
        }
        viewModelScope.launch {
            interactFriend.getStateFriend().collect { users ->
                val listFriend = users.map {
                    it.copy(state = InfoFieldQuery.STATE_FRIEND)
                }
                friendList.postValue(listFriend)
            }
        }
        allUserList.addSource(friendList) { users ->
            allUserList.value = handleAllUser(users, requestList.value, userRemainList.value)
        }
        allUserList.addSource(requestList) { users ->
            allUserList.value = handleAllUser(friendList.value, users, userRemainList.value)
        }
        allUserList.addSource(userRemainList) { users ->
            allUserList.value = handleAllUser(friendList.value, requestList.value, users)
        }
    }

    private fun handleAllUser(
        users1: List<User>?,
        users2: List<User>?,
        users3: List<User>?
    ): List<User> {
        val combinedList = mutableListOf<User>()
        users1?.let { combinedList.addAll(it) }
        users2?.let { combinedList.addAll(it) }
        users3?.let { combinedList.addAll(it) }
        return combinedList.distinct()
    }

    fun combineValues(): MutableLiveData<List<User>> {
        return allUserList
    }

}
