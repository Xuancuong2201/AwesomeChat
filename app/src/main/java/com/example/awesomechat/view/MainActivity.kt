package com.example.awesomechat.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
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
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            sharedPref =
                newBase.getSharedPreferences(InfoFieldQuery.CHANGE_LANGUAGE, Context.MODE_PRIVATE)
            val myLang = sharedPref.getString(InfoFieldQuery.LANGUAGE, InfoFieldQuery.ENGLISH)!!
            val configuration = Configuration(newBase.resources.configuration)
            configuration.setLocale(Locale(myLang))

            val updatedContext = newBase.createConfigurationContext(configuration)

            super.attachBaseContext(updatedContext)
        } else
            super.attachBaseContext(null)


    }
}