package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.ProfileDao
import com.medlog.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {

    fun getAll(): Flow<List<ProfileEntity>> = profileDao.getAll()

    fun getActive(): Flow<ProfileEntity?> = profileDao.getActive()

    fun getById(id: Long): Flow<ProfileEntity?> = profileDao.getById(id)

    fun count(): Flow<Int> = profileDao.count()

    suspend fun insert(profile: ProfileEntity): Long {
        val now = java.time.Instant.now().toString()
        return profileDao.insert(profile.copy(createdAt = now, updatedAt = now))
    }

    suspend fun update(profile: ProfileEntity) {
        profileDao.update(profile.copy(updatedAt = java.time.Instant.now().toString()))
    }

    suspend fun setActive(id: Long) = profileDao.setActive(id)

    suspend fun deleteById(id: Long) = profileDao.deleteById(id)
}