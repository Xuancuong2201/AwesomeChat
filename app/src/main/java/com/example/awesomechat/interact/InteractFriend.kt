package com.example.awesomechat.interact

import com.example.awesomechat.model.User
import kotlinx.coroutines.flow.Flow

interface InteractFriend {
    val emailCurrent: String
    suspend fun getStateFriend():  Flow<List<User>>
    suspend fun getRequestFriend():  Flow<List<User>>
    suspend fun getInvitationFriend(): Flow<List<User>>
    suspend fun  getRemainUser() : Flow<List<User>>
    suspend fun sendRequestFriend(sideB:String)
    suspend fun refuseInvitationFriend(sideA:String)
    suspend fun acceptInvitationFriend(sideA:String)
    suspend fun cancelRequestFriend(sideB:String)
}