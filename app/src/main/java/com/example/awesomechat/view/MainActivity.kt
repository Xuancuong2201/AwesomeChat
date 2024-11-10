package com.example.awesomechat.view

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.example.awesomechat.ChatApplication
import com.example.awesomechat.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var myApp: ChatApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myApp = application as ChatApplication
        setLanguage()
        this.setContentView(R.layout.activity_main)
    }
    private fun setLanguage(){
        val configuration = resources.configuration
        val myLang =myApp.sharedPref.getString("language","vi")!!
        val locale = Locale(myLang)
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}