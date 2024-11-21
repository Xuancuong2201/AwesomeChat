package com.example.awesomechat.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.awesomechat.R
import com.example.awesomechat.interact.InfoFieldQuery
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        sharedPref = getSharedPreferences(InfoFieldQuery.CHANGE_LANGUAGE, Context.MODE_PRIVATE)
        setLanguage()

    }

    private fun setLanguage() {
        val configuration = resources.configuration
        val myLang = sharedPref.getString(InfoFieldQuery.LANGUAGE, InfoFieldQuery.VIETNAM)!!
        val locale = Locale(myLang)
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

}