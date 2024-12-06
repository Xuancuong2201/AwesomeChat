package com.example.awesomechat.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentSplashBinding
import com.example.awesomechat.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment @Inject constructor() : FragmentBase<FragmentSplashBinding>() {
    private val viewModel: SplashViewModel by viewModels()

    override fun getFragmentView(): Int = R.layout.fragment_splash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            viewModel.stateLogin.observe(viewLifecycleOwner) { isLoggedIn ->
                try {
                    val action =
                        if (isLoggedIn) R.id.splash_go_to_HomeFragment else R.id.splash_go_to_loginFragment
                    if (isLoggedIn) {
                        viewModel.updateToken()
                    }
                    Navigation.findNavController(view).navigate(action)
                } catch (e: Exception) {
                    Navigation.findNavController(view).navigate(R.id.splash_go_to_loginFragment)
                }
            }
        }
    }


}