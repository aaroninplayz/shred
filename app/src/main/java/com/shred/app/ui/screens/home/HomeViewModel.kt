package com.shred.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.AppSettings
import com.shred.app.data.model.FormatTypeStat
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

    companion object {
        private var persistedCategoryOrder: List<MediaType> = listOf(MediaType.PHOTO, MediaType.VIDEO, MediaType.AUDIO)
    }

    private val _categoryOrder = MutableStateFlow(persistedCategoryOrder)
    val categoryOrder: StateFlow<List<MediaType>> = _categoryOrder.asStateFlow()

    fun reorderCategory(looped: MediaType) {
        val current = _categoryOrder.value
        val updated = current.filter { it != looped } + looped
        persistedCategoryOrder = updated
        _categoryOrder.value = updated
    }


    private val photoStats = combine(
        mediaRepository.getTotalCountFlow(MediaType.PHOTO),
        mediaRepository.getTotalSizeFlow(MediaType.PHOTO)
    ) { totalCount, totalSize ->
        CategoryStats(totalCount, totalSize)
    }

    private val videoStats = combine(
        mediaRepository.getTotalCountFlow(MediaType.VIDEO),
        mediaRepository.getTotalSizeFlow(MediaType.VIDEO)
    ) { totalCount, totalSize ->
        CategoryStats(totalCount, totalSize)
    }

    private val audioStats = combine(
        mediaRepository.getTotalCountFlow(MediaType.AUDIO),
        mediaRepository.getTotalSizeFlow(MediaType.AUDIO)
    ) { totalCount, totalSize ->
        CategoryStats(totalCount, totalSize)
    }

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

    val photoFormatStats: StateFlow<List<FormatTypeStat>> =
        mediaRepository.getFormatTypeStatsFlow(MediaType.PHOTO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoFormatStats: StateFlow<List<FormatTypeStat>> =
        mediaRepository.getFormatTypeStatsFlow(MediaType.VIDEO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioFormatStats: StateFlow<List<FormatTypeStat>> =
        mediaRepository.getFormatTypeStatsFlow(MediaType.AUDIO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

