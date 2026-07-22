package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId"), Index("date")]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String,
    val doctor: String? = null,
    val location: String? = null,
    val date: String,                      // ISO date
    val time: String? = null,              // "HH:MM"
    val duration: Int? = null,             // minutes
    val type: String = "checkup",          // "checkup"|"followup"|"procedure"|"lab"|"imaging"|"other"
    val status: String = "upcoming",       // "upcoming"|"completed"|"cancelled"|"missed"
    val notes: String? = null,
    val reminderSet: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)