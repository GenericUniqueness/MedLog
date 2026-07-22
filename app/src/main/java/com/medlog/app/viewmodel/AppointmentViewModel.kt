package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.data.local.repository.AppointmentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppointmentViewModel(private val repository: AppointmentRepository) : ViewModel() {

    private val _appointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val appointments: StateFlow<List<AppointmentEntity>> = _appointments.asStateFlow()

    private val _selectedAppointment = MutableStateFlow<AppointmentEntity?>(null)
    val selectedAppointment: StateFlow<AppointmentEntity?> = _selectedAppointment.asStateFlow()

    private val _upcomingAppointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val upcomingAppointments: StateFlow<List<AppointmentEntity>> = _upcomingAppointments.asStateFlow()

    private var upcomingCollectionJob: Job? = null

    fun loadForProfile(profileId: Long) {
        viewModelScope.launch {
            repository.getByProfile(profileId).collect { _appointments.value = it }
        }
        loadUpcoming(profileId)
    }

    private fun loadUpcoming(profileId: Long) {
        upcomingCollectionJob?.cancel()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        upcomingCollectionJob = viewModelScope.launch {
            repository.getUpcoming(profileId, today).collect { _upcomingAppointments.value = it }
        }
    }

    fun selectAppointment(appointment: AppointmentEntity?) {
        _selectedAppointment.value = appointment
    }

    fun createAppointment(
        profileId: Long,
        title: String,
        doctor: String?,
        location: String?,
        date: String,
        time: String?,
        duration: Int?,
        type: String,
        notes: String?,
        reminderSet: Boolean
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val appointment = AppointmentEntity(
                profileId = profileId,
                title = title,
                doctor = doctor,
                location = location,
                date = date,
                time = time,
                duration = duration,
                type = type,
                status = "upcoming",
                notes = notes,
                reminderSet = reminderSet,
                createdAt = now,
                updatedAt = now
            )
            repository.insert(appointment)
        }
    }

    fun updateAppointment(
        id: Long,
        title: String,
        doctor: String?,
        location: String?,
        date: String,
        time: String?,
        duration: Int?,
        type: String,
        notes: String?,
        reminderSet: Boolean
    ) {
        viewModelScope.launch {
            val existing = _appointments.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                title = title,
                doctor = doctor,
                location = location,
                date = date,
                time = time,
                duration = duration,
                type = type,
                notes = notes,
                reminderSet = reminderSet,
                updatedAt = now
            )
            repository.update(updated)
        }
    }

    fun deleteAppointment(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun completeAppointment(id: Long) {
        viewModelScope.launch {
            val existing = _appointments.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                status = "completed",
                updatedAt = now
            )
            repository.update(updated)
        }
    }

    fun cancelAppointment(id: Long) {
        viewModelScope.launch {
            val existing = _appointments.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                status = "cancelled",
                updatedAt = now
            )
            repository.update(updated)
        }
    }
}
