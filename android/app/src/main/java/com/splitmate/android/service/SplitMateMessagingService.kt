package com.splitmate.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.splitmate.android.R

class SplitMateMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body  = message.notification?.body  ?: return

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "splitmate")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon since R.drawable.ic_notification might not exist
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        // Send FCM token to your backend
        CoroutineScope(Dispatchers.IO).launch {
            // api.updateFcmToken(token)
            println("New FCM token: $token")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SplitMate Notifications"
            val descriptionText = "Notifications for SplitMate expenses and groups"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("splitmate", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}