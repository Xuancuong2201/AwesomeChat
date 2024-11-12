package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.awesomechat.R
import com.example.awesomechat.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SplashFragment : Fragment() {

    private val viewModel: SplashViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        CoroutineScope(Dispatchers.Main).launch(Dispatchers.Main) {
            delay(2000)
            viewModel.stateLogin.observe(viewLifecycleOwner) { isLoggedIn ->
                val action =
                    if (isLoggedIn) R.id.splash_go_to_HomeFragment else R.id.splash_go_to_loginFragment
                Navigation.findNavController(view).navigate(action)
            }
        }
        return view
    }

}