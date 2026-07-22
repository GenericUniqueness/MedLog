package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.MedicationChangeDao
import com.medlog.app.data.local.dao.MedicationDao
import com.medlog.app.data.local.dao.MedicationLogDao
import com.medlog.app.data.local.entity.MedicationChangeEntity
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val medicationLogDao: MedicationLogDao,
    private val medicationChangeDao: MedicationChangeDao,
) {

    // ── Medication CRUD ──────────────────────────────────────────────

    fun getAll(profileId: Long): Flow<List<MedicationEntity>> =
        medicationDao.getByProfile(profileId)

    fun getByProfile(profileId: Long): Flow<List<MedicationEntity>> =
        medicationDao.getByProfile(profileId)

    fun getActive(profileId: Long): Flow<List<MedicationEntity>> =
        medicationDao.getActiveByProfile(profileId)

    fun getActiveByProfile(profileId: Long): Flow<List<MedicationEntity>> =
        medicationDao.getActiveByProfile(profileId)

    fun getById(id: Long): Flow<MedicationEntity?> =
        medicationDao.getById(id)

    suspend fun insert(medication: MedicationEntity): Long {
        val now = Instant.now().toString()
        return medicationDao.insert(medication.copy(createdAt = now, updatedAt = now))
    }

    suspend fun update(medication: MedicationEntity) {
        medicationDao.update(medication.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun delete(id: Long) = medicationDao.deleteById(id)

    suspend fun deleteById(id: Long) = medicationDao.deleteById(id)

    fun searchByName(profileId: Long, query: String): Flow<List<MedicationEntity>> =
        medicationDao.searchByName(profileId, query)

    // ── Medication Log ───────────────────────────────────────────────

    fun getLogsByMedication(medicationId: Long): Flow<List<MedicationLogEntity>> =
        medicationLogDao.getByMedication(medicationId)

    fun getLogsByProfile(profileId: Long): Flow<List<MedicationLogEntity>> =
        medicationLogDao.getByProfile(profileId)

    fun getTodayLogs(profileId: Long): Flow<List<MedicationLogEntity>> {
        val today = java.time.LocalDate.now().toString()
        return medicationLogDao.getTodayLogs(profileId, today)
    }

    fun getAdherenceLast7Days(profileId: Long): Flow<List<MedicationLogEntity>> {
        val weekAgo = java.time.LocalDate.now().minusDays(7).toString()
        return medicationLogDao.getAdherenceLast7Days(profileId, weekAgo)
    }

    suspend fun logMedication(log: MedicationLogEntity): Long {
        val now = Instant.now().toString()
        return medicationLogDao.insert(log.copy(createdAt = now))
    }

    suspend fun logBulk(logs: List<MedicationLogEntity>) {
        val now = Instant.now().toString()
        medicationLogDao.insertAll(logs.map { it.copy(createdAt = now) })
    }

    suspend fun updateLog(log: MedicationLogEntity) {
        medicationLogDao.update(log)
    }

    suspend fun deleteLog(id: Long) = medicationLogDao.deleteById(id)

    // ── Medication Changes ───────────────────────────────────────────

    fun getChanges(medicationId: Long): Flow<List<MedicationChangeEntity>> =
        medicationChangeDao.getByMedication(medicationId)

    suspend fun addChange(change: MedicationChangeEntity): Long {
        val now = Instant.now().toString()
        return medicationChangeDao.insert(change.copy(createdAt = now))
    }

    suspend fun updateChange(change: MedicationChangeEntity) {
        medicationChangeDao.update(change)
    }

    suspend fun deleteChange(id: Long) = medicationChangeDao.deleteById(id)
}