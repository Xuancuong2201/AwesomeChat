package com.example.awesomechat.interact

import android.net.Uri
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.model.Messages
import kotlinx.coroutines.flow.Flow

interface InteractMessage {
   val emailCurrent: String
   suspend fun getListMessage(): Flow<List<Messages>>
   suspend fun sentMessage(recipient:String,content:String)
   suspend fun changeStatusMessage(recipient: String)
   suspend fun sentImage(recipient: String,uri: Uri)
   suspend fun sentMultiImage(recipient: String, listImage: List<String>)
   suspend fun createConversation(idUser:String,idRecipient:String):String
   suspend fun listenerChangeDetailsMessage(recipient: String): Flow<List<DetailMessage>>

   suspend fun fetchIdUser(email: String): String?
}