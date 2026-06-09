package com.example.homesmartpantry.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.homesmartpantry.MainActivity
import com.example.homesmartpantry.R

object NotificationHelper {
    private const val CHANNEL_ID = "expiry_reminder"
    private const val CHANNEL_NAME = "食材过期提醒"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "食材即将过期时发送提醒通知"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showExpiryNotification(
        context: Context,
        expiredCount: Int,
        expiringSoonCount: Int,
        itemNames: List<String>
    ) {
        if (expiredCount == 0 && expiringSoonCount == 0) return

        val title = buildString {
            if (expiredCount > 0) append("${expiredCount}种食材已过期")
            if (expiredCount > 0 && expiringSoonCount > 0) append("，")
            if (expiringSoonCount > 0) append("${expiringSoonCount}种即将过期")
        }

        val body = itemNames.take(5).joinToString("、") +
                if (itemNames.size > 5) " 等${itemNames.size}种" else ""

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Check permission on Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }
}
