package com.shred.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.*
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettingsFlow().collect {
                _settings.value = it
            }
        }
    }

    fun updateSettings(updated: AppSettings) {
        viewModelScope.launch {
            _settings.value = updated
            settingsRepository.updateSettings(updated)
        }
    }

    fun resetKeptSession() {
        viewModelScope.launch {
            mediaRepository.resetKeptSession()
            _toastMessage.value = "ALL KEPT FILES RESTORED TO QUEUE."
        }
    }

    fun addLoadingPhrase(phrase: String) {
        if (phrase.isBlank()) return
        val current = getLoadingPhrasesList().toMutableList()
        current.add(phrase.trim())
        val json = JSONArray(current).toString()
        updateSettings(_settings.value.copy(loadingPhrases = json))
    }

    fun removeLoadingPhrase(index: Int) {
        val current = getLoadingPhrasesList().toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            val json = JSONArray(current).toString()
            updateSettings(_settings.value.copy(loadingPhrases = json))
        }
    }

    fun getLoadingPhrasesList(): List<String> {
        return try {
            val array = JSONArray(_settings.value.loadingPhrases)
            List(array.length()) { array.getString(it) }
        } catch (e: Exception) {
            listOf("SHREDDING DISK ENTROPY...")
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
