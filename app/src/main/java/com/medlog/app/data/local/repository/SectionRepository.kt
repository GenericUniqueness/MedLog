package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.SectionDao
import com.medlog.app.data.local.dao.SectionEntryDao
import com.medlog.app.data.local.entity.SectionEntity
import com.medlog.app.data.local.entity.SectionEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class SectionRepository(
    private val sectionDao: SectionDao,
    private val sectionEntryDao: SectionEntryDao,
) {

    // ── Section CRUD ─────────────────────────────────────────────────

    fun getAll(profileId: Long): Flow<List<SectionEntity>> =
        sectionDao.getByProfile(profileId)

    fun getAllOrdered(profileId: Long): Flow<List<SectionEntity>> =
        sectionDao.getAllOrdered(profileId)

    fun getById(id: Long): Flow<SectionEntity?> =
        sectionDao.getById(id)

    suspend fun insert(section: SectionEntity): Long {
        val now = Instant.now().toString()
        return sectionDao.insert(section.copy(createdAt = now, updatedAt = now))
    }

    suspend fun update(section: SectionEntity) {
        sectionDao.update(section.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun delete(id: Long) = sectionDao.deleteById(id)

    // ── Section Entries ──────────────────────────────────────────────

    fun getEntries(sectionId: Long): Flow<List<SectionEntryEntity>> =
        sectionEntryDao.getBySection(sectionId)

    fun getEntryById(id: Long): Flow<SectionEntryEntity?> =
        sectionEntryDao.getById(id)

    suspend fun insertEntry(entry: SectionEntryEntity): Long {
        val now = Instant.now().toString()
        return sectionEntryDao.insert(entry.copy(createdAt = now, updatedAt = now))
    }

    suspend fun updateEntry(entry: SectionEntryEntity) {
        sectionEntryDao.update(entry.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun deleteEntry(id: Long) = sectionEntryDao.deleteById(id)

    suspend fun deleteEntriesBySection(sectionId: Long) =
        sectionEntryDao.deleteBySection(sectionId)
}