package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications WHERE profileId = :profileId")
    fun getByProfile(profileId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE profileId = :profileId AND isActive = 1")
    fun getActiveByProfile(profileId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getById(id: Long): Flow<MedicationEntity?>

    @Query("SELECT COUNT(*) FROM medications WHERE profileId = :profileId AND isActive = 1")
    fun getActiveCount(profileId: Long): Flow<Int>

    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM medications WHERE profileId = :profileId AND name LIKE '%' || :query || '%'")
    fun searchByName(profileId: Long, query: String): Flow<List<MedicationEntity>>
}
