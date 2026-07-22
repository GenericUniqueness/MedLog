package com.medlog.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medlog.app.R
import com.medlog.app.ui.MainActivity

/**
 * BroadcastReceiver for appointment reminders scheduled via AlarmManager.
 *
 * Expected Intent extras:
 *   - appointmentId: Long
 *   - profileId: Long
 *   - title: String
 *   - doctor: String?
 *   - time: String (formatted time like "9:30 AM")
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "appointment_reminders"
        const val CHANNEL_NAME = "Appointment Reminders"
        const val CHANNEL_DESC = "Reminders for upcoming appointments"
        private const val NOTIFICATION_ID_BASE = 100_000

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                }
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Ensure notification channel exists
        createChannel(context)

        val appointmentId = intent.getLongExtra("appointmentId", -1L)
        val title = intent.getStringExtra("title") ?: return
        val doctor = intent.getStringExtra("doctor") ?: ""
        val time = intent.getStringExtra("time") ?: ""

        // Content intent — opens the main activity (no deep link)
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = context.getString(
            R.string.notification_appointment_text,
            title,
            doctor.ifBlank { "your doctor" },
            time.ifBlank { "the scheduled time" }
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_appointment))
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        val notificationId = (NOTIFICATION_ID_BASE + (appointmentId % 10_000)).toInt()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }
}