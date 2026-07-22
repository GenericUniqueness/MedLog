package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_changes",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId"), Index("profileId")]
)
data class MedicationChangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val profileId: Long,
    val changedAt: String,                 // ISO date
    val changeType: String,                // "dosage"|"frequency"|"added"|"discontinued"|"replaced"
    val oldValue: String? = null,
    val newValue: String? = null,
    val reason: String? = null,
    val doctor: String? = null,
    val createdAt: String = ""
)