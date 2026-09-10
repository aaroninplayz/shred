package com.shred.app.ui.screens.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.*
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import com.shred.app.util.HapticFeedbackHelper
import com.shred.app.util.SoundEffectHelper
import com.shred.app.util.TrashManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayDeque

sealed class SwipeItem {
    data class SingleFile(val file: MediaFile) : SwipeItem()
    data class Group(val group: FolderGroup) : SwipeItem()
}

data class LastAction(
    val actionType: ActionType,
    val file: MediaFile
)

data class SwipeUiState(
    val items: List<SwipeItem> = emptyList(),
    val currentPosition: Int = 0,
    val totalCount: Int = 0,
    val deletedSizeInSession: Long = 0,
    val keptCount: Int = 0,
    val deletedCount: Int = 0,
    val favoritedCount: Int = 0,
    val canUndo: Boolean = false,
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings()
)

class SwipeViewModel(
    val mediaType: MediaType,
    private val mediaRepository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    private val trashManager: TrashManager,
    private val hapticHelper: HapticFeedbackHelper,
    private val soundHelper: SoundEffectHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwipeUiState())
    val uiState: StateFlow<SwipeUiState> = _uiState.asStateFlow()

    private var undoStack = ArrayDeque<LastAction>()
    private var allQueueFiles = mutableListOf<MediaFile>()

    init {
        loadQueue()
    }

    private fun loadQueue() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val settings = settingsRepository.getSettings()
            val rawFiles = mediaRepository.getActiveQueue(mediaType)

            // Apply Sort Order
            val sortedFiles = when (settings.sortOrder) {
                SortOrder.OLDEST_FIRST -> rawFiles.sortedBy { if (it.dateTaken > 0) it.dateTaken else it.dateModified }
                SortOrder.NEWEST_FIRST -> rawFiles.sortedByDescending { if (it.dateTaken > 0) it.dateTaken else it.dateModified }
                SortOrder.LARGEST_FIRST -> rawFiles.sortedByDescending { it.sizeBytes }
                SortOrder.SMALLEST_FIRST -> rawFiles.sortedBy { it.sizeBytes }
                SortOrder.RANDOM -> rawFiles.shuffled()
            }

            allQueueFiles = sortedFiles.toMutableList()

            // Group into folder cards for distinct non-standard folders with >= 3 files
            val folderGroups = sortedFiles.groupBy { it.folderPath }
            val swipeItems = mutableListOf<SwipeItem>()

            for ((path, files) in folderGroups) {
                // If folder has 4+ files and is not a generic root, offer folder header card first
                if (files.size >= 4 && !path.endsWith("/DCIM/Camera") && !path.endsWith("/Pictures")) {
                    swipeItems.add(
                        SwipeItem.Group(
                            FolderGroup(
                                folderPath = path,
                                folderName = files.first().folderName,
                                mediaType = mediaType,
                                fileCount = files.size,
                                totalSizeBytes = files.sumOf { it.sizeBytes },
                                files = files
                            )
                        )
                    )
                } else {
                    files.forEach { swipeItems.add(SwipeItem.SingleFile(it)) }
                }
            }

            _uiState.value = _uiState.value.copy(
                items = swipeItems,
                totalCount = sortedFiles.size,
                currentPosition = 0,
                isLoading = false,
                settings = settings
            )
        }
    }

    fun handleSwipeAction(actionType: ActionType, file: MediaFile) {
        viewModelScope.launch {
            val settings = _uiState.value.settings
            hapticHelper.triggerActionHaptic(actionType, settings.hapticsEnabled, settings.hapticIntensity)
            soundHelper.playActionSound(actionType, settings.soundEnabled, settings.soundVolume)

            when (actionType) {
                ActionType.KEEP -> {
                    mediaRepository.markAsKept(file.id)
                    _uiState.value = _uiState.value.copy(
                        keptCount = _uiState.value.keptCount + 1,
                        currentPosition = _uiState.value.currentPosition + 1
                    )
                }
                ActionType.DELETE -> {
                    trashManager.moveToTrash(file)
                    _uiState.value = _uiState.value.copy(
                        deletedCount = _uiState.value.deletedCount + 1,
                        deletedSizeInSession = _uiState.value.deletedSizeInSession + file.sizeBytes,
                        currentPosition = _uiState.value.currentPosition + 1
                    )
                }
                ActionType.FAVORITE -> {
                    mediaRepository.markAsFavorite(file.id)
                    _uiState.value = _uiState.value.copy(
                        favoritedCount = _uiState.value.favoritedCount + 1,
                        currentPosition = _uiState.value.currentPosition + 1
                    )
                }
            }

            // Record single undo
            undoStack.clear()
            undoStack.push(LastAction(actionType, file))

            // Remove top item from UI list
            val updatedItems = _uiState.value.items.toMutableList()
            if (updatedItems.isNotEmpty()) {
                updatedItems.removeAt(0)
            }
            _uiState.value = _uiState.value.copy(
                items = updatedItems,
                canUndo = true
            )
        }
    }

    fun handleFolderGroupSwipe(actionType: ActionType, group: FolderGroup) {
        viewModelScope.launch {
            val settings = _uiState.value.settings
            hapticHelper.triggerActionHaptic(actionType, settings.hapticsEnabled, settings.hapticIntensity)
            soundHelper.playActionSound(actionType, settings.soundEnabled, settings.soundVolume)

            when (actionType) {
                ActionType.KEEP -> {
                    mediaRepository.markFolderAsKept(group.folderPath)
                }
                ActionType.DELETE -> {
                    group.files.forEach { file ->
                        trashManager.moveToTrash(file)
                    }
                }
                ActionType.FAVORITE -> {
                    mediaRepository.markFolderAsFavorite(group.folderPath)
                }
            }

            val updatedItems = _uiState.value.items.toMutableList()
            if (updatedItems.isNotEmpty()) {
                updatedItems.removeAt(0)
            }
            _uiState.value = _uiState.value.copy(items = updatedItems)
        }
    }

    fun expandFolderGroup(group: FolderGroup) {
        val updatedItems = _uiState.value.items.toMutableList()
        if (updatedItems.isNotEmpty()) {
            updatedItems.removeAt(0)
            // Insert all individual files at the top of the queue
            group.files.asReversed().forEach { file ->
                updatedItems.add(0, SwipeItem.SingleFile(file))
            }
            _uiState.value = _uiState.value.copy(items = updatedItems)
        }
    }

    fun performUndo() {
        val last = undoStack.poll() ?: return
        viewModelScope.launch {
            when (last.actionType) {
                ActionType.DELETE -> {
                    trashManager.restoreFromTrash(last.file)
                    _uiState.value = _uiState.value.copy(
                        deletedCount = (_uiState.value.deletedCount - 1).coerceAtLeast(0),
                        deletedSizeInSession = (_uiState.value.deletedSizeInSession - last.file.sizeBytes).coerceAtLeast(0)
                    )
                }
                ActionType.KEEP -> {
                    mediaRepository.restoreFile(last.file, last.file.path)
                    _uiState.value = _uiState.value.copy(
                        keptCount = (_uiState.value.keptCount - 1).coerceAtLeast(0)
                    )
                }
                ActionType.FAVORITE -> {
                    mediaRepository.restoreFile(last.file, last.file.path)
                    _uiState.value = _uiState.value.copy(
                        favoritedCount = (_uiState.value.favoritedCount - 1).coerceAtLeast(0)
                    )
                }
            }

            // Prepend file back to items
            val updatedItems = _uiState.value.items.toMutableList()
            updatedItems.add(0, SwipeItem.SingleFile(last.file))

            _uiState.value = _uiState.value.copy(
                items = updatedItems,
                currentPosition = (_uiState.value.currentPosition - 1).coerceAtLeast(0),
                canUndo = false
            )
        }
    }
}
