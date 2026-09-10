package com.shred.app.ui.screens.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.AppSettings
import com.shred.app.data.model.LoadingAnimation
import com.shred.app.data.model.ScanMode
import com.shred.app.data.model.ScanProgress
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import com.shred.app.scanner.MediaScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.random.Random

sealed class LoadingStep {
    data class Scanning(val progress: Float, val currentPath: String, val mediaFound: Int) : LoadingStep()
    object Blank : LoadingStep()
    data class Phrase(val text: String, val animation: LoadingAnimation) : LoadingStep()
    object ReadyToTransition : LoadingStep()
}

class LoadingViewModel(
    private val scanner: MediaScanner,
    private val settingsRepository: SettingsRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _step = MutableStateFlow<LoadingStep>(LoadingStep.Scanning(0f, "Initializing...", 0))
    val step: StateFlow<LoadingStep> = _step.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        startLoadingSequence()
    }

    private fun startLoadingSequence() {
        viewModelScope.launch {
            val appSettings = settingsRepository.getSettings()
            _settings.value = appSettings

            // Auto-reset expired kept files if enabled
            if (appSettings.autoResetEnabled) {
                mediaRepository.autoResetExpiredKept(appSettings.autoResetDays)
            }

            if (appSettings.scanMode == ScanMode.MANUAL) {
                // Skip scan on launch
                showPhraseAndFinish(appSettings)
                return@launch
            }

            // Parse excluded folders
            val excludedList = try {
                val array = JSONArray(appSettings.excludedFolders)
                List(array.length()) { array.getString(it) }
            } catch (e: Exception) {
                emptyList()
            }

            var lastScanned = 0
            scanner.scanFileSystem(excludedList, appSettings.minFileSizeBytes).collect { progress ->
                lastScanned = progress.scannedCount
                // Smooth synthetic progress interpolation
                val normalizedProgress = (progress.scannedCount / 5000f).coerceIn(0.05f, 0.95f)
                _step.value = LoadingStep.Scanning(
                    progress = if (progress.isComplete) 1.0f else normalizedProgress,
                    currentPath = progress.currentPath,
                    mediaFound = progress.discoveredMediaCount
                )
            }

            // Brief pause at 100%
            delay(300)

            // Step 2: Blank screen briefly
            _step.value = LoadingStep.Blank
            delay(350)

            // Step 3: Show custom randomized phrase
            showPhraseAndFinish(appSettings)
        }
    }

    private suspend fun showPhraseAndFinish(appSettings: AppSettings) {
        val phrasesList = try {
            val array = JSONArray(appSettings.loadingPhrases)
            List(array.length()) { array.getString(it) }
        } catch (e: Exception) {
            listOf("SHREDDING DISK ENTROPY...")
        }

        val chosenPhrase = if (phrasesList.isNotEmpty()) {
            phrasesList[Random.nextInt(phrasesList.size)]
        } else {
            "SHRED YOUR JUNK."
        }

        _step.value = LoadingStep.Phrase(chosenPhrase, appSettings.loadingAnimation)
        // Give time for phrase to display
        delay(1400)

        // Step 4: Ready
        _step.value = LoadingStep.ReadyToTransition
    }
}
