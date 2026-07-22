package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.SectionEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionEntryDao {

    @Query("SELECT * FROM section_entries WHERE sectionId = :sectionId ORDER BY sortOrder ASC, createdAt DESC")
    fun getBySection(sectionId: Long): Flow<List<SectionEntryEntity>>

    @Query("SELECT * FROM section_entries WHERE id = :id")
    fun getById(id: Long): Flow<SectionEntryEntity?>

    @Insert
    suspend fun insert(entry: SectionEntryEntity): Long

    @Update
    suspend fun update(entry: SectionEntryEntity)

    @Query("DELETE FROM section_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM section_entries WHERE sectionId = :sectionId")
    suspend fun deleteBySection(sectionId: Long)
}
