package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.MedicationLogDao
import com.medlog.app.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

class MedicationLogRepository(private val dao: MedicationLogDao) {

    fun getByMedication(medicationId: Long): Flow<List<MedicationLogEntity>> =
        dao.getByMedication(medicationId)

    fun getByProfile(profileId: Long): Flow<List<MedicationLogEntity>> =
        dao.getByProfile(profileId)

    fun getByDateRange(profileId: Long, startDate: String, endDate: String): Flow<List<MedicationLogEntity>> =
        dao.getByDateRange(profileId, startDate, endDate)

    fun getTodayLogs(profileId: Long, today: String): Flow<List<MedicationLogEntity>> =
        dao.getTodayLogs(profileId, today)

    fun getAdherenceLast7Days(profileId: Long, weekAgo: String): Flow<List<MedicationLogEntity>> =
        dao.getAdherenceLast7Days(profileId, weekAgo)

    suspend fun insert(log: MedicationLogEntity): Long = dao.insert(log)

    suspend fun insertAll(logs: List<MedicationLogEntity>) = dao.insertAll(logs)

    suspend fun update(log: MedicationLogEntity) = dao.update(log)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteByMedication(medicationId: Long) = dao.deleteByMedication(medicationId)
}
