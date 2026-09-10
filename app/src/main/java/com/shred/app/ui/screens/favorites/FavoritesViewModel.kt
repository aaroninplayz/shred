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

    fun handleSwipeAction(actionType: ActionType, file: MediaFile) {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            hapticHelper.triggerActionHaptic(actionType, settings.hapticsEnabled, settings.hapticIntensity)
            soundHelper.playActionSound(actionType, settings.soundEnabled, settings.soundVolume)

            when (actionType) {
                ActionType.DELETE -> {
                    trashManager.moveToTrash(file)
                }
                ActionType.KEEP,
                ActionType.FAVORITE -> {
                    // Retain in favorites, advance top card
                    _favorites.value = _favorites.value.filterNot { it.id == file.id }
                }
            }
        }
    }
}
