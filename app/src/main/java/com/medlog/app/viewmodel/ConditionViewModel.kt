package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.data.local.entity.ConditionNoteEntity
import com.medlog.app.data.local.repository.ConditionNoteRepository
import com.medlog.app.data.local.repository.ConditionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConditionViewModel(
    private val conditionRepository: ConditionRepository,
    private val conditionNoteRepository: ConditionNoteRepository
) : ViewModel() {

    private val _conditions = MutableStateFlow<List<ConditionEntity>>(emptyList())
    val conditions: StateFlow<List<ConditionEntity>> = _conditions.asStateFlow()

    private val _selectedCondition = MutableStateFlow<ConditionEntity?>(null)
    val selectedCondition: StateFlow<ConditionEntity?> = _selectedCondition.asStateFlow()

    private val _notesForSelected = MutableStateFlow<List<ConditionNoteEntity>>(emptyList())
    val notesForSelected: StateFlow<List<ConditionNoteEntity>> = _notesForSelected.asStateFlow()

    private var notesCollectionJob: Job? = null

    fun loadForProfile(profileId: Long) {
        viewModelScope.launch {
            conditionRepository.getByProfile(profileId).collect { _conditions.value = it }
        }
    }

    fun selectCondition(condition: ConditionEntity?) {
        _selectedCondition.value = condition
        loadNotesForCondition(condition?.id ?: 0L)
    }

    private fun loadNotesForCondition(conditionId: Long) {
        notesCollectionJob?.cancel()
        if (conditionId == 0L) {
            _notesForSelected.value = emptyList()
            return
        }
        notesCollectionJob = viewModelScope.launch {
            conditionNoteRepository.getByCondition(conditionId).collect { _notesForSelected.value = it }
        }
    }

    fun createCondition(
        profileId: Long,
        name: String,
        severity: String,
        status: String,
        diagnosedAt: String?,
        doctor: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val condition = ConditionEntity(
                profileId = profileId,
                name = name,
                severity = severity,
                status = status,
                diagnosedAt = diagnosedAt,
                doctor = doctor,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )
            conditionRepository.insert(condition)
        }
    }

    fun updateCondition(
        id: Long,
        name: String,
        severity: String,
        status: String,
        diagnosedAt: String?,
        doctor: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val existing = _conditions.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                name = name,
                severity = severity,
                status = status,
                diagnosedAt = diagnosedAt,
                doctor = doctor,
                notes = notes,
                updatedAt = now
            )
            conditionRepository.update(updated)
        }
    }

    fun deleteCondition(id: Long) {
        viewModelScope.launch {
            conditionRepository.deleteById(id)
        }
    }

    fun addNote(
        conditionId: Long,
        content: String,
        mood: String?,
        painLevel: Int?
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val note = ConditionNoteEntity(
                conditionId = conditionId,
                date = today,
                content = content,
                mood = mood,
                painLevel = painLevel,
                createdAt = now,
                updatedAt = now
            )
            conditionNoteRepository.insert(note)
        }
    }

    fun updateNote(
        id: Long,
        date: String,
        content: String,
        mood: String?,
        painLevel: Int?
    ) {
        viewModelScope.launch {
            val existing = _notesForSelected.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                date = date,
                content = content,
                mood = mood,
                painLevel = painLevel,
                updatedAt = now
            )
            conditionNoteRepository.update(updated)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            conditionNoteRepository.deleteById(id)
        }
    }
}
