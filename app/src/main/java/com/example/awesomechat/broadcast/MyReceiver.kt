package com.example.awesomechat.broadcast

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MyReceiver : BroadcastReceiver() {
    @Inject
    lateinit var interact: InteractMessage
    override fun onReceive(context: Context?, intent: Intent?) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val content = remoteInput.getCharSequence(InfoFieldQuery.KEY_CONTENT).toString()
            val email = intent?.getStringExtra(InfoFieldQuery.KEY_EMAIL).toString()
            CoroutineScope(Dispatchers.IO).launch {
                interact.sentMessage(email, content)
                withContext(Dispatchers.Main) {
                    val notificationManager =
                        context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(InfoFieldQuery.ID_NOTIFY)
                }
            }
        }
    }
}
