package com.example.awesomechat.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.viewmodel.LoginViewmodel
import com.example.awesomechat.viewmodel.SignUpViewModel

class ViewModelAuthenticationFactory private constructor(private val interact: InteractAuthentication) :
    ViewModelProvider.Factory {
    companion object {
        @Volatile
        private var INSTANCE: ViewModelAuthenticationFactory? = null
        fun getInstance(interact: InteractAuthentication): ViewModelAuthenticationFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelAuthenticationFactory(interact).also { INSTANCE = it }
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
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(interact)
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
        viewModels[modelClass] = viewModel
        return viewModel as T
    }
}

class ViewModelLoginFactory(private val interact: InteractAuthentication, private val interactUser: InteractUser, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(LoginViewmodel::class.java)) {
           return LoginViewmodel(interact, interactUser, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


