package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Patient profile. All other data is scoped to a profile via profileId.
 * Switching profiles switches ALL visible data.
 */
@Entity(
    tableName = "profiles",
    indices = []
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateOfBirth: String? = null,       // ISO-8601 date "YYYY-MM-DD"
    val gender: String? = null,            // "male" | "female" | "other" | "prefer-not-to-say"
    val bloodType: String? = null,         // "A+" | "A-" | "B+" | "B-" | "AB+" | "AB-" | "O+" | "O-"
    val allergies: String? = null,         // Free-text, comma-separated
    val emergencyContact: String? = null,
    val notes: String? = null,
    val color: String = "#006B5E",         // Hex accent color for profile
    val isActive: Boolean = true,
    val createdAt: String = "",            // ISO-8601 datetime
    val updatedAt: String = ""
)