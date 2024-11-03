package com.example.awesomechat.model

import java.util.Date

data class DetailMessage (
    val content:String,
    val sentby: String,
    val status: Boolean,
    var time: Date?=null,
    val type: String,
    val multiImage:MutableList<String>?= mutableListOf()
)