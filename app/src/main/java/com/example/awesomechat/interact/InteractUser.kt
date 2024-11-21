package com.example.awesomechat.interact

import android.net.Uri
import com.example.awesomechat.model.User

interface InteractUser {
    val emailCurrent: String
    fun updaterRecordUser(
        imageUri: Uri?,
        email: String,
        name: String,
        numberPhone: String,
        birthDay: String
    )

    suspend fun getRecordUser(email: String): User?
    suspend fun getUrlUser(email: String): String?
    fun signOut()
}