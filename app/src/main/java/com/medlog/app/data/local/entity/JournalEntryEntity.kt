package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_entries",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId"), Index("date")]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String? = null,
    val content: String,
    val date: String,                      // ISO date
    val mood: String? = null,              // "great"|"good"|"okay"|"bad"|"terrible"
    val tags: String? = null,              // Comma-separated
    val createdAt: String = "",
    val updatedAt: String = ""
)