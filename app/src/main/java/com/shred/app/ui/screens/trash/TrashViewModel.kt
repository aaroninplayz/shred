package com.shred.app.ui.screens.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.MediaFile
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import com.shred.app.util.HapticFeedbackHelper
import com.shred.app.util.SoundEffectHelper
import com.shred.app.util.TrashManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class TrashViewModel(
    private val mediaRepository: MediaRepository,
    private val trashManager: TrashManager,
    private val settingsRepository: SettingsRepository,
    private val hapticHelper: HapticFeedbackHelper,
    private val soundHelper: SoundEffectHelper
) : ViewModel() {

    private val _trashItems = MutableStateFlow<List<MediaFile>>(emptyList())
    val trashItems: StateFlow<List<MediaFile>> = _trashItems.asStateFlow()

    val totalTrashSize: StateFlow<Long> = mediaRepository.getTotalDeletedSizeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val retentionHours: StateFlow<Int> = settingsRepository.getSettingsFlow()
        .map { it.trashRetentionHours }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val _isPurgeDialogVisible = MutableStateFlow(false)
    val isPurgeDialogVisible: StateFlow<Boolean> = _isPurgeDialogVisible.asStateFlow()

    init {
        loadTrashFiles()
    }

    private fun loadTrashFiles() {
        viewModelScope.launch {
            mediaRepository.getTrashFilesFlow().collectLatest { files ->
                _trashItems.value = files
            }
        }
    }

    fun loopCard(file: MediaFile) {
        val current = _trashItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == file.id }
        if (index != -1) {
            val item = current.removeAt(index)
            current.add(item)
            _trashItems.value = current
        }
    }

    fun focusItem(file: MediaFile) {
        val current = _trashItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == file.id }
        if (index > 0) {
            val item = current.removeAt(index)
            current.add(0, item)
            _trashItems.value = current
        }
    }

    fun restoreFile(file: MediaFile) {
        viewModelScope.launch {
            hapticHelper.triggerTick(enabled = true)
            trashManager.restoreFromTrash(file)
            _trashItems.value = _trashItems.value.filter { it.id != file.id }
        }
    }

    fun showPurgeDialog() {
        _isPurgeDialogVisible.value = true
    }

    fun dismissPurgeDialog() {
        _isPurgeDialogVisible.value = false
    }

    fun confirmPurgeAll() {
        viewModelScope.launch {
            _isPurgeDialogVisible.value = false
            hapticHelper.triggerTick(enabled = true)
            val allTrash = mediaRepository.getAllTrashFiles()
            for (file in allTrash) {
                try {
                    val diskFile = File(file.path)
                    if (diskFile.exists()) {
                        diskFile.delete()
                    }
                    mediaRepository.deletePermanently(file.id)
                } catch (e: Exception) {
                    // Ignore
                }
            }
            _trashItems.value = emptyList()
        }
    }
}
