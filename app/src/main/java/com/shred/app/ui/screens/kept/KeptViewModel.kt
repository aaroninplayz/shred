package com.shred.app.ui.screens.kept

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.data.repository.MediaRepository
import com.shred.app.util.HapticFeedbackHelper
import com.shred.app.util.SoundEffectHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KeptViewModel(
    val mediaType: MediaType,
    val folderPath: String?,
    private val mediaRepository: MediaRepository,
    private val hapticHelper: HapticFeedbackHelper,
    private val soundHelper: SoundEffectHelper
) : ViewModel() {

    private val _isFolderFilterActive = MutableStateFlow(folderPath != null)
    val isFolderFilterActive: StateFlow<Boolean> = _isFolderFilterActive.asStateFlow()

    private val _localItems = MutableStateFlow<List<MediaFile>>(emptyList())
    val items: StateFlow<List<MediaFile>> = _localItems.asStateFlow()

    init {
        loadKeptFiles()
    }

    private fun loadKeptFiles() {
        viewModelScope.launch {
            _isFolderFilterActive.collectLatest { filterFolder ->
                val flow = if (filterFolder && folderPath != null) {
                    mediaRepository.getKeptFilesInFolderFlow(folderPath, mediaType)
                } else {
                    mediaRepository.getKeptFilesFlow(mediaType)
                }

                flow.collectLatest { files ->
                    _localItems.value = files
                }
            }
        }
    }

    fun toggleFolderFilter() {
        if (folderPath != null) {
            _isFolderFilterActive.value = !_isFolderFilterActive.value
        }
    }

    fun loopCard(file: MediaFile) {
        val current = _localItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == file.id }
        if (index != -1) {
            val item = current.removeAt(index)
            current.add(item)
            _localItems.value = current
        }
    }

    fun unkeep(file: MediaFile) {
        viewModelScope.launch {
            hapticHelper.triggerTick(enabled = true)
            mediaRepository.unkeep(file.id)
            _localItems.value = _localItems.value.filter { it.id != file.id }
        }
    }
}
