package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Per-profile key-value settings. Stored as strings; parsed by the UI layer.
 * Example keys: "notification_enabled", "theme_mode", "reminder_lead_minutes".
 */
@Entity(
    tableName = "app_settings",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId")],
    primaryKeys = ["profileId", "key"]
)
data class AppSettingEntity(
    val profileId: Long,
    val key: String,
    val value: String,
    val createdAt: String = "",
    val updatedAt: String = ""
)