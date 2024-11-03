package com.example.awesomechat.interact

import android.net.Uri
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.model.Messages

interface InteractMessage {
   val emailCurrent: String
   suspend fun getListMessage():List<Messages>
   suspend fun sentMessage(recipient:String,content:String)
   suspend fun getDetailsMessage(recipient: String):List<DetailMessage>
   suspend fun changeStatusMessage(emailRecipient: String)
   suspend fun sentImage(recipient: String,uri: Uri)
   suspend fun sentMultiImage(recipient: String, listImage: List<String>)
   suspend fun createConversation(recipient: String)
}