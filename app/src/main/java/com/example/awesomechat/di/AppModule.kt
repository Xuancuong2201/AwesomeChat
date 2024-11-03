package com.example.awesomechat.di

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.example.awesomechat.adapter.RecipientMessageAdapter
import com.example.awesomechat.model.User
import com.example.awesomechat.util.DataStoreManager
import com.example.awesomechat.viewmodel.CreateMessViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideDatabase():FirebaseFirestore{
        return Firebase.firestore
    }
    @Provides
    fun provideFirebaseAuth():FirebaseAuth{
        return FirebaseAuth.getInstance()
    }
    @Provides
    fun userCurrent(auth: FirebaseAuth):MutableLiveData<FirebaseUser>{
        return MutableLiveData<FirebaseUser>(auth.currentUser)
    }
    @Provides
    fun provideStorage() : StorageReference{
        return FirebaseStorage.getInstance().getReference("Images")
    }
















}