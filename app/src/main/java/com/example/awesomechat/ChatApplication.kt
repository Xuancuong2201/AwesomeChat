package com.example.awesomechat

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class ChatApplication : Application(){

    override fun onCreate() {
        super.onCreate()

    }
}