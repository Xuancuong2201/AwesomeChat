package com.example.awesomechat.api

import com.example.awesomechat.interact.InfoFieldQuery
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {
    private const val MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToken(): String? {
        try {
            val jsonString = InfoFieldQuery.JSON_STRING
            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredential = GoogleCredentials.fromStream(stream).createScoped(
                arrayListOf(
                    MESSAGING_SCOPE
                )
            )
            googleCredential.refresh()

            return googleCredential.accessToken.tokenValue
        } catch (e: IOException) {
            return null
        }
    }
}