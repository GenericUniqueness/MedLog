package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.AppSettingDao
import com.medlog.app.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class AppSettingRepository(private val appSettingDao: AppSettingDao) {

    fun getAll(profileId: Long): Flow<List<AppSettingEntity>> =
        appSettingDao.getByProfile(profileId)

    fun get(profileId: Long, key: String): Flow<AppSettingEntity?> =
        appSettingDao.get(profileId, key)

    suspend fun upsert(setting: AppSettingEntity) {
        val now = Instant.now().toString()
        appSettingDao.upsert(setting.copy(createdAt = now, updatedAt = now))
    }

    suspend fun delete(profileId: Long, key: String) =
        appSettingDao.delete(profileId, key)
}