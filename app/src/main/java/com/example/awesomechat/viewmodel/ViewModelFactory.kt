package com.example.awesomechat.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.awesomechat.repository.AuthenticationRepository

class MyViewModelFactory(private val repository: AuthenticationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SignUpViewModel(repository) as T
    }
}