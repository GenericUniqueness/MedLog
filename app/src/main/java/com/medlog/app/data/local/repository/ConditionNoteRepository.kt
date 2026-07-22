package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.ConditionNoteDao
import com.medlog.app.data.local.entity.ConditionNoteEntity
import kotlinx.coroutines.flow.Flow

class ConditionNoteRepository(private val dao: ConditionNoteDao) {

    fun getByCondition(conditionId: Long): Flow<List<ConditionNoteEntity>> =
        dao.getByCondition(conditionId)

    fun getById(id: Long): Flow<ConditionNoteEntity?> = dao.getById(id)

    suspend fun insert(note: ConditionNoteEntity): Long = dao.insert(note)

    suspend fun update(note: ConditionNoteEntity) = dao.update(note)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
