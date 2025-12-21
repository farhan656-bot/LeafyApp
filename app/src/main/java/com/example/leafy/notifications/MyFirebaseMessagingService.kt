package com.example.leafy.notifications

import android.util.Log
import com.example.leafy.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Kirim token ke server (opsional)
        Log.d("FCM_TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // kalau kamu pakai notification payload
        message.notification?.let {
            val title = it.title ?: "Leafy"
            val body = it.body ?: "Ada notifikasi baru"

            // pastikan channel ada
            NotificationUtils.createNotificationChannel(applicationContext)

            NotificationUtils.showNotification(
                context = applicationContext,
                title = title,
                body = body
            )
        }
    }
}
