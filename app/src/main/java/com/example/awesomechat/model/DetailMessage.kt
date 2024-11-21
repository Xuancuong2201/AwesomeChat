package com.example.awesomechat.model

import java.util.Date

data class DetailMessage(
    val content: String = "",
    var sentby: String = "",
    var status: Boolean = false,
    var time: Date? = null,
    val type: String = "",
    val multiImage: MutableList<String>? = mutableListOf(),
    var show: Boolean = true
)