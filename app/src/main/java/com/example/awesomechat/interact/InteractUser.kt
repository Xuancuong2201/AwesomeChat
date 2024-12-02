package com.example.awesomechat.interact

import com.example.awesomechat.model.User

interface InteractUser {
    val emailCurrent: String
    fun updaterRecordUser(
        user: User, callBack: (Boolean) -> Unit
    )
    suspend fun getRecordUser(email: String ): User?
    fun signOut()
}