package com.example.awesomechat.model

data class NotificationData(
    val token: String? = null,
    val data: HashMap<String, String>
)

data class Notification(
    val message: NotificationData? = null
)