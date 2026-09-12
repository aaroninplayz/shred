package com.shred.app.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.data.model.ScanProgress
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.model.ScanMode
import com.shred.app.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.util.ArrayDeque

import androidx.exifinterface.media.ExifInterface

class MediaScanner(
    private val context: Context,
    private val mediaRepository: MediaRepository
) {
    companion object {
        private const val TAG = "MediaScanner"
        private const val BATCH_SIZE = 25
    }

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow<ScanProgress?>(null)
    val scanProgress: StateFlow<ScanProgress?> = _scanProgress.asStateFlow()

    fun startBackgroundScan(
        coroutineScope: CoroutineScope,
        settingsRepository: SettingsRepository
    ): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            if (_isScanning.value) return@launch
            _isScanning.value = true

            try {
                val appSettings = settingsRepository.getSettings()

                // Auto-reset expired kept files if enabled
                if (appSettings.autoResetEnabled) {
                    mediaRepository.autoResetExpiredKept(appSettings.autoResetDays)
                }

                if (appSettings.scanMode == ScanMode.MANUAL) {
                    _isScanning.value = false
                    return@launch
                }

                val excludedList = try {
                    val array = JSONArray(appSettings.excludedFolders)
                    List(array.length()) { array.getString(it) }
                } catch (e: Exception) {
                    emptyList()
                }

                scanFileSystem(excludedList, appSettings.minFileSizeBytes).collect { progress ->
                    _scanProgress.value = progress
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background scan error", e)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun scanFileSystem(excludedFolders: List<String> = emptyList(), minSizeBytes: Long = 0): Flow<ScanProgress> = flow {
        val rootDir = Environment.getExternalStorageDirectory()
        if (rootDir == null || !rootDir.exists()) {
            emit(ScanProgress(isComplete = true))
            return@flow
        }

        var scannedTotal = 0
        var mediaTotal = 0
        var photosTotal = 0
        var videosTotal = 0
        var audioTotal = 0
        var photosSize = 0L
        var videosSize = 0L
        var audioSize = 0L

        var lastEmitTime = System.currentTimeMillis()
        val batch = mutableListOf<MediaFile>()
        val queue = ArrayDeque<File>()
        queue.add(rootDir)

        val trashFolderPath = File(rootDir, ".PhoneCleaner/trash").absolutePath

        while (queue.isNotEmpty()) {
            val currentDir = queue.poll() ?: continue

            val currentPath = currentDir.absolutePath
            if (currentPath.startsWith(trashFolderPath)) continue
            if (excludedFolders.any { currentPath.startsWith(it) }) continue
            if (currentPath.startsWith("/proc") || currentPath.startsWith("/sys") || currentPath.startsWith("/dev")) continue

            val files = try {
                currentDir.listFiles()
            } catch (e: Exception) {
                null
            } ?: continue

            for (file in files) {
                scannedTotal++
                if (file.isDirectory) {
                    if (!file.absolutePath.startsWith(trashFolderPath)) {
                        queue.add(file)
                    }
                } else if (file.isFile && file.exists() && file.canRead() && file.length() >= minSizeBytes) {
                    val ext = file.extension.lowercase()
                    val mediaType = FileExtensionRegistry.getMediaType(ext)
                    if (mediaType != null) {
                        val length = file.length()
                        mediaTotal++
                        when (mediaType) {
                            MediaType.PHOTO -> {
                                photosTotal++
                                photosSize += length
                            }
                            MediaType.VIDEO -> {
                                videosTotal++
                                videosSize += length
                            }
                            MediaType.AUDIO -> {
                                audioTotal++
                                audioSize += length
                            }
                        }

                        val isHidden = file.name.startsWith(".") || file.parent?.contains("/.") == true
                        val parentFolder = file.parentFile
                        val folderPath = parentFolder?.absolutePath ?: rootDir.absolutePath
                        val folderName = parentFolder?.name?.ifEmpty { "Storage" } ?: "Storage"

                        val mediaFile = MediaFile(
                            path = file.absolutePath,
                            fileName = file.name,
                            folderPath = folderPath,
                            folderName = folderName,
                            sizeBytes = length,
                            dateModified = file.lastModified(),
                            dateTaken = file.lastModified(),
                            width = 0,
                            height = 0,
                            durationMs = 0L,
                            extension = ext,
                            mediaType = mediaType,
                            isHidden = isHidden
                        )
                        batch.add(mediaFile)

                        if (batch.size >= BATCH_SIZE) {
                            mediaRepository.insertDiscoveredFiles(batch.toList())
                            batch.clear()
                        }
                    }
                }

                val now = System.currentTimeMillis()
                if (scannedTotal % 200 == 0 || (now - lastEmitTime) > 300) {
                    lastEmitTime = now
                    if (batch.isNotEmpty()) {
                        mediaRepository.insertDiscoveredFiles(batch.toList())
                        batch.clear()
                    }
                    emit(
                        ScanProgress(
                            scannedCount = scannedTotal,
                            discoveredMediaCount = mediaTotal,
                            discoveredPhotosCount = photosTotal,
                            discoveredVideosCount = videosTotal,
                            discoveredAudioCount = audioTotal,
                            discoveredPhotosSize = photosSize,
                            discoveredVideosSize = videosSize,
                            discoveredAudioSize = audioSize,
                            currentPath = file.name,
                            isComplete = false
                        )
                    )
                }
            }
        }

        if (batch.isNotEmpty()) {
            mediaRepository.insertDiscoveredFiles(batch.toList())
            batch.clear()
        }

        emit(
            ScanProgress(
                scannedCount = scannedTotal,
                discoveredMediaCount = mediaTotal,
                discoveredPhotosCount = photosTotal,
                discoveredVideosCount = videosTotal,
                discoveredAudioCount = audioTotal,
                discoveredPhotosSize = photosSize,
                discoveredVideosSize = videosSize,
                discoveredAudioSize = audioSize,
                currentPath = "Done",
                isComplete = true
            )
        )
    }.flowOn(Dispatchers.IO)
}
