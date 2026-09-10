package com.shred.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shred.app.data.model.AppSettings
import com.shred.app.data.model.MediaType
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
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
    val settings: AppSettings = AppSettings()
)

data class CategoryStats(
    val count: Int = 0,
    val size: Long = 0
)

class HomeViewModel(
    private val mediaRepository: MediaRepository,
    private val settingsRepository: SettingsRepository
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

    val uiState: StateFlow<HomeUiState> = combine(
        photoStats,
        videoStats,
        audioStats,
        globalStats
    ) { p, v, a, g ->
        HomeUiState(
            photoCount = p.count,
            photoSize = p.size,
            videoCount = v.count,
            videoSize = v.size,
            audioCount = a.count,
            audioSize = a.size,
            totalDeletedSize = g.first,
            favoritesCount = g.second,
            settings = g.third
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
