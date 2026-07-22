package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.AppSettingEntity
import com.medlog.app.data.local.repository.AppSettingRepository
import com.medlog.app.data.local.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingsViewModel(
    private val appSettingRepository: AppSettingRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _notificationEnabled = MutableStateFlow(true)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _reminderLeadMinutes = MutableStateFlow(30)
    val reminderLeadMinutes: StateFlow<Int> = _reminderLeadMinutes.asStateFlow()

    private val _currentProfileId = MutableStateFlow(0L)

    fun loadSettings(profileId: Long) {
        _currentProfileId.value = profileId
        viewModelScope.launch {
            appSettingRepository.get(profileId, "notification_enabled").collect { setting ->
                _notificationEnabled.value = setting?.value?.toBoolean() ?: true
            }
        }
        viewModelScope.launch {
            appSettingRepository.get(profileId, "reminder_lead_minutes").collect { setting ->
                _reminderLeadMinutes.value = setting?.value?.toIntOrNull() ?: 30
            }
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val profileId = _currentProfileId.value
            if (profileId == 0L) return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            appSettingRepository.upsert(
                AppSettingEntity(
                    profileId = profileId,
                    key = "notification_enabled",
                    value = enabled.toString(),
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun setReminderLeadMinutes(minutes: Int) {
        viewModelScope.launch {
            val profileId = _currentProfileId.value
            if (profileId == 0L) return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            appSettingRepository.upsert(
                AppSettingEntity(
                    profileId = profileId,
                    key = "reminder_lead_minutes",
                    value = minutes.toString(),
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    /**
     * Deletes all profiles, which cascades to delete all associated data
     * (medications, conditions, appointments, journal entries, etc.)
     */
    fun clearAllData() {
        viewModelScope.launch {
            val profiles = profileRepository.getAll().first()
            for (profile in profiles) {
                profileRepository.deleteById(profile.id)
            }
        }
    }
}
