package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.ConditionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionDao {

    @Query("SELECT * FROM conditions WHERE profileId = :profileId")
    fun getByProfile(profileId: Long): Flow<List<ConditionEntity>>

    @Query("SELECT * FROM conditions WHERE id = :id")
    fun getById(id: Long): Flow<ConditionEntity?>

    @Query("SELECT COUNT(*) FROM conditions WHERE profileId = :profileId AND status = 'active'")
    fun getActiveCount(profileId: Long): Flow<Int>

    @Insert
    suspend fun insert(condition: ConditionEntity): Long

    @Update
    suspend fun update(condition: ConditionEntity)

    @Query("DELETE FROM conditions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
