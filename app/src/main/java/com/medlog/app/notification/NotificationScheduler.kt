package com.medlog.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.data.local.entity.MedicationEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Centralized scheduler for medication and appointment reminders.
 *
 * Medication reminders use [WorkManager] with a 15-minute periodic check.
 * Appointment reminders use [AlarmManager.setExactAndAllowWhileIdle] for
 * precise, one-shot delivery at the scheduled time (minus the lead time).
 */
object NotificationScheduler {

    // ── Unique work / request tags ────────────────────────────────────
    private const val MEDICATION_WORK_TAG_PREFIX = "med_reminder_"
    private const val APPOINTMENT_REQUEST_CODE_BASE = 300_000
    private const val DEFAULT_REMINDER_LEAD_MINUTES = 30L

    // ── Medication Reminders (WorkManager) ───────────────────────────

    /**
     * Schedules a periodic WorkManager check for the given medication.
     * The worker fires every 15 minutes and, if a dose is due within the
     * next check window, sends a broadcast to [MedicationReminderReceiver].
     *
     * @param context application context
     * @param medication the medication to remind about
     * @param profileId the active profile's id
     */
    fun scheduleMedicationReminder(
        context: Context,
        medication: MedicationEntity,
        profileId: Long
    ) {
        cancelMedicationReminder(context, medication.id)

        val workName = "$MEDICATION_WORK_TAG_PREFIX${medication.id}"

        // Store medication info in work Data so the worker can read it
        val data = androidx.work.Data.Builder()
            .putLong("medicationId", medication.id)
            .putLong("profileId", profileId)
            .putString("name", medication.name)
            .putString("dosage", medication.dosage)
            .putString("frequency", medication.frequency)
            .putString("customSchedule", medication.customSchedule)
            .putBoolean("isActive", medication.isActive)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<MedicationCheckWorker>(
            15, TimeUnit.MINUTES
        )
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancels the periodic medication reminder work for the given medication.
     */
    fun cancelMedicationReminder(context: Context, medicationId: Long) {
        val workName = "$MEDICATION_WORK_TAG_PREFIX$medicationId"
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    // ── Appointment Reminders (AlarmManager) ──────────────────────────

    /**
     * Schedules an exact alarm for the appointment time minus the reminder
     * lead time (default 30 minutes).
     *
     * @param context application context
     * @param appointment the appointment to remind about
     * @param profileId the active profile's id
     * @param leadMinutes how many minutes before the appointment to fire (default 30)
     */
    fun scheduleAppointmentReminder(
        context: Context,
        appointment: AppointmentEntity,
        profileId: Long,
        leadMinutes: Long = DEFAULT_REMINDER_LEAD_MINUTES
    ) {
        cancelAppointmentReminder(context, appointment.id)

        if (!appointment.reminderSet) return
        if (appointment.status != "upcoming") return

        val timeStr = appointment.time ?: return
        val date = try {
            LocalDate.parse(appointment.date)
        } catch (e: Exception) {
            return
        }

        val time = try {
            val parts = timeStr.split(":")
            LocalTime.of(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            return
        }

        var triggerDateTime = LocalDateTime.of(date, time)
            .minusMinutes(leadMinutes)

        // If the trigger is in the past, don't schedule
        if (triggerDateTime.isBefore(LocalDateTime.now())) return

        val triggerMillis = triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("appointmentId", appointment.id)
            putExtra("profileId", profileId)
            putExtra("title", appointment.title)
            putExtra("doctor", appointment.doctor ?: "")
            putExtra("time", timeStr)
        }

        val requestCode = (APPOINTMENT_REQUEST_CODE_BASE + (appointment.id % 10_000)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }

    /**
     * Cancels the appointment reminder alarm for the given appointment.
     */
    fun cancelAppointmentReminder(context: Context, appointmentId: Long) {
        val requestCode = (APPOINTMENT_REQUEST_CODE_BASE + (appointmentId % 10_000)).toInt()
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
        // Also try cancelling with FLAG_NO_CREATE (null-safe)
        try {
            val safePendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(safePendingIntent)
            safePendingIntent.cancel()
        } catch (_: Exception) { /* already cancelled */ }
    }
}