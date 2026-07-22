package com.medlog.app.data.local.converter

import androidx.room.TypeConverter

/**
 * Room TypeConverters for the MedLog database.
 *
 * Design decision: all temporal fields (dates, datetimes, timestamps) across every
 * entity are stored as [String] in ISO-8601 format (e.g. "2025-01-15", "2025-01-15T09:30:00Z").
 * This keeps the database schema simple, avoids platform-specific date serialization
 * quirks, and makes the raw data human-readable when inspecting the database directly.
 *
 * If a future migration introduces fields of type [java.time.LocalDateTime],
 * [java.time.LocalDate], or similar, the corresponding `@TypeConverter` methods
 * should be added here.
 */
class Converters {

    // No temporal converters needed — all date/datetime values are stored as String.

    // --- Example converters for future use ----------------------------------------
    //
    // @TypeConverter
    // fun fromLocalDateTime(value: LocalDateTime?): String? =
    //     value?.toString()
    //
    // @TypeConverter
    // fun toLocalDateTime(value: String?): LocalDateTime? =
    //     value?.let { LocalDateTime.parse(it) }
    //
    // @TypeConverter
    // fun fromLocalDate(value: LocalDate?): String? =
    //     value?.toString()
    //
    // @TypeConverter
    // fun toLocalDate(value: String?): LocalDate? =
    //     value?.let { LocalDate.parse(it) }
    // ---------------------------------------------------------------------------
}