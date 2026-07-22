package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.medlog.app.data.local.entity.FileAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileAttachmentDao {

    @Query("SELECT * FROM file_attachments WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getByProfile(profileId: Long): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE id = :id")
    fun getById(id: Long): Flow<FileAttachmentEntity?>

    @Query("SELECT * FROM file_attachments WHERE linkedType = :linkedType AND linkedId = :linkedId ORDER BY createdAt DESC")
    fun getByLinked(linkedType: String, linkedId: Long): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE profileId = :profileId AND category = :category ORDER BY createdAt DESC")
    fun getByCategory(profileId: Long, category: String): Flow<List<FileAttachmentEntity>>

    @Insert
    suspend fun insert(attachment: FileAttachmentEntity): Long

    @Query("DELETE FROM file_attachments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
