package com.example.awesomechat.interact

import com.example.awesomechat.model.User

interface InteractFriend {
    val emailCurrent: String
    suspend fun getStateFriend(email: String, state: String): List<User>
    suspend fun getRequestFriend(email: String, state: String): List<User>
    suspend fun getInvitationFriend(email: String, state: String): List<User>
    suspend fun getAllUser(email:String) : List<User>
    suspend fun sendRequestFriend(sideB:String)
    suspend fun refuseInvitationFriend(sideA:String)
    suspend fun acceptInvitationFriend(sideA:String)
    suspend fun cancelRequestFriend(sideB:String)
    suspend fun getQuantityRequestFriend():Int
}