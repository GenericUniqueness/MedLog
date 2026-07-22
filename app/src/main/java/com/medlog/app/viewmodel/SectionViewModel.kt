package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.SectionEntity
import com.medlog.app.data.local.entity.SectionEntryEntity
import com.medlog.app.data.local.repository.SectionEntryRepository
import com.medlog.app.data.local.repository.SectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SectionViewModel(
    private val sectionRepository: SectionRepository,
    private val sectionEntryRepository: SectionEntryRepository
) : ViewModel() {

    private val _sections = MutableStateFlow<List<SectionEntity>>(emptyList())
    val sections: StateFlow<List<SectionEntity>> = _sections.asStateFlow()

    private val _selectedSection = MutableStateFlow<SectionEntity?>(null)
    val selectedSection: StateFlow<SectionEntity?> = _selectedSection.asStateFlow()

    private val _entriesForSelected = MutableStateFlow<List<SectionEntryEntity>>(emptyList())
    val entriesForSelected: StateFlow<List<SectionEntryEntity>> = _entriesForSelected.asStateFlow()

    private var entriesCollectionJob: Job? = null

    fun loadForProfile(profileId: Long) {
        viewModelScope.launch {
            sectionRepository.getAllOrdered(profileId).collect { _sections.value = it }
        }
    }

    fun selectSection(section: SectionEntity?) {
        _selectedSection.value = section
        loadEntriesForSection(section?.id ?: 0L)
    }

    private fun loadEntriesForSection(sectionId: Long) {
        entriesCollectionJob?.cancel()
        if (sectionId == 0L) {
            _entriesForSelected.value = emptyList()
            return
        }
        entriesCollectionJob = viewModelScope.launch {
            sectionEntryRepository.getBySection(sectionId).collect { _entriesForSelected.value = it }
        }
    }

    fun createSection(
        profileId: Long,
        title: String,
        description: String?,
        icon: String?,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val section = SectionEntity(
                profileId = profileId,
                title = title,
                description = description,
                icon = icon,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now
            )
            sectionRepository.insert(section)
        }
    }

    fun updateSection(
        id: Long,
        title: String,
        description: String?,
        icon: String?,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            val existing = _sections.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                title = title,
                description = description,
                icon = icon,
                sortOrder = sortOrder,
                updatedAt = now
            )
            sectionRepository.update(updated)
        }
    }

    fun deleteSection(id: Long) {
        viewModelScope.launch {
            sectionRepository.deleteById(id)
        }
    }

    fun addEntry(
        sectionId: Long,
        title: String,
        content: String?,
        date: String?,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val entry = SectionEntryEntity(
                sectionId = sectionId,
                title = title,
                content = content,
                date = date,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now
            )
            sectionEntryRepository.insert(entry)
        }
    }

    fun updateEntry(
        id: Long,
        title: String,
        content: String?,
        date: String?,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            val existing = _entriesForSelected.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                title = title,
                content = content,
                date = date,
                sortOrder = sortOrder,
                updatedAt = now
            )
            sectionEntryRepository.update(updated)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            sectionEntryRepository.deleteById(id)
        }
    }

    fun reorderSections(sections: List<SectionEntity>) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            sections.forEachIndexed { index, section ->
                sectionRepository.update(
                    section.copy(sortOrder = index, updatedAt = now)
                )
            }
        }
    }
}
