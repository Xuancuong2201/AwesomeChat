package com.example.awesomechat.model

import java.io.Serializable

data class User(
    val url: String = "",
    val name: String = "",
    val email: String = "",
    val numberphone: String = "",
    val birthday: String = "",
    var state: String = "",
    val token: String = ""
) : Serializable
