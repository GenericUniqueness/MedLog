package com.medlog.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.MedicationLogEntity
import com.medlog.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * BroadcastReceiver for periodic medication reminders.
 *
 * On initial fire: shows a notification with "Taken" and "Skip" action buttons.
 * When a "TAKEN" action is received: inserts a MedicationLogEntity with status "taken".
 * When a "SKIPPED" action is received: inserts a MedicationLogEntity with status "skipped".
 *
 * Expected Intent extras:
 *   - medicationId: Long
 *   - profileId: Long
 *   - name: String
 *   - dosage: String
 *   - action: String? ("TAKEN" or "SKIPPED" — present only for action callbacks)
 */
class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val CHANNEL_NAME = "Medication Reminders"
        const val CHANNEL_DESC = "Reminders to take your medications"
        private const val NOTIFICATION_ID_BASE = 200_000
        const val ACTION_TAKEN = "TAKEN"
        const val ACTION_SKIPPED = "SKIPPED"

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
        createChannel(context)

        val medicationId = intent.getLongExtra("medicationId", -1L)
        val profileId = intent.getLongExtra("profileId", -1L)
        val name = intent.getStringExtra("name") ?: return
        val dosage = intent.getStringExtra("dosage") ?: ""
        val action = intent.getStringExtra("action")

        when (action) {
            ACTION_TAKEN -> {
                insertLog(context, medicationId, profileId, "taken")
                dismissNotification(context, medicationId)
            }
            ACTION_SKIPPED -> {
                insertLog(context, medicationId, profileId, "skipped")
                dismissNotification(context, medicationId)
            }
            else -> {
                // Initial reminder — show notification with actions
                showReminderNotification(context, medicationId, profileId, name, dosage)
            }
        }
    }

    private fun showReminderNotification(
        context: Context,
        medicationId: Long,
        profileId: Long,
        name: String,
        dosage: String
    ) {
        val notificationText = context.getString(
            R.string.notification_medication_text,
            name,
            dosage
        )

        // Content intent — opens the app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            medicationId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Taken" action — carries profileId in its own extras
        val takenIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("medicationId", medicationId)
            putExtra("profileId", profileId)
            putExtra("name", name)
            putExtra("dosage", dosage)
            putExtra("action", ACTION_TAKEN)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 2 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Skip" action
        val skipIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("medicationId", medicationId)
            putExtra("profileId", profileId)
            putExtra("name", name)
            putExtra("dosage", dosage)
            putExtra("action", ACTION_SKIPPED)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 2 + 2).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(context.getString(R.string.notification_medication))
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Taken", takenPendingIntent)
            .addAction(0, "Skip", skipPendingIntent)
            .build()

        val notificationId = (NOTIFICATION_ID_BASE + (medicationId % 10_000)).toInt()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    private fun insertLog(
        context: Context,
        medicationId: Long,
        profileId: Long,
        status: String
    ) {
        if (profileId == -1L) return
        val app = context.applicationContext as? MedLogApplication ?: return
        val log = MedicationLogEntity(
            medicationId = medicationId,
            profileId = profileId,
            takenAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            status = status
        )
        CoroutineScope(Dispatchers.IO).launch {
            app.database.medicationLogDao().insert(log)
        }
    }

    private fun dismissNotification(context: Context, medicationId: Long) {
        val notificationId = (NOTIFICATION_ID_BASE + (medicationId % 10_000)).toInt()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationId)
    }
}