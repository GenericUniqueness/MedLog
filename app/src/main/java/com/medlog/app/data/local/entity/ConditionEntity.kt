package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conditions",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId")]
)
data class ConditionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val severity: String = "moderate",     // "mild" | "moderate" | "severe"
    val status: String = "active",         // "active" | "managed" | "resolved"
    val diagnosedAt: String? = null,       // ISO date
    val doctor: String? = null,
    val notes: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)