package com.example.awesomechat.viewmodel

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomechat.api.NotificationAPI
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.model.Notification
import com.example.awesomechat.model.NotificationData
import com.example.awesomechat.model.User
import com.example.awesomechat.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsMessageViewModel @Inject constructor(
    private val interactMessage: InteractMessage,
    @ApplicationContext val context: Context
) :
    ViewModel() {
    private lateinit var user: User
    val name by lazy { MutableLiveData<String>() }
    val imageUrl by lazy { MutableLiveData<String>() }
    val email by lazy { MutableLiveData<String>() }
    val content by lazy { MutableLiveData<String>() }
    val listDetailsMessage by lazy { MutableLiveData<List<DetailMessage>>() }
    val listImageLiveData by lazy { MutableLiveData<List<String>>() }
    val stateButton by lazy { MutableLiveData(false) }

    init {
        viewModelScope.launch {
            user = DataStoreManager.getSavedInformationUser(context)
        }
    }

    fun getDetailsMessage(recipient: String) {
        viewModelScope.launch {
            val idUser: String = interactMessage.fetchIdUser(interactMessage.emailCurrent)!!
            interactMessage.listenerChangeDetailsMessage(recipient).collect { messages ->
                val updatedMessages = messages.map { message ->
                    message.apply {
                        sentby =
                            if (this.sentby == idUser) InfoFieldQuery.STATE_USER else InfoFieldQuery.STATE_RECIPIENT
                    }
                }
                val sortedMessages = updatedMessages.sortedBy { it.time }
                listDetailsMessage.postValue(sortedMessages)
            }
        }
    }

    fun sentMessage(recipient: String, content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            interactMessage.sentMessage(recipient, content)
            sendNotificationMessage(recipient, content)
        }
    }

    fun changeStateButton() {
        stateButton.postValue(!stateButton.value!!)

        if (stateButton.value!!) {
            listImageLiveData.postValue(emptyList())
        }
    }

    fun selectImage(uri: String) {
        val currentList = listImageLiveData.value?.toMutableList() ?: mutableListOf()
        if (currentList.contains(uri)) {
            currentList.remove(uri)
        } else {
            currentList.add(uri)
        }
        listImageLiveData.postValue(currentList)
    }

    fun clearListImage() {
        listImageLiveData.value = emptyList()
    }

    suspend fun sentImage(recipient: String, content: String) {
        sendNotificationMessage(recipient, content)
        if (listImageLiveData.value != null) {
            if (listImageLiveData.value!!.size == 1) {
                viewModelScope.launch {
                    interactMessage.sentImage(
                        recipient,
                        listImageLiveData.value!!.firstOrNull()!!.toUri()
                    )
                }.join()
            } else {
                viewModelScope.launch {
                    interactMessage.sentMultiImage(recipient, listImageLiveData.value!!)
                }.join()
            }
        }
    }

    private fun sendNotificationMessage(recipient: String, content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val token = interactMessage.getTokenUser(recipient)
            if (token != null) {

                val notification = Notification(
                    message = NotificationData(
                        token, hashMapOf(
                            InfoFieldQuery.KEY_TITLE to user.name,
                            InfoFieldQuery.KEY_CONTENT to content,
                            InfoFieldQuery.KEY_IMG to user.url,
                            InfoFieldQuery.KEY_EMAIL to interactMessage.emailCurrent,
                            InfoFieldQuery.TYPE_NOTIFY to InfoFieldQuery.TYPE_MESS
                        )
                    )
                )
                NotificationAPI.sendNotification().notification(notification).execute()
            }
        }
    }

    suspend fun getListImageFromGallery(): List<String> {
        return withContext(Dispatchers.IO) {
            val imageList = mutableListOf<String>()
            val projection: Array<String> = arrayOf(MediaStore.Images.Media._ID)
            val mCursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
            )
            mCursor?.use { cursor ->
                val index = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(index)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    imageList.add(contentUri.toString())
                }
            }
            imageList
        }
    }
}