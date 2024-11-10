package com.example.awesomechat.model


import android.os.Parcelable
import androidx.datastore.core.Serializer
import java.io.Serializable
import java.util.Date


data class Messages(var url:String?="", var email:String?="", var name:String?="", var currentMessage:String?="", var time: Date?=null, var quantity:Int?=0, var status:Boolean=false,var type :String?=""): Serializable
