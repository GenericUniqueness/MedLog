package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.FileAttachmentDao
import com.medlog.app.data.local.entity.FileAttachmentEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class FileAttachmentRepository(private val fileAttachmentDao: FileAttachmentDao) {

    fun getAll(profileId: Long): Flow<List<FileAttachmentEntity>> =
        fileAttachmentDao.getByProfile(profileId)

    fun getByProfile(profileId: Long): Flow<List<FileAttachmentEntity>> =
        fileAttachmentDao.getByProfile(profileId)

    fun getById(id: Long): Flow<FileAttachmentEntity?> =
        fileAttachmentDao.getById(id)

    fun getByLinked(linkedType: String, linkedId: Long): Flow<List<FileAttachmentEntity>> =
        fileAttachmentDao.getByLinked(linkedType, linkedId)

    fun getByCategory(profileId: Long, category: String): Flow<List<FileAttachmentEntity>> =
        fileAttachmentDao.getByCategory(profileId, category)

    suspend fun insert(attachment: FileAttachmentEntity): Long {
        val now = Instant.now().toString()
        return fileAttachmentDao.insert(attachment.copy(createdAt = now))
    }

    suspend fun deleteById(id: Long) = fileAttachmentDao.deleteById(id)
}