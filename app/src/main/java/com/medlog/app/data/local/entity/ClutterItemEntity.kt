package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Scratch-pad items. Temporary or uncategorized notes the user
 * wants to keep but hasn't filed elsewhere yet.
 */
@Entity(
    tableName = "clutter_items",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId")]
)
data class ClutterItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val content: String,
    val category: String? = null,
    val isPinned: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)