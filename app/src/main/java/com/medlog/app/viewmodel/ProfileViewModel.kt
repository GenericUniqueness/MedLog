package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.local.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _profiles = MutableStateFlow<List<ProfileEntity>>(emptyList())
    val profiles: StateFlow<List<ProfileEntity>> = _profiles.asStateFlow()

    private val _activeProfile = MutableStateFlow<ProfileEntity?>(null)
    val activeProfile: StateFlow<ProfileEntity?> = _activeProfile.asStateFlow()

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { _profiles.value = it }
        }
        viewModelScope.launch {
            repository.getActive().collect { _activeProfile.value = it }
        }
        viewModelScope.launch {
            repository.count().collect { _count.value = it }
        }
    }

    fun createProfile(
        name: String,
        dateOfBirth: String?,
        gender: String?,
        bloodType: String?,
        allergies: String?,
        emergencyContact: String?,
        notes: String,
        color: String
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val profile = ProfileEntity(
                name = name,
                dateOfBirth = dateOfBirth,
                gender = gender,
                bloodType = bloodType,
                allergies = allergies,
                emergencyContact = emergencyContact,
                notes = notes,
                color = color,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            val id = repository.insert(profile)
            if (_profiles.value.isEmpty()) {
                repository.setActive(id)
            }
        }
    }

    fun updateProfile(
        id: Long,
        name: String,
        dateOfBirth: String?,
        gender: String?,
        bloodType: String?,
        allergies: String?,
        emergencyContact: String?,
        notes: String,
        color: String
    ) {
        viewModelScope.launch {
            val existing = _profiles.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                name = name,
                dateOfBirth = dateOfBirth,
                gender = gender,
                bloodType = bloodType,
                allergies = allergies,
                emergencyContact = emergencyContact,
                notes = notes,
                color = color,
                updatedAt = now
            )
            repository.update(updated)
        }
    }

    fun setActiveProfile(id: Long) {
        viewModelScope.launch {
            repository.setActive(id)
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
