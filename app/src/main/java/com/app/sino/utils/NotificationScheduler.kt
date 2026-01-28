package com.app.sino.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.sino.R
import com.app.sino.data.remote.dto.EventDto
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    const val CHANNEL_ID = "sino_event_channel"
    const val CHANNEL_NAME = "Sino Event Notifications"
    const val CHANNEL_DESCRIPTION = "Notifications for your scheduled events in Sino."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, event: EventDto) {
        // Ensure event ID is not null for scheduling unique notifications
        val eventId = event.idEvents ?: return

        // Calculate notification time (e.g., 15 minutes before the event)
        val eventDateTime = event.eventDate.atTime(event.eventTime ?: java.time.LocalTime.of(0, 0))
        val notificationTimeMillis = eventDateTime
            .minusMinutes(15) // Notify 15 minutes before
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Only schedule if the notification time is in the future
        if (notificationTimeMillis > System.currentTimeMillis()) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("event_id", eventId)
                putExtra("event_title", event.dscTitle)
                putExtra("event_description", event.dscDescription ?: "No description")
                putExtra("event_date", event.eventDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                putExtra("event_time", event.eventTime?.format(DateTimeFormatter.ISO_LOCAL_TIME) ?: "")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId, // Use eventId as request code for unique pending intents
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelNotification(context: Context, eventId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
