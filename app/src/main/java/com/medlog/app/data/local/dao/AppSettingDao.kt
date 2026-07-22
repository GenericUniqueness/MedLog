package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medlog.app.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingDao {

    @Query("SELECT * FROM app_settings WHERE profileId = :profileId")
    fun getByProfile(profileId: Long): Flow<List<AppSettingEntity>>

    @Query("SELECT * FROM app_settings WHERE profileId = :profileId AND `key` = :key")
    fun get(profileId: Long, key: String): Flow<AppSettingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: AppSettingEntity)

    @Query("DELETE FROM app_settings WHERE profileId = :profileId AND `key` = :key")
    suspend fun delete(profileId: Long, key: String)
}
