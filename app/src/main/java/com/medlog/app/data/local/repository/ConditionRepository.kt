package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.ConditionDao
import com.medlog.app.data.local.dao.ConditionNoteDao
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.data.local.entity.ConditionNoteEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class ConditionRepository(
    private val conditionDao: ConditionDao,
    private val conditionNoteDao: ConditionNoteDao,
) {

    // ── Condition CRUD ───────────────────────────────────────────────

    fun getAll(profileId: Long): Flow<List<ConditionEntity>> =
        conditionDao.getByProfile(profileId)

    fun getById(id: Long): Flow<ConditionEntity?> =
        conditionDao.getById(id)

    fun getActiveCount(profileId: Long): Flow<Int> =
        conditionDao.getActiveCount(profileId)

    suspend fun insert(condition: ConditionEntity): Long {
        val now = Instant.now().toString()
        return conditionDao.insert(condition.copy(createdAt = now, updatedAt = now))
    }

    suspend fun update(condition: ConditionEntity) {
        conditionDao.update(condition.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun delete(id: Long) = conditionDao.deleteById(id)

    // ── Condition Notes ──────────────────────────────────────────────

    fun getNotes(conditionId: Long): Flow<List<ConditionNoteEntity>> =
        conditionNoteDao.getByCondition(conditionId)

    fun getNoteById(id: Long): Flow<ConditionNoteEntity?> =
        conditionNoteDao.getById(id)

    suspend fun insertNote(note: ConditionNoteEntity): Long {
        val now = Instant.now().toString()
        return conditionNoteDao.insert(note.copy(createdAt = now, updatedAt = now))
    }

    suspend fun updateNote(note: ConditionNoteEntity) {
        conditionNoteDao.update(note.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun deleteNote(id: Long) = conditionNoteDao.deleteById(id)
}