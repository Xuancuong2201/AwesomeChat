package com.example.awesomechat.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.viewmodel.ChatViewModel
import com.example.awesomechat.viewmodel.CreateMessViewModel
import com.example.awesomechat.viewmodel.DetailsMessageViewModel


class ViewModelChatFactory(private val interactMessage: InteractMessage) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        val viewModel = when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(interactMessage) as T
            }
            modelClass.isAssignableFrom(DetailsMessageViewModel::class.java) -> {
                DetailsMessageViewModel(interactMessage) as T
            }
            modelClass.isAssignableFrom(CreateMessViewModel::class.java) -> {
                CreateMessViewModel(interactMessage) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
        return viewModel
    }
}