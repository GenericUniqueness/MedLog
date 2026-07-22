package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.data.local.entity.JournalEntryEntity
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import com.medlog.app.data.local.repository.AppointmentRepository
import com.medlog.app.data.local.repository.ConditionRepository
import com.medlog.app.data.local.repository.JournalRepository
import com.medlog.app.data.local.repository.MedicationLogRepository
import com.medlog.app.data.local.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TodayMedicationInfo(
    val medication: MedicationEntity,
    val hasBeenLoggedToday: Boolean = false,
    val lastLogStatus: String? = null
)

data class DashboardState(
    val todayMedications: List<TodayMedicationInfo> = emptyList(),
    val upcomingAppointments: List<AppointmentEntity> = emptyList(),
    val recentJournal: List<JournalEntryEntity> = emptyList(),
    val activeConditionsCount: Int = 0,
    val medicationAdherence: Int = 0,
    val totalMedications: Int = 0,
    val upcomingAppointmentsCount: Int = 0,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val conditionRepository: ConditionRepository,
    private val appointmentRepository: AppointmentRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    fun loadDashboard(profileId: Long) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val sevenDaysAgo = LocalDate.now()
                    .minusDays(7)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
                val weekLater = LocalDate.now()
                    .plusDays(7)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)

                // Collect all reactive flows and merge into dashboard state.
                // Kotlin's combine() supports max 5 flows, so we nest two combines.
                val medicationsFlow = medicationRepository.getActive(profileId)
                val todayLogsFlow = medicationLogRepository.getTodayLogs(profileId, today)
                val weekLogsFlow = medicationLogRepository.getAdherenceLast7Days(profileId, sevenDaysAgo)
                val upcomingApptsFlow = appointmentRepository.getUpcoming(profileId, today)
                val weekApptsFlow = appointmentRepository.getByDateRange(profileId, today, weekLater)

                // Inner combine: journals + conditions + totalMeds (the simpler ones)
                val recentJournals = journalRepository.getRecent(profileId, 5)
                val activeConditions = conditionRepository.getActiveCount(profileId)
                // totalMedications: use a simple count from the medications list
                // (we'll derive it from the medications flow directly)

                combine(
                    medicationsFlow,
                    todayLogsFlow,
                    weekLogsFlow,
                    upcomingApptsFlow,
                    weekApptsFlow
                ) { meds, tLogs, wLogs, uAppts, wAppts ->
                    Pair(
                        listOf(meds, tLogs, wLogs, uAppts, wAppts),
                        null // placeholder for second batch
                    )
                }.combine(recentJournals) { pair, journals ->
                    Triple(pair.first, journals, null as Int?)
                }.combine(activeConditions) { triple, condCount ->
                    // triple.first = list of 5 items, triple.second = journals, condCount = Int
                    @Suppress("UNCHECKED_CAST")
                    val medications = triple.first[0] as List<MedicationEntity>
                    @Suppress("UNCHECKED_CAST")
                    val tLogs = triple.first[1] as List<MedicationLogEntity>
                    @Suppress("UNCHECKED_CAST")
                    val wLogs = triple.first[2] as List<MedicationLogEntity>
                    @Suppress("UNCHECKED_CAST")
                    val uAppts = triple.first[3] as List<AppointmentEntity>
                    @Suppress("UNCHECKED_CAST")
                    val wAppts = triple.first[4] as List<AppointmentEntity>
                    val rJournals = triple.second as List<JournalEntryEntity>

                    // Build today's medication info
                    val todayMeds = medications.map { med ->
                        val medLogsToday = tLogs.filter { it.medicationId == med.id }
                        TodayMedicationInfo(
                            medication = med,
                            hasBeenLoggedToday = medLogsToday.isNotEmpty(),
                            lastLogStatus = medLogsToday.firstOrNull()?.status
                        )
                    }

                    // Calculate adherence for last 7 days
                    var expectedTotal = 0
                    var takenTotal = 0

                    for (med in medications) {
                        val dailyDoses = when (med.frequency) {
                            "once-daily" -> 1
                            "twice-daily" -> 2
                            "three-daily" -> 3
                            "weekly" -> 1.0 / 7.0
                            "as-needed" -> continue // skip as-needed from adherence
                            "custom" -> 1
                            else -> 1
                        }
                        expectedTotal += (dailyDoses * 7).toInt()
                    }

                    for (log in wLogs) {
                        if (log.status == "taken" || log.status == "late") {
                            takenTotal++
                        }
                    }

                    val adherence = if (expectedTotal > 0) {
                        (takenTotal * 100) / expectedTotal
                    } else {
                        100
                    }

                    // Merge upcoming and week-range appointments (dedup by id)
                    val allUpcoming = (wAppts + uAppts).distinctBy { it.id }

                    DashboardState(
                        todayMedications = todayMeds,
                        upcomingAppointments = allUpcoming,
                        recentJournal = rJournals,
                        activeConditionsCount = condCount,
                        medicationAdherence = adherence,
                        totalMedications = medications.size,
                        upcomingAppointmentsCount = allUpcoming.size,
                        isLoading = false
                    )
                }.collect { state ->
                    _dashboardState.value = state
                }
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(isLoading = false)
            }
        }
    }
}