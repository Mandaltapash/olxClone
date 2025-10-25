package com.tods.project_olx.service

import com.tods.project_olx.analytics.AnalyticsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tods.project_olx.R
import com.tods.project_olx.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Log notification received
        analyticsManager.logCustomEvent("notification_received", mapOf(
            "from" to (remoteMessage.from ?: "unknown")
        ))

        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            handleDataPayload(remoteMessage.data)
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            showNotification(
                it.title ?: "New Notification",
                it.body ?: "",
                remoteMessage.data
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server
        sendTokenToServer(token)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        when (data["type"]) {
            "new_message" -> handleNewMessage(data)
            "ad_update" -> handleAdUpdate(data)
            "price_drop" -> handlePriceDrop(data)
            else -> showGenericNotification(data)
        }
    }

    private fun handleNewMessage(data: Map<String, String>) {
        val senderId = data["sender_id"]
        val message = data["message"]
        showNotification(
            "New Message",
            message ?: "You have a new message",
            data,
            CHANNEL_MESSAGES
        )
    }

    private fun handleAdUpdate(data: Map<String, String>) {
        val adTitle = data["ad_title"]
        showNotification(
            "Ad Updated",
            "Your ad '$adTitle' has been updated",
            data,
            CHANNEL_ADS
        )
    }

    private fun handlePriceDrop(data: Map<String, String>) {
        val adTitle = data["ad_title"]
        val newPrice = data["new_price"]
        showNotification(
            "Price Drop Alert!",
            "$adTitle is now $newPrice",
            data,
            CHANNEL_PRICE_ALERTS
        )
    }

    private fun showGenericNotification(data: Map<String, String>) {
        showNotification(
            data["title"] ?: "New Notification",
            data["body"] ?: "",
            data
        )
    }

    private fun showNotification(
        title: String,
        message: String,
        data: Map<String, String>,
        channelId: String = CHANNEL_DEFAULT
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(notificationManager)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_DEFAULT,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New chat messages"
                },
                NotificationChannel(
                    CHANNEL_ADS,
                    "Ad Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates about your ads"
                },
                NotificationChannel(
                    CHANNEL_PRICE_ALERTS,
                    "Price Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Price drop notifications"
                }
            )

            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement server API call to save token
        analyticsManager.logCustomEvent("fcm_token_refreshed", mapOf(
            "token_length" to token.length
        ))
    }

    companion object {
        private const val CHANNEL_DEFAULT = "default"
        private const val CHANNEL_MESSAGES = "messages"
        private const val CHANNEL_ADS = "ads"
        private const val CHANNEL_PRICE_ALERTS = "price_alerts"
    }
}