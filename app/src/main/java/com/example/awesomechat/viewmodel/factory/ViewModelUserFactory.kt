package com.example.awesomechat.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.viewmodel.EditViewModel
import com.example.awesomechat.viewmodel.LoginViewmodel
import com.example.awesomechat.viewmodel.PageViewModel


class ViewModelPageFactory private constructor(
    private val interact: InteractAuthentication,
    private val application: Application
) : ViewModelProvider.Factory {
    companion object {
        @Volatile
        private var INSTANCE: ViewModelPageFactory? = null
        fun getInstance(
            interact: InteractAuthentication, application: Application
        ): ViewModelPageFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelPageFactory(
                    interact, application
                ).also { INSTANCE = it }
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
            modelClass.isAssignableFrom(PageViewModel::class.java) -> {
                PageViewModel(interact,application)
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
        viewModels[modelClass] = viewModel
        return viewModel as T
    }
}

class ViewModelEditFactory(private val interact: InteractUser, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            return EditViewModel(interact,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
