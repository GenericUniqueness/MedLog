package com.medlog.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medlog.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries WHERE profileId = :profileId ORDER BY date DESC, createdAt DESC")
    fun getByProfile(profileId: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    fun getById(id: Long): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE profileId = :profileId AND date >= :start AND date <= :end ORDER BY date DESC, createdAt DESC")
    fun getByDateRange(profileId: Long, start: String, end: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE profileId = :profileId ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun getRecent(profileId: Long, limit: Int): Flow<List<JournalEntryEntity>>

    @Insert
    suspend fun insert(entry: JournalEntryEntity): Long

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM journal_entries WHERE profileId = :profileId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY date DESC, createdAt DESC")
    fun searchByContent(profileId: Long, query: String): Flow<List<JournalEntryEntity>>
}
