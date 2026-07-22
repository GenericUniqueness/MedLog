package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.JournalDao
import com.medlog.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class JournalRepository(private val journalDao: JournalDao) {

    fun getAll(profileId: Long): Flow<List<JournalEntryEntity>> =
        journalDao.getByProfile(profileId)

    fun getByProfile(profileId: Long): Flow<List<JournalEntryEntity>> =
        journalDao.getByProfile(profileId)

    fun getById(id: Long): Flow<JournalEntryEntity?> =
        journalDao.getById(id)

    fun getByDateRange(profileId: Long, start: String, end: String): Flow<List<JournalEntryEntity>> =
        journalDao.getByDateRange(profileId, start, end)

    fun getRecent(profileId: Long, limit: Int): Flow<List<JournalEntryEntity>> =
        journalDao.getRecent(profileId, limit)

    suspend fun insert(entry: JournalEntryEntity): Long {
        val now = Instant.now().toString()
        return journalDao.insert(entry.copy(createdAt = now, updatedAt = now))
    }

    suspend fun update(entry: JournalEntryEntity) {
        journalDao.update(entry.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun deleteById(id: Long) = journalDao.deleteById(id)

    fun searchByContent(profileId: Long, query: String): Flow<List<JournalEntryEntity>> =
        journalDao.searchByContent(profileId, query)
}