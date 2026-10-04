package com.shred.app.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.MediaFile
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import com.shred.app.util.HapticFeedbackHelper
import com.shred.app.util.SoundEffectHelper
import com.shred.app.util.TrashManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val mediaRepository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    private val trashManager: TrashManager,
    private val hapticHelper: HapticFeedbackHelper,
    private val soundHelper: SoundEffectHelper
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<MediaFile>>(emptyList())
    val favorites: StateFlow<List<MediaFile>> = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            mediaRepository.getFavoritesFlow().collect { files ->
                _favorites.value = files
            }
        }
    }

    fun unfavorite(file: MediaFile) {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            hapticHelper.triggerActionHaptic(ActionType.FAVORITE, settings.hapticsEnabled, settings.hapticIntensity)
            soundHelper.playActionSound(ActionType.FAVORITE, settings.soundEnabled, settings.soundVolume)
            mediaRepository.unfavorite(file.id)
        }
    }

    fun loopCard(file: MediaFile) {
        val currentList = _favorites.value
        if (currentList.isNotEmpty()) {
            _favorites.value = currentList.filterNot { it.id == file.id } + file
        }
    }

    fun focusItem(file: MediaFile) {
        val current = _favorites.value.toMutableList()
        val index = current.indexOfFirst { it.id == file.id }
        if (index > 0) {
            val item = current.removeAt(index)
            current.add(0, item)
            _favorites.value = current
        }
    }
}
