package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId")]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val dosage: String,                    // e.g. "500mg"
    val frequency: String = "once-daily",  // "once-daily"|"twice-daily"|"three-daily"|"weekly"|"as-needed"|"custom"
    val customSchedule: String? = null,
    val route: String = "oral",            // "oral"|"topical"|"injection"|"inhaler"|"other"
    val prescriber: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,           // null = ongoing
    val notes: String? = null,
    val color: String = "#006B5E",
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)