package com.example.awesomechat.interact


interface InteractAuthentication {
    val emailCurrent: String
    fun checkUserCurrent():Boolean
    fun registerAccount(name: String, email: String, password: String, callBack: (Boolean) -> Unit)
    fun login(email: String, password: String, callBack: (Boolean) -> Unit)
    suspend fun  signOut()
    fun addUser(name: String, email: String)
    fun fetchTokenAndUpdate()
    suspend fun updateToken(token: String)
}