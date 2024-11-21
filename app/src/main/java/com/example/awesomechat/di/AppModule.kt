package com.example.awesomechat.di


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.example.awesomechat.R
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.interact.InteractMessage
import com.example.awesomechat.interact.InteractUser
import com.example.awesomechat.repository.firebase.AuthenticationRepository
import com.example.awesomechat.repository.firebase.FriendRepository
import com.example.awesomechat.repository.firebase.MessageRepository
import com.example.awesomechat.repository.firebase.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideDatabase(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun userCurrent(auth: FirebaseAuth): MutableLiveData<FirebaseUser> {
        return MutableLiveData<FirebaseUser>(auth.currentUser)
    }

    @Provides
    fun provideEmail(auth: FirebaseAuth): String {
        return auth.currentUser?.email.toString()
    }

    @Provides
    fun provideStorage(): StorageReference {
        return FirebaseStorage.getInstance().getReference("Images")
    }

    @Singleton
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, InfoFieldQuery.CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_login)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    @Singleton
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManagerCompat {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                InfoFieldQuery.CHANNEL_ID,
                InfoFieldQuery.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindInteractFriend(friendRepository: FriendRepository): InteractFriend

    @Binds
    abstract fun bindInteractAuthentication(authenticationRepository: AuthenticationRepository): InteractAuthentication

    @Binds
    abstract fun bindInteractMessage(messageRepository: MessageRepository): InteractMessage

    @Binds
    abstract fun bindInteractUser(userRepository: UserRepository): InteractUser
}