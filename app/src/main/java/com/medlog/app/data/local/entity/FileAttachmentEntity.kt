package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "file_attachments",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["profileId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("profileId"), Index("linkedType", "linkedId")]
)
data class FileAttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val fileName: String,
    val fileType: String,                  // MIME type
    val fileSize: Long,                    // bytes
    val filePath: String,                  // Internal storage path
    val category: String? = null,          // "lab-result"|"prescription"|"imaging"|"other"
    val description: String? = null,
    val linkedType: String? = null,        // "condition"|"medication"|"appointment"
    val linkedId: Long? = null,
    val createdAt: String = ""
)