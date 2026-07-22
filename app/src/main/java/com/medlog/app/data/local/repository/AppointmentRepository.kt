package com.medlog.app.data.local.repository

import com.medlog.app.data.local.dao.AppointmentDao
import com.medlog.app.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class AppointmentRepository(private val appointmentDao: AppointmentDao) {

    fun getAll(profileId: Long): Flow<List<AppointmentEntity>> =
        appointmentDao.getByProfile(profileId)

    fun getById(id: Long): Flow<AppointmentEntity?> =
        appointmentDao.getById(id)

    fun getUpcoming(profileId: Long, today: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getUpcoming(profileId, today)

    fun getByDateRange(profileId: Long, start: String, end: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getByDateRange(profileId, start, end)

    suspend fun insert(appointment: AppointmentEntity): Long {
        val now = Instant.now().toString()
        return appointmentDao.insert(appointment.copy(createdAt = now, updatedAt = now))
    }

    suspend fun update(appointment: AppointmentEntity) {
        appointmentDao.update(appointment.copy(updatedAt = Instant.now().toString()))
    }

    suspend fun delete(id: Long) = appointmentDao.deleteById(id)

    fun searchByTitle(profileId: Long, query: String): Flow<List<AppointmentEntity>> =
        appointmentDao.searchByTitle(profileId, query)
}