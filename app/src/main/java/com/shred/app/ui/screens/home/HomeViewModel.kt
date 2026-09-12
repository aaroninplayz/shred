package com.shred.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.AppSettings
import com.shred.app.data.model.MediaType
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*

import com.shred.app.scanner.MediaScanner
import kotlinx.coroutines.flow.*

data class HomeUiState(
    val photoCount: Int = 0,
    val photoSize: Long = 0,
    val videoCount: Int = 0,
    val videoSize: Long = 0,
    val audioCount: Int = 0,
    val audioSize: Long = 0,
    val totalDeletedSize: Long = 0,
    val favoritesCount: Int = 0,
    val isScanning: Boolean = false,
    val scannedCount: Int = 0,
    val settings: AppSettings = AppSettings()
)

data class CategoryStats(
    val count: Int = 0,
    val size: Long = 0
)

class HomeViewModel(
    private val mediaRepository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    private val scanner: MediaScanner
) : ViewModel() {

    private val photoStats = combine(
        mediaRepository.getActiveCountFlow(MediaType.PHOTO),
        mediaRepository.getActiveTotalSizeFlow(MediaType.PHOTO)
    ) { count, size -> CategoryStats(count, size) }

    private val videoStats = combine(
        mediaRepository.getActiveCountFlow(MediaType.VIDEO),
        mediaRepository.getActiveTotalSizeFlow(MediaType.VIDEO)
    ) { count, size -> CategoryStats(count, size) }

    private val audioStats = combine(
        mediaRepository.getActiveCountFlow(MediaType.AUDIO),
        mediaRepository.getActiveTotalSizeFlow(MediaType.AUDIO)
    ) { count, size -> CategoryStats(count, size) }

    private val globalStats = combine(
        mediaRepository.getTotalDeletedSizeFlow(),
        mediaRepository.getFavoritesCountFlow(),
        settingsRepository.getSettingsFlow()
    ) { delSize, favCount, settings -> Triple(delSize, favCount, settings) }

    private val scanState = combine(
        scanner.isScanning,
        scanner.scanProgress
    ) { isScanning, progress -> Pair(isScanning, progress) }

    val uiState: StateFlow<HomeUiState> = combine(
        photoStats,
        videoStats,
        audioStats,
        globalStats,
        scanState
    ) { p, v, a, g, s ->
        val isScanning = s.first
        val progress = s.second

        val livePhotoCount = if (isScanning && progress != null) maxOf(p.count, progress.discoveredPhotosCount) else p.count
        val livePhotoSize = if (isScanning && progress != null) maxOf(p.size, progress.discoveredPhotosSize) else p.size

        val liveVideoCount = if (isScanning && progress != null) maxOf(v.count, progress.discoveredVideosCount) else v.count
        val liveVideoSize = if (isScanning && progress != null) maxOf(v.size, progress.discoveredVideosSize) else v.size

        val liveAudioCount = if (isScanning && progress != null) maxOf(a.count, progress.discoveredAudioCount) else a.count
        val liveAudioSize = if (isScanning && progress != null) maxOf(a.size, progress.discoveredAudioSize) else a.size

        HomeUiState(
            photoCount = livePhotoCount,
            photoSize = livePhotoSize,
            videoCount = liveVideoCount,
            videoSize = liveVideoSize,
            audioCount = liveAudioCount,
            audioSize = liveAudioSize,
            totalDeletedSize = g.first,
            favoritesCount = g.second,
            isScanning = isScanning,
            scannedCount = progress?.scannedCount ?: 0,
            settings = g.third
        )
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
