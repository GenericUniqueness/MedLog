package com.medlog.app.notification

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.medlog.app.MedLogApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * WorkManager worker that runs every 15 minutes to check if a medication
 * dose is due. If the current time falls within the medication's scheduled
 * window and no log has been recorded for the current window, it fires
 * a broadcast to [MedicationReminderReceiver] which shows the notification.
 */
class MedicationCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val medicationId = inputData.getLong("medicationId", -1L)
        val profileId = inputData.getLong("profileId", -1L)
        val name = inputData.getString("name") ?: return Result.failure()
        val dosage = inputData.getString("dosage") ?: ""
        val frequency = inputData.getString("frequency") ?: "once-daily"
        val customSchedule = inputData.getString("customSchedule")
        val isActive = inputData.getBoolean("isActive", true)

        if (medicationId == -1L || profileId == -1L || !isActive) {
            return Result.success()
        }

        // Determine the scheduled times based on frequency
        val scheduledTimes = getScheduledTimes(frequency, customSchedule)
        if (scheduledTimes.isEmpty()) return Result.success()

        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()

        // Check if we're within a 7-minute window of any scheduled time
        // (since the worker runs every 15 minutes, a ±7 min window ensures
        // we catch the dose exactly once)
        val windowMinutes = 7L
        val isDue = scheduledTimes.any { scheduled ->
            val diffMinutes = java.time.Duration.between(scheduled, currentTime).toMinutes()
            diffMinutes in -windowMinutes..windowMinutes
        }

        if (!isDue) return Result.success()

        // Check if already logged today for this medication
        val app = applicationContext as? MedLogApplication ?: return Result.failure()
        val today = LocalDate.now().toString()

        // We do a synchronous DB check on the worker thread (IO is fine here)
        // Use runBlocking since we're already on a background thread
        val alreadyLogged = runBlocking {
            try {
                app.database.medicationLogDao()
                    .getByMedication(medicationId)
                    .first()
                    .any { it.takenAt.startsWith(today) }
            } catch (e: Exception) {
                false
            }
        }

        if (alreadyLogged) return Result.success()

        // Fire the notification broadcast
        val intent = Intent(applicationContext, MedicationReminderReceiver::class.java).apply {
            putExtra("medicationId", medicationId)
            putExtra("profileId", profileId)
            putExtra("name", name)
            putExtra("dosage", dosage)
        }
        applicationContext.sendBroadcast(intent)

        return Result.success()
    }

    /**
     * Returns the LocalTime(s) at which a dose should be taken based on frequency.
     */
    private fun getScheduledTimes(frequency: String, customSchedule: String?): List<LocalTime> {
        return when (frequency) {
            "once-daily" -> listOf(LocalTime.of(8, 0))
            "twice-daily" -> listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))
            "three-daily" -> listOf(LocalTime.of(8, 0), LocalTime.of(14, 0), LocalTime.of(20, 0))
            "weekly" -> {
                // For weekly, use Monday 8:00 AM as a default check point
                val today = LocalDate.now()
                val dayOfWeek = today.dayOfWeek.value // 1=Mon, 7=Sun
                if (dayOfWeek == 1) listOf(LocalTime.of(8, 0)) else emptyList()
            }
            "as-needed" -> emptyList() // No automatic reminders for as-needed
            "custom" -> {
                // Parse customSchedule: expected format "08:00,14:00,20:00"
                if (customSchedule.isNullOrBlank()) return emptyList()
                customSchedule.split(",").mapNotNull { timeStr ->
                    try {
                        val parts = timeStr.trim().split(":")
                        LocalTime.of(parts[0].toInt(), parts[1].toInt())
                    } catch (e: Exception) { null }
                }
            }
            else -> emptyList()
        }
    }
}