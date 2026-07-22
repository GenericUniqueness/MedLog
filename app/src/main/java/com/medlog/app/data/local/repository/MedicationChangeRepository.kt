package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.MedicationChangeDao
import com.medlog.app.data.local.entity.MedicationChangeEntity
import kotlinx.coroutines.flow.Flow

class MedicationChangeRepository(private val dao: MedicationChangeDao) {

    fun getByMedication(medicationId: Long): Flow<List<MedicationChangeEntity>> =
        dao.getByMedication(medicationId)

    fun getByProfile(profileId: Long): Flow<List<MedicationChangeEntity>> =
        dao.getByProfile(profileId)

    suspend fun insert(change: MedicationChangeEntity): Long = dao.insert(change)

    suspend fun update(change: MedicationChangeEntity) = dao.update(change)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
