package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.ClutterItemEntity
import com.medlog.app.data.local.repository.ClutterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ClutterViewModel(private val repository: ClutterRepository) : ViewModel() {

    private val _items = MutableStateFlow<List<ClutterItemEntity>>(emptyList())
    val items: StateFlow<List<ClutterItemEntity>> = _items.asStateFlow()

    private val _selectedItem = MutableStateFlow<ClutterItemEntity?>(null)
    val selectedItem: StateFlow<ClutterItemEntity?> = _selectedItem.asStateFlow()

    fun loadForProfile(profileId: Long) {
        viewModelScope.launch {
            repository.getByProfile(profileId).collect { _items.value = it }
        }
    }

    fun selectItem(item: ClutterItemEntity?) {
        _selectedItem.value = item
    }

    fun addItem(content: String, profileId: Long, category: String?) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val item = ClutterItemEntity(
                profileId = profileId,
                content = content,
                category = category,
                isPinned = false,
                createdAt = now,
                updatedAt = now
            )
            repository.insert(item)
        }
    }

    fun updateItem(item: ClutterItemEntity) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = item.copy(updatedAt = now)
            repository.update(updated)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun togglePin(id: Long) {
        viewModelScope.launch {
            val existing = _items.value.find { it.id == id } ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updated = existing.copy(
                isPinned = !existing.isPinned,
                updatedAt = now
            )
            repository.update(updated)
        }
    }
}
