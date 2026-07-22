package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.ClutterDao
import com.medlog.app.data.local.entity.ClutterItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class ClutterRepository(private val clutterDao: ClutterDao) {

    fun getAll(profileId: Long): Flow<List<ClutterItemEntity>> =
        clutterDao.getByProfile(profileId)

    fun getByProfile(profileId: Long): Flow<List<ClutterItemEntity>> =
        clutterDao.getByProfile(profileId)

    fun getPinned(profileId: Long): Flow<List<ClutterItemEntity>> =
        clutterDao.getPinned(profileId)

    fun getById(id: Long): Flow<ClutterItemEntity?> =
        clutterDao.getById(id)

    suspend fun insert(item: ClutterItemEntity): Long {
        val now = Instant.now().toString()
        return clutterDao.insert(item.copy(createdAt = now, updatedAt = now))
    }

    suspend fun update(item: ClutterItemEntity) {
        clutterDao.update(item.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun deleteById(id: Long) = clutterDao.deleteById(id)
}