package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.SectionEntryDao
import com.medlog.app.data.local.entity.SectionEntryEntity
import kotlinx.coroutines.flow.Flow

class SectionEntryRepository(private val dao: SectionEntryDao) {

    fun getBySection(sectionId: Long): Flow<List<SectionEntryEntity>> = dao.getBySection(sectionId)

    fun getById(id: Long): Flow<SectionEntryEntity?> = dao.getById(id)

    suspend fun insert(entry: SectionEntryEntity): Long = dao.insert(entry)

    suspend fun update(entry: SectionEntryEntity) = dao.update(entry)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteBySection(sectionId: Long) = dao.deleteBySection(sectionId)
}
