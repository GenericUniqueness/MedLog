package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.JournalEntryEntity
import com.medlog.app.data.local.repository.JournalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JournalViewModel(private val repository: JournalRepository) : ViewModel() {

    private val _entries = MutableStateFlow<List<JournalEntryEntity>>(emptyList())
    val entries: StateFlow<List<JournalEntryEntity>> = _entries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<JournalEntryEntity?>(null)
    val selectedEntry: StateFlow<JournalEntryEntity?> = _selectedEntry.asStateFlow()

    private val _dateRangeEntries = MutableStateFlow<List<JournalEntryEntity>>(emptyList())
    val dateRangeEntries: StateFlow<List<JournalEntryEntity>> = _dateRangeEntries.asStateFlow()

    private var dateRangeJob: Job? = null

    fun loadForProfile(profileId: Long) {
        viewModelScope.launch {
            repository.getByProfile(profileId).collect { _entries.value = it }
        }
    }

    fun selectEntry(entry: JournalEntryEntity?) {
        _selectedEntry.value = entry
    }

    fun createEntry(
        profileId: Long,
        title: String?,
        content: String,
        date: String?,
        mood: String?,
        tags: String?
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val entryDate = date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val entry = JournalEntryEntity(
                profileId = profileId,
                title = title,
                content = content,
                date = entryDate,
                mood = mood,
                tags = tags,
                createdAt = now,
                updatedAt = now
            )
            repository.insert(entry)
        }
    }

    fun updateEntry(
        id: Long,
        title: String?,
        content: String,
        date: String,
        mood: String?,
        tags: String?
    ) {
        viewModelScope.launch {
            val existing = _entries.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                title = title,
                content = content,
                date = date,
                mood = mood,
                tags = tags,
                updatedAt = now
            )
            repository.update(updated)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun getByDateRange(profileId: Long, start: String, end: String) {
        dateRangeJob?.cancel()
        dateRangeJob = viewModelScope.launch {
            repository.getByDateRange(profileId, start, end).collect { _dateRangeEntries.value = it }
        }
    }
}
