package com.example.awesomechat.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.awesomechat.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("ChangeLanguage", Context.MODE_PRIVATE)
        setLanguage()
        this.setContentView(R.layout.activity_main)
    }

    private fun setLanguage() {
        val configuration = resources.configuration
        val myLang = sharedPref.getString("language", "vi")!!
        val locale = Locale(myLang)
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}