package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.MedicationChangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationChangeDao {

    @Query("SELECT * FROM medication_changes WHERE medicationId = :medicationId ORDER BY changedAt DESC")
    fun getByMedication(medicationId: Long): Flow<List<MedicationChangeEntity>>

    @Query("SELECT * FROM medication_changes WHERE profileId = :profileId ORDER BY changedAt DESC")
    fun getByProfile(profileId: Long): Flow<List<MedicationChangeEntity>>

    @Insert
    suspend fun insert(change: MedicationChangeEntity): Long

    @Update
    suspend fun update(change: MedicationChangeEntity)

    @Query("DELETE FROM medication_changes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
