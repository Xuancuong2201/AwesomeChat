package com.example.awesomechat.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.awesomechat.R
import com.example.awesomechat.broadcast.MyReceiver
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractAuthentication
import com.example.awesomechat.model.Messages
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var interact: InteractAuthentication

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            interact.updateToken(token)
        }
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
            generateNotification(remoteMessage)
    }

    private fun generateNotification(remoteMessage: RemoteMessage) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            loadLargeIcon(remoteMessage.data[InfoFieldQuery.KEY_IMG]!!, object : LoadImageCallback {
                override fun onImageLoaded(bitmap: Bitmap) {
                    notificationBuilder.setLargeIcon(bitmap)
                }

                override fun onError() {
                    notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources,R.drawable.icon_user))
                }
            })
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

            if (remoteMessage.data[InfoFieldQuery.TYPE_NOTIFY].equals(InfoFieldQuery.TYPE_MESS)) {
                val contentInput =
                    RemoteInput.Builder(InfoFieldQuery.KEY_CONTENT).setLabel(getString(R.string.input_message)).build()
                val replyIntent = Intent(applicationContext, MyReceiver::class.java).apply {
                    putExtra(InfoFieldQuery.KEY_EMAIL, remoteMessage.data[InfoFieldQuery.KEY_EMAIL])
                }
                val replyPendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    1,
                    replyIntent,
                    flag
                )
                val message = Messages(
                    url = remoteMessage.data[InfoFieldQuery.KEY_IMG],
                    email = remoteMessage.data[InfoFieldQuery.KEY_EMAIL],
                    name = remoteMessage.data[InfoFieldQuery.KEY_TITLE]
                )
                val pendingIntent = NavDeepLinkBuilder(applicationContext)
                    .setGraph(R.navigation.appchat_nav)
                    .addDestination(R.id.fragmentDetailsMessage)
                    .setArguments(Bundle().apply {
                        putSerializable(InfoFieldQuery.KEY_DETAILS, message)
                    })
                    .createPendingIntent()
                val replyAction = NotificationCompat.Action.Builder(0, "reply", replyPendingIntent)
                    .addRemoteInput(contentInput).build()
                notificationBuilder.setContentText(remoteMessage.data[InfoFieldQuery.KEY_CONTENT])
                    .setContentTitle(remoteMessage.data[InfoFieldQuery.KEY_TITLE])
                    .addAction(replyAction)
                    .setContentIntent(pendingIntent)
            }
            else{
                val pendingIntent = NavDeepLinkBuilder(applicationContext)
                    .setGraph(R.navigation.bottom_nav)
                    .addDestination(R.id.friendFragment)
                    .createPendingIntent()
                notificationBuilder.setContentText(getString(R.string.notify_invitation))
                    .setContentTitle(remoteMessage.data[InfoFieldQuery.KEY_TITLE])
                    .setContentIntent(pendingIntent)
            }
            return notificationManager.notify(InfoFieldQuery.ID_NOTIFY, notificationBuilder.build())
        }
    }

    private fun loadLargeIcon(url: String, callback: LoadImageCallback) {
        Glide.with(applicationContext)
            .asBitmap()
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    callback.onImageLoaded(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    callback.onError()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    interface LoadImageCallback {
        fun onImageLoaded(bitmap: Bitmap)
        fun onError()
    }

}