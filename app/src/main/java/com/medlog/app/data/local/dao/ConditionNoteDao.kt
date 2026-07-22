package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.ConditionNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionNoteDao {

    @Query("SELECT * FROM condition_notes WHERE conditionId = :conditionId ORDER BY date DESC, createdAt DESC")
    fun getByCondition(conditionId: Long): Flow<List<ConditionNoteEntity>>

    @Query("SELECT * FROM condition_notes WHERE id = :id")
    fun getById(id: Long): Flow<ConditionNoteEntity?>

    @Insert
    suspend fun insert(note: ConditionNoteEntity): Long

    @Update
    suspend fun update(note: ConditionNoteEntity)

    @Query("DELETE FROM condition_notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
