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
    val allFolderGroups: List<FolderGroup> = emptyList(),
    val isFolderGridVisible: Boolean = false,
    val currentPosition: Int = 0,
    val totalCount: Int = 0,
    val deletedSizeInSession: Long = 0,
    val keptCount: Int = 0,
    val deletedCount: Int = 0,
    val favoritedCount: Int = 0,
    val canUndo: Boolean = false,
    val isLoading: Boolean = true,
    val pendingFolderDelete: FolderGroup? = null,
    val settings: AppSettings = AppSettings()
)

class SwipeViewModel(
    val mediaType: MediaType,
    val folderPath: String? = null,
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
        observeSettings()
    }

    private var settingsJob: kotlinx.coroutines.Job? = null

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = viewModelScope.launch {
            settingsRepository.getSettingsFlow().collect { newSettings ->
                val currentSort = _uiState.value.settings.sortOrder
                val sortChanged = currentSort != newSettings.sortOrder
                _uiState.value = _uiState.value.copy(settings = newSettings)
                if (sortChanged) {
                    loadQueue()
                }
            }
        }
    }

    private var isLoadingMore = false

    private fun loadQueue() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val settings = settingsRepository.getSettings()

            val totalCount: Int
            val initialKept: Int
            val initialFav: Int
            val initialDel: Int
            val initialDelSize: Long

            if (!folderPath.isNullOrEmpty()) {
                totalCount = mediaRepository.getFolderTotalCount(folderPath, mediaType)
                initialKept = mediaRepository.getFolderKeptCount(folderPath, mediaType)
                initialFav = mediaRepository.getFolderFavoritedCount(folderPath, mediaType)
                initialDel = mediaRepository.getFolderDeletedCount(folderPath, mediaType)
                val folderDelSize = mediaRepository.getFolderDeletedSize(folderPath)
                initialDelSize = if (folderDelSize > 0) folderDelSize else mediaRepository.getTotalDeletedSize()
            } else {
                totalCount = mediaRepository.getTotalCount(mediaType)
                initialKept = mediaRepository.getKeptCount(mediaType)
                initialFav = mediaRepository.getFavoritedCount(mediaType)
                initialDel = mediaRepository.getDeletedCount(mediaType)
                initialDelSize = mediaRepository.getTotalDeletedSize()
            }

            val reviewedCount = initialKept + initialFav + initialDel

            val rawFiles = if (!folderPath.isNullOrEmpty()) {
                mediaRepository.getFilesInFolderBatch(folderPath, mediaType, settings.sortOrder, 300)
            } else {
                val active = mediaRepository.getActiveQueueBatch(mediaType, settings.sortOrder, 300)
                if (active.isEmpty()) {
                    mediaRepository.resetKeptSession()
                    mediaRepository.getActiveQueueBatch(mediaType, settings.sortOrder, 300)
                } else {
                    active
                }
            }

            // Apply precise sorting via DateUtils (prioritizing true capture time, then modified time, then size)
            val sortedFiles = com.shred.app.util.DateUtils.sortMediaFiles(rawFiles, settings.sortOrder)

            allQueueFiles = sortedFiles.toMutableList()

            // Card deck items strictly 1:1 in sorted order (no folder clumping)
            val swipeItems = sortedFiles.map { SwipeItem.SingleFile(it) }

            // Group into folder summaries for pull-down panel / stats, without disturbing card deck sequence
            val folderGroups = sortedFiles.groupBy { it.folderPath }
            val discoveredGroups = folderGroups.map { (path, files) ->
                FolderGroup(
                    folderPath = path,
                    folderName = files.first().folderName,
                    mediaType = mediaType,
                    fileCount = files.size,
                    totalSizeBytes = files.sumOf { it.sizeBytes },
                    files = files
                )
            }

            _uiState.value = _uiState.value.copy(
                items = swipeItems,
                allFolderGroups = discoveredGroups,
                currentPosition = reviewedCount,
                totalCount = totalCount,
                keptCount = initialKept,
                deletedCount = initialDel,
                favoritedCount = initialFav,
                deletedSizeInSession = initialDelSize,
                isLoading = false,
                settings = settings
            )
        }
    }

    private fun checkAndFetchMoreFiles() {
        if (_uiState.value.items.size <= 10 && !isLoadingMore) {
            isLoadingMore = true
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val settings = _uiState.value.settings
                val nextFiles = if (!folderPath.isNullOrEmpty()) {
                    mediaRepository.getFilesInFolderBatch(folderPath, mediaType, settings.sortOrder, 300)
                } else {
                    mediaRepository.getActiveQueueBatch(mediaType, settings.sortOrder, 300)
                }
                val currentIds = _uiState.value.items.mapNotNull {
                    when (it) {
                        is SwipeItem.SingleFile -> it.file.id
                        is SwipeItem.Group -> null
                    }
                }.toSet()
                val fresh = nextFiles.filter { !currentIds.contains(it.id) }
                if (fresh.isNotEmpty()) {
                    val sortedFresh = com.shred.app.util.DateUtils.sortMediaFiles(fresh, settings.sortOrder)
                    val newItems = _uiState.value.items + sortedFresh.map { SwipeItem.SingleFile(it) }
                    _uiState.value = _uiState.value.copy(items = newItems)
                }
                isLoadingMore = false
            }
        }
    }

    fun showFolderGrid() {
        _uiState.value = _uiState.value.copy(isFolderGridVisible = true)
    }

    fun hideFolderGrid() {
        _uiState.value = _uiState.value.copy(isFolderGridVisible = false)
    }

    fun selectFolderToStart(group: FolderGroup) {
        viewModelScope.launch {
            val currentItems = _uiState.value.items.toMutableList()

            // Extract items matching target folder
            val matchedItems = currentItems.filter { item ->
                when (item) {
                    is SwipeItem.Group -> item.group.folderPath == group.folderPath
                    is SwipeItem.SingleFile -> item.file.folderPath == group.folderPath
                }
            }

            val remainingItems = currentItems.filter { item ->
                when (item) {
                    is SwipeItem.Group -> item.group.folderPath != group.folderPath
                    is SwipeItem.SingleFile -> item.file.folderPath != group.folderPath
                }
            }

            val newItems = if (matchedItems.isNotEmpty()) {
                matchedItems + remainingItems
            } else {
                // If files of this folder were not in current active queue slice, prepend group's files directly
                group.files.map { SwipeItem.SingleFile(it) } + currentItems
            }

            _uiState.value = _uiState.value.copy(
                items = newItems,
                isFolderGridVisible = false
            )
        }
    }

    fun handleSwipeAction(actionType: ActionType, file: MediaFile) {
        val settings = _uiState.value.settings
        hapticHelper.triggerActionHaptic(actionType, settings.hapticsEnabled, settings.hapticIntensity)

        // Push to undo stack
        undoStack.push(LastAction(actionType, file))

        // Remove top item immediately from UI list for instant responsiveness
        val updatedItems = _uiState.value.items.toMutableList()
        if (updatedItems.isNotEmpty()) {
            updatedItems.removeAt(0)
        }

        val newKept = if (actionType == ActionType.KEEP) _uiState.value.keptCount + 1 else _uiState.value.keptCount
        val newDeleted = if (actionType == ActionType.DELETE) _uiState.value.deletedCount + 1 else _uiState.value.deletedCount
        val newFav = if (actionType == ActionType.FAVORITE) _uiState.value.favoritedCount + 1 else _uiState.value.favoritedCount
        val newFreed = if (actionType == ActionType.DELETE) _uiState.value.deletedSizeInSession + file.sizeBytes else _uiState.value.deletedSizeInSession

        _uiState.value = _uiState.value.copy(
            items = updatedItems,
            keptCount = newKept,
            deletedCount = newDeleted,
            favoritedCount = newFav,
            deletedSizeInSession = newFreed,
            currentPosition = _uiState.value.currentPosition + 1,
            canUndo = undoStack.isNotEmpty()
        )

        // Asynchronous DB operation off main thread
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            when (actionType) {
                ActionType.KEEP -> {
                    mediaRepository.markAsKept(file.id)
                }
                ActionType.DELETE -> {
                    try {
                        trashManager.moveToTrash(file)
                    } catch (e: Exception) {
                        android.util.Log.e("SwipeViewModel", "Failed to move file to trash: ${file.path}", e)
                    }
                }
                ActionType.FAVORITE -> {
                    mediaRepository.markAsFavorite(file.id)
                }
            }
        }

        checkAndFetchMoreFiles()
    }

    fun requestFolderDelete(group: FolderGroup) {
        _uiState.value = _uiState.value.copy(pendingFolderDelete = group)
    }

    fun cancelFolderDelete() {
        _uiState.value = _uiState.value.copy(pendingFolderDelete = null)
    }

    fun confirmFolderDelete() {
        val group = _uiState.value.pendingFolderDelete ?: return
        viewModelScope.launch {
            val settings = _uiState.value.settings
            hapticHelper.triggerActionHaptic(ActionType.DELETE, settings.hapticsEnabled, settings.hapticIntensity)
            // Sound effects removed

            group.files.forEach { file ->
                try {
                    trashManager.moveToTrash(file)
                } catch (e: Exception) {
                    android.util.Log.e("SwipeViewModel", "Failed to trash folder file: ${file.path}", e)
                }
                undoStack.push(LastAction(ActionType.DELETE, file))
            }

            val updatedItems = _uiState.value.items.toMutableList()
            if (updatedItems.isNotEmpty() && updatedItems.first() is SwipeItem.Group && (updatedItems.first() as SwipeItem.Group).group.folderPath == group.folderPath) {
                updatedItems.removeAt(0)
            } else {
                updatedItems.removeAll { it is SwipeItem.Group && it.group.folderPath == group.folderPath }
            }

            _uiState.value = _uiState.value.copy(
                items = updatedItems,
                deletedCount = _uiState.value.deletedCount + group.fileCount,
                deletedSizeInSession = _uiState.value.deletedSizeInSession + group.totalSizeBytes,
                pendingFolderDelete = null,
                canUndo = undoStack.isNotEmpty()
            )
        }
    }

    fun reshuffleTopItem() {
        val updatedItems = _uiState.value.items.toMutableList()
        if (updatedItems.size > 1) {
            val item = updatedItems.removeAt(0)
            updatedItems.add(item)
            _uiState.value = _uiState.value.copy(items = updatedItems)
        }
    }

    fun handleFolderGroupSwipe(actionType: ActionType, group: FolderGroup) {
        viewModelScope.launch {
            val settings = _uiState.value.settings

            when (actionType) {
                ActionType.KEEP -> {
                    hapticHelper.triggerActionHaptic(actionType, settings.hapticsEnabled, settings.hapticIntensity)
                    soundHelper.playActionSound(actionType, settings.soundEnabled, settings.soundVolume)
                    mediaRepository.markFolderAsKept(group.folderPath)

                    val updatedItems = _uiState.value.items.toMutableList()
                    if (updatedItems.isNotEmpty()) updatedItems.removeAt(0)

                    _uiState.value = _uiState.value.copy(
                        items = updatedItems,
                        keptCount = _uiState.value.keptCount + group.fileCount
                    )
                }
                ActionType.DELETE -> {
                    requestFolderDelete(group)
                }
                ActionType.FAVORITE -> {
                    hapticHelper.triggerActionHaptic(actionType, settings.hapticsEnabled, settings.hapticIntensity)
                    soundHelper.playActionSound(actionType, settings.soundEnabled, settings.soundVolume)
                    mediaRepository.markFolderAsFavorite(group.folderPath)

                    val updatedItems = _uiState.value.items.toMutableList()
                    if (updatedItems.isNotEmpty()) updatedItems.removeAt(0)

                    _uiState.value = _uiState.value.copy(
                        items = updatedItems,
                        favoritedCount = _uiState.value.favoritedCount + group.fileCount
                    )
                }
            }
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
                    try {
                        trashManager.restoreFromTrash(last.file)
                    } catch (e: Exception) {
                        android.util.Log.e("SwipeViewModel", "Failed to restore file from trash: ${last.file.path}", e)
                    }
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
                canUndo = undoStack.isNotEmpty()
            )
        }
    }
}
