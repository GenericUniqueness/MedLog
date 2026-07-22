package com.medlog.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Date/time formatting utilities. All dates in the database are stored
 * as ISO-8601 strings; these helpers parse and format them for display.
 */
object DateUtils {

    private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val isoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val displayDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
    private val displayTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val displayShortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

    fun todayIso(): String = LocalDate.now().format(isoDateFormatter)
    fun nowIso(): String = LocalDateTime.now().format(isoDateTimeFormatter)

    /** Parse "2025-01-15" → "Jan 15, 2025" */
    fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            LocalDate.parse(isoDate, isoDateFormatter).format(displayDateFormatter)
        } catch (e: Exception) { isoDate }
    }

    /** Parse "2025-01-15T09:30:00" → "Jan 15, 2025 at 9:30 AM" */
    fun formatDateTime(isoDateTime: String?): String {
        if (isoDateTime.isNullOrBlank()) return ""
        return try {
            LocalDateTime.parse(isoDateTime, isoDateTimeFormatter).format(displayDateTimeFormatter)
        } catch (e: Exception) {
            // Might be a date-only string
            formatDate(isoDateTime)
        }
    }

    /** Parse "09:30" → "9:30 AM" */
    fun formatTime(time: String?): String {
        if (time.isNullOrBlank()) return ""
        return try {
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            String.format("%d:%02d %s",
                if (hour == 0 || hour == 12) 12 else hour % 12,
                minute,
                if (hour < 12) "AM" else "PM"
            )
        } catch (e: Exception) { time }
    }

    /** Parse "2025-01-15" → "Jan 15" */
    fun formatShortDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            LocalDate.parse(isoDate, isoDateFormatter).format(displayShortDateFormatter)
        } catch (e: Exception) { isoDate }
    }

    /** "2025-01-15" → "3 days ago" or relative description */
    fun relativeDate(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, isoDateFormatter)
            val today = LocalDate.now()
            val days = java.time.temporal.ChronoUnit.DAYS.between(date, today)
            when {
                days == 0L -> "Today"
                days == 1L -> "Yesterday"
                days > 1 -> "$days days ago"
                days == -1L -> "Tomorrow"
                days < -1 -> "In ${-days} days"
                else -> formatDate(isoDate)
            }
        } catch (e: Exception) { formatDate(isoDate) }
    }

    /** Format file size in human-readable form */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        }
    }
}

/** Frequency display labels */
object FrequencyLabels {
    private val map = mapOf(
        "once-daily" to "Once daily",
        "twice-daily" to "Twice daily",
        "three-daily" to "Three times daily",
        "weekly" to "Weekly",
        "as-needed" to "As needed",
        "custom" to "Custom schedule"
    )
    fun get(key: String): String = map[key] ?: key
}

/** Route display labels */
object RouteLabels {
    private val map = mapOf(
        "oral" to "Oral",
        "topical" to "Topical",
        "injection" to "Injection",
        "inhaler" to "Inhaler",
        "other" to "Other"
    )
    fun get(key: String): String = map[key] ?: key
}

/** Appointment type display labels */
object AppointmentTypeLabels {
    private val map = mapOf(
        "checkup" to "Check-up",
        "followup" to "Follow-up",
        "procedure" to "Procedure",
        "lab" to "Lab Work",
        "imaging" to "Imaging",
        "other" to "Other"
    )
    fun get(key: String): String = map[key] ?: key
}

/** Gender display labels */
object GenderLabels {
    private val map = mapOf(
        "male" to "Male",
        "female" to "Female",
        "other" to "Other",
        "prefer-not-to-say" to "Prefer not to say"
    )
    fun get(key: String): String = map[key] ?: key
}