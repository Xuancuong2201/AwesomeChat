package com.example.awesomechat.model

data class Conversation(
    val idMessages: String? = "",
    val type: String = "",
    val user: ArrayList<String>
)
