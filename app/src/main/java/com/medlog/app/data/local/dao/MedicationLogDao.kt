package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY takenAt DESC")
    fun getByMedication(medicationId: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE profileId = :profileId ORDER BY takenAt DESC")
    fun getByProfile(profileId: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE profileId = :profileId AND takenAt >= :startDate AND takenAt <= :endDate ORDER BY takenAt DESC")
    fun getByDateRange(profileId: Long, startDate: String, endDate: String): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE profileId = :profileId AND takenAt LIKE :today || '%' ORDER BY takenAt DESC")
    fun getTodayLogs(profileId: Long, today: String): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE profileId = :profileId AND takenAt >= :weekAgo ORDER BY takenAt DESC")
    fun getAdherenceLast7Days(profileId: Long, weekAgo: String): Flow<List<MedicationLogEntity>>

    @Insert
    suspend fun insert(log: MedicationLogEntity): Long

    @Insert
    suspend fun insertAll(logs: List<MedicationLogEntity>)

    @Update
    suspend fun update(log: MedicationLogEntity)

    @Query("DELETE FROM medication_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM medication_logs WHERE medicationId = :medicationId")
    suspend fun deleteByMedication(medicationId: Long)
}
