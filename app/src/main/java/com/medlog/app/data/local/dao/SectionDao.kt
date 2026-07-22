package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {

    @Query("SELECT * FROM sections WHERE profileId = :profileId")
    fun getByProfile(profileId: Long): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE id = :id")
    fun getById(id: Long): Flow<SectionEntity?>

    @Insert
    suspend fun insert(section: SectionEntity): Long

    @Update
    suspend fun update(section: SectionEntity)

    @Query("DELETE FROM sections WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM sections WHERE profileId = :profileId ORDER BY sortOrder ASC")
    fun getAllOrdered(profileId: Long): Flow<List<SectionEntity>>
}
