package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments WHERE profileId = :profileId ORDER BY date ASC, time ASC")
    fun getByProfile(profileId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    fun getById(id: Long): Flow<AppointmentEntity?>

    @Query("SELECT * FROM appointments WHERE profileId = :profileId AND date >= :today AND status = 'upcoming' ORDER BY date ASC, time ASC")
    fun getUpcoming(profileId: Long, today: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE profileId = :profileId AND date >= :start AND date <= :end ORDER BY date ASC, time ASC")
    fun getByDateRange(profileId: Long, start: String, end: String): Flow<List<AppointmentEntity>>

    @Insert
    suspend fun insert(appointment: AppointmentEntity): Long

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM appointments WHERE profileId = :profileId AND title LIKE '%' || :query || '%'")
    fun searchByTitle(profileId: Long, query: String): Flow<List<AppointmentEntity>>
}
