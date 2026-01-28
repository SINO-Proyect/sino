package com.app.sino.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.app.sino.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val eventId = intent?.getIntExtra("event_id", -1) ?: -1
            val eventTitle = intent?.getStringExtra("event_title") ?: "Event Reminder"
            val eventDescription = intent?.getStringExtra("event_description") ?: "You have an upcoming event."
            val eventDate = intent?.getStringExtra("event_date") ?: ""
            val eventTime = intent?.getStringExtra("event_time") ?: ""

            // Build the notification
            val builder = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(eventTitle)
                .setContentText("$eventDescription at $eventTime on $eventDate")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            // Show the notification with permission check
            val notificationManager = NotificationManagerCompat.from(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(eventId, builder.build())
                }
            } else {
                notificationManager.notify(eventId, builder.build())
            }
        }
    }
}