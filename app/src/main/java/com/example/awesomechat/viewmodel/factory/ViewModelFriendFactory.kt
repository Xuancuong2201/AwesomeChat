package com.example.awesomechat.viewmodel.factory


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.viewmodel.CreateMessViewModel
import com.example.awesomechat.viewmodel.DetailsMessageViewModel
import com.example.awesomechat.viewmodel.EditViewModel
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel

class ViewModelFriendFactory(private val interactFriend: InteractFriend) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        val viewModel = when {
            modelClass.isAssignableFrom(FriendViewModel::class.java) -> {
                FriendViewModel(interactFriend) as T
            }

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel() as T
            }
           else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
        return viewModel
    }
}

class ViewModelSearchFactory private constructor() : ViewModelProvider.Factory {
    companion object {
        @Volatile
        private var INSTANCE: ViewModelSearchFactory? = null
        fun getInstance(): ViewModelSearchFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?:ViewModelSearchFactory().also { INSTANCE = it }
            }
        }
    }
    private val viewModels: MutableMap<Class<out ViewModel>, ViewModel> = mutableMapOf()
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return viewModels[modelClass] as? T ?: createNewViewModel(modelClass)
    }
    private fun <T : ViewModel> createNewViewModel(modelClass: Class<T>): T {
        val viewModel = when {

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel()
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
        viewModels[modelClass] = viewModel
        return viewModel as T
    }
}
