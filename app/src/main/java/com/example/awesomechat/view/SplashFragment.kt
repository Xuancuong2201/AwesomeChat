package com.example.awesomechat.view

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.awesomechat.R
import kotlinx.coroutines.*


class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        GlobalScope.launch(Dispatchers.Main) {
            delay(2000)
            Navigation.findNavController(view).navigate(R.id.go_to_loginFragment)
        }
        return view
    }


}