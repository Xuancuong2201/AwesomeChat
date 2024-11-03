package com.example.awesomechat.util

import android.content.Context
import android.util.Log
import com.example.awesomechat.UserPreferences
import com.example.awesomechat.model.User
import com.example.awesomechat.util.UserInfoSerializer.userInfoDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

object DataStoreManager {
    suspend fun getSavedInformationUser(context:Context): User {
        return withContext(Dispatchers.IO) {
            val userInfo = context.userInfoDataStore.data.firstOrNull()
            if (userInfo == null) {
                val user = User()
                return@withContext user
            } else {
                return@withContext User(userInfo.url,userInfo.name,userInfo.email,userInfo.numberphone,userInfo.birthday)
            }
        }
    }
    suspend fun saveInformationUser(user:User, context: Context){
        try {
            withContext(Dispatchers.IO) {
                context.userInfoDataStore.updateData { currentUser: UserPreferences ->
                    currentUser.toBuilder()
                        .setUrl(user.url.takeIf { it.isNotEmpty() } ?: "")
                        .setName(user.name)
                        .setEmail(user.email)
                        .setBirthday(user.birthday.takeIf { it.isNotEmpty() } ?: "")
                        .setNumberphone(user.numberphone.takeIf { it.isNotEmpty() } ?: "")
                        .build()
                }
            }
        } catch(e: Exception) {
            Log.e("Error", "Error writing to proto store: $e")
            throw e
        }
    }
}