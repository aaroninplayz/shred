package com.shred.app.ui.screens.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.FolderGroup
import com.shred.app.data.model.MediaType
import com.shred.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FolderBrowserUiState(
    val folders: List<FolderGroup> = emptyList(),
    val isLoading: Boolean = true
)

class FolderBrowserViewModel(
    val mediaType: MediaType,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FolderBrowserUiState())
    val uiState: StateFlow<FolderBrowserUiState> = _uiState.asStateFlow()

    companion object {
        private val persistedFolderOrders = mutableMapOf<MediaType, List<String>>()
    }

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val summaries = mediaRepository.getFolderSummaries(mediaType)
            val folderGroups = summaries.map { summary ->
                val extList = mediaRepository.getFolderExtensions(summary.folderPath, mediaType)
                val formatText = if (extList.isNotEmpty()) {
                    extList.map { it.uppercase().trim().removePrefix(".") }
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                } else "FILES"

                FolderGroup(
                    folderPath = summary.folderPath,
                    folderName = summary.folderName.ifEmpty { "Folder" },
                    mediaType = mediaType,
                    fileCount = summary.fileCount,
                    totalSizeBytes = summary.totalSizeBytes,
                    coverPath = summary.coverPath,
                    formatTypes = formatText
                )
            }

            val savedOrder = persistedFolderOrders[mediaType]
            val currentFolders = _uiState.value.folders
            val finalFolders = if (!savedOrder.isNullOrEmpty()) {
                val existingMap = folderGroups.associateBy { it.folderPath }
                val preserved = savedOrder.mapNotNull { existingMap[it] }
                val newOnes = folderGroups.filter { it.folderPath !in savedOrder.toSet() }
                preserved + newOnes
            } else if (currentFolders.isNotEmpty()) {
                val existingMap = folderGroups.associateBy { it.folderPath }
                val preserved = currentFolders.mapNotNull { existingMap[it.folderPath] }
                val newOnes = folderGroups.filter { it.folderPath !in currentFolders.map { f -> f.folderPath }.toSet() }
                preserved + newOnes
            } else {
                folderGroups
            }

            _uiState.value = FolderBrowserUiState(
                folders = finalFolders,
                isLoading = false
            )
        }
    }

    fun reorderFolder(folderPath: String) {
        val current = _uiState.value.folders
        val matched = current.find { it.folderPath == folderPath } ?: return
        val reordered = current.filter { it.folderPath != folderPath } + matched
        persistedFolderOrders[mediaType] = reordered.map { it.folderPath }
        _uiState.value = _uiState.value.copy(folders = reordered)
    }

    fun focusFolder(folderPath: String) {
        val current = _uiState.value.folders.toMutableList()
        val index = current.indexOfFirst { it.folderPath == folderPath }
        if (index > 0) {
            val item = current.removeAt(index)
            current.add(0, item)
            persistedFolderOrders[mediaType] = current.map { it.folderPath }
            _uiState.value = _uiState.value.copy(folders = current)
        }
    }
}
