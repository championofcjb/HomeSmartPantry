package com.example.homesmartpantry.presentation.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.homesmartpantry.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpiryAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CHECK_EXPIRY) {
            checkAndNotify(context)
        }
    }

    private fun checkAndNotify(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val now = System.currentTimeMillis()
                val threeDaysLater = now + 3 * 24 * 60 * 60 * 1000L

                val allInventory = db.inventoryDao().getAllInventory()
                    // collect first emission
                var expiredItems = emptyList<String>()
                var expiringItems = emptyList<String>()

                db.inventoryDao().getExpiredItems(now).forEach { entity ->
                    val ingredient = db.ingredientDao().getIngredientById(entity.ingredientId)
                    if (ingredient != null) expiredItems = expiredItems + ingredient.name
                }

                db.inventoryDao().getExpiringSoon(now, threeDaysLater).forEach { entity ->
                    val ingredient = db.ingredientDao().getIngredientById(entity.ingredientId)
                    if (ingredient != null &&
                        ingredient.name !in expiredItems) {
                        expiringItems = expiringItems + ingredient.name
                    }
                }

                val allNames = expiredItems + expiringItems
                NotificationHelper.showExpiryNotification(
                    context,
                    expiredItems.size,
                    expiringItems.size,
                    allNames
                )
            } catch (_: Exception) {
                // Silently fail — DB may be temporarily unavailable
            }
        }
    }

    companion object {
        const val ACTION_CHECK_EXPIRY = "com.example.homesmartpantry.CHECK_EXPIRY"
        private const val REQUEST_CODE = 2024

        fun scheduleDailyChecks(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Cancel existing alarms first
            cancelAlarms(context)

            val intent = Intent(context, ExpiryAlarmReceiver::class.java).apply {
                action = ACTION_CHECK_EXPIRY
            }

            // Schedule for both weekday and weekend times
            // The receiver will just check, so we can schedule multiple times
            val now = Calendar.getInstance()

            // Today's check times
            scheduleAlarm(context, alarmManager, intent, 8, 15)   // 08:15 weekday
            scheduleAlarm(context, alarmManager, intent, 11, 0)   // 11:00 weekend
            scheduleAlarm(context, alarmManager, intent, 20, 0)   // 20:00 weekday
        }

        private fun scheduleAlarm(
            context: Context,
            alarmManager: AlarmManager,
            intent: Intent,
            hourOfDay: Int,
            minute: Int
        ) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If the time has passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val uniqueCode = "$REQUEST_CODE${hourOfDay}${minute}".hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ allows exact alarms
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        }

        private fun cancelAlarms(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ExpiryAlarmReceiver::class.java).apply {
                action = ACTION_CHECK_EXPIRY
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        // Check if current time matches the reminder schedule
        fun shouldRemindNow(): Boolean {
            val now = Calendar.getInstance()
            val hour = now.get(Calendar.HOUR_OF_DAY)
            val minute = now.get(Calendar.MINUTE)
            val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)

            val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY

            return when {
                isWeekday -> (hour == 8 && minute in 0..20) || (hour == 20 && minute in 0..5)
                else -> hour == 11 && minute in 0..5
            }
        }
    }
}
