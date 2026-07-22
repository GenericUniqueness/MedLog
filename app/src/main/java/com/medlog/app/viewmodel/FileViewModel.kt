package com.medlog.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medlog.app.data.local.entity.FileAttachmentEntity
import com.medlog.app.data.local.repository.FileAttachmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileViewModel(private val repository: FileAttachmentRepository) : ViewModel() {

    private val _files = MutableStateFlow<List<FileAttachmentEntity>>(emptyList())
    val files: StateFlow<List<FileAttachmentEntity>> = _files.asStateFlow()

    private val _selectedFile = MutableStateFlow<FileAttachmentEntity?>(null)
    val selectedFile: StateFlow<FileAttachmentEntity?> = _selectedFile.asStateFlow()

    fun loadForProfile(profileId: Long) {
        viewModelScope.launch {
            repository.getByProfile(profileId).collect { _files.value = it }
        }
    }

    fun selectFile(file: FileAttachmentEntity?) {
        _selectedFile.value = file
    }

    /**
     * Creates a database record for a file attachment. The actual file copy/storage
     * is handled by the UI layer before or after calling this method.
     */
    fun addFile(
        profileId: Long,
        fileName: String,
        fileType: String,
        fileSize: Long,
        filePath: String,
        category: String?,
        description: String?,
        linkedType: String?,
        linkedId: Long?
    ) {
        viewModelScope.launch {
            val now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val attachment = FileAttachmentEntity(
                profileId = profileId,
                fileName = fileName,
                fileType = fileType,
                fileSize = fileSize,
                filePath = filePath,
                category = category,
                description = description,
                linkedType = linkedType,
                linkedId = linkedId,
                createdAt = now
            )
            repository.insert(attachment)
        }
    }

    fun deleteFile(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
