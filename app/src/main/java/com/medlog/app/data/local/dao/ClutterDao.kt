package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.ClutterItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClutterDao {

    @Query("SELECT * FROM clutter_items WHERE profileId = :profileId ORDER BY isPinned DESC, updatedAt DESC")
    fun getByProfile(profileId: Long): Flow<List<ClutterItemEntity>>

    @Query("SELECT * FROM clutter_items WHERE profileId = :profileId AND isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinned(profileId: Long): Flow<List<ClutterItemEntity>>

    @Query("SELECT * FROM clutter_items WHERE id = :id")
    fun getById(id: Long): Flow<ClutterItemEntity?>

    @Insert
    suspend fun insert(item: ClutterItemEntity): Long

    @Update
    suspend fun update(item: ClutterItemEntity)

    @Query("DELETE FROM clutter_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
