package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "condition_notes",
    foreignKeys = [ForeignKey(
        entity = ConditionEntity::class,
        parentColumns = ["id"],
        childColumns = ["conditionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("conditionId")]
)
data class ConditionNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conditionId: Long,
    val date: String,                      // ISO date
    val content: String,
    val mood: String? = null,              // "good" | "okay" | "bad" | "crisis"
    val painLevel: Int? = null,            // 0-10
    val createdAt: String = "",
    val updatedAt: String = ""
)