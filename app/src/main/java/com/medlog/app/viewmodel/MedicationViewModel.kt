package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import com.medlog.app.data.local.repository.MedicationLogRepository
import com.medlog.app.data.local.repository.MedicationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicationViewModel(
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository
) : ViewModel() {

    private val _medications = MutableStateFlow<List<MedicationEntity>>(emptyList())
    val medications: StateFlow<List<MedicationEntity>> = _medications.asStateFlow()

    private val _selectedMedication = MutableStateFlow<MedicationEntity?>(null)
    val selectedMedication: StateFlow<MedicationEntity?> = _selectedMedication.asStateFlow()

    private val _logsForSelected = MutableStateFlow<List<MedicationLogEntity>>(emptyList())
    val logsForSelected: StateFlow<List<MedicationLogEntity>> = _logsForSelected.asStateFlow()

    private val _todayLogs = MutableStateFlow<List<MedicationLogEntity>>(emptyList())
    val todayLogs: StateFlow<List<MedicationLogEntity>> = _todayLogs.asStateFlow()

    private val _currentProfileId = MutableStateFlow(0L)

    private var logsCollectionJob: Job? = null
    private var todayLogsCollectionJob: Job? = null

    fun loadForProfile(profileId: Long) {
        _currentProfileId.value = profileId
        viewModelScope.launch {
            medicationRepository.getByProfile(profileId).collect { _medications.value = it }
        }
        loadTodayLogs(profileId)
    }

    fun selectMedication(medication: MedicationEntity?) {
        _selectedMedication.value = medication
        loadLogsForMedication(medication?.id ?: 0L)
    }

    private fun loadLogsForMedication(medicationId: Long) {
        logsCollectionJob?.cancel()
        if (medicationId == 0L) {
            _logsForSelected.value = emptyList()
            return
        }
        logsCollectionJob = viewModelScope.launch {
            medicationLogRepository.getByMedication(medicationId).collect { _logsForSelected.value = it }
        }
    }

    private fun loadTodayLogs(profileId: Long) {
        todayLogsCollectionJob?.cancel()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        todayLogsCollectionJob = viewModelScope.launch {
            medicationLogRepository.getTodayLogs(profileId, today).collect { _todayLogs.value = it }
        }
    }

    fun createMedication(
        profileId: Long,
        name: String,
        dosage: String,
        frequency: String,
        customSchedule: String?,
        route: String,
        prescriber: String?,
        startDate: String?,
        endDate: String?,
        notes: String?,
        color: String
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val medication = MedicationEntity(
                profileId = profileId,
                name = name,
                dosage = dosage,
                frequency = frequency,
                customSchedule = customSchedule,
                route = route,
                prescriber = prescriber,
                startDate = startDate,
                endDate = endDate,
                notes = notes,
                color = color,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            medicationRepository.insert(medication)
        }
    }

    fun updateMedication(
        id: Long,
        name: String,
        dosage: String,
        frequency: String,
        customSchedule: String?,
        route: String,
        prescriber: String?,
        startDate: String?,
        endDate: String?,
        notes: String?,
        color: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            val existing = _medications.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                name = name,
                dosage = dosage,
                frequency = frequency,
                customSchedule = customSchedule,
                route = route,
                prescriber = prescriber,
                startDate = startDate,
                endDate = endDate,
                notes = notes,
                color = color,
                isActive = isActive,
                updatedAt = now
            )
            medicationRepository.update(updated)
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            medicationRepository.deleteById(id)
        }
    }

    fun logMedication(
        medicationId: Long,
        profileId: Long,
        status: String,
        dose: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val log = MedicationLogEntity(
                medicationId = medicationId,
                profileId = profileId,
                takenAt = now,
                dose = dose,
                status = status,
                notes = notes,
                createdAt = now
            )
            medicationLogRepository.insert(log)
        }
    }

    fun getAdherence(profileId: Long): StateFlow<Int> {
        val adherenceFlow = MutableStateFlow(0)

        viewModelScope.launch {
            val weekAgo = LocalDate.now()
                .minusDays(7)
                .format(DateTimeFormatter.ISO_LOCAL_DATE)

            val activeMedications = medicationRepository.getActiveByProfile(profileId)
            val weekLogs = medicationLogRepository.getAdherenceLast7Days(profileId, weekAgo)

            combine(activeMedications, weekLogs) { medications, logs ->
                var expectedTotal = 0
                var takenTotal = 0

                for (med in medications) {
                    val dailyDoses = when (med.frequency) {
                        "once-daily" -> 1
                        "twice-daily" -> 2
                        "three-daily" -> 3
                        "weekly" -> 1.0 / 7.0
                        "as-needed" -> continue  // skip as-needed from adherence calc
                        "custom" -> 1 // default assumption for custom
                        else -> 1
                    }
                    expectedTotal += (dailyDoses * 7).toInt()
                }

                for (log in logs) {
                    if (log.status == "taken" || log.status == "late") {
                        takenTotal++
                    }
                }

                if (expectedTotal > 0) {
                    (takenTotal * 100) / expectedTotal
                } else {
                    100 // no expected doses means 100% adherence
                }
            }.collect { adherenceFlow.value = it }
        }

        return adherenceFlow
    }
}
