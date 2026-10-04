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

import android.provider.MediaStore

import com.shred.app.data.model.ScanScope
import kotlinx.coroutines.yield

class MediaScanner(
    private val context: Context,
    private val mediaRepository: MediaRepository
) {
    companion object {
        private const val TAG = "MediaScanner"
        private const val BATCH_SIZE = 250
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

                scanFileSystem(
                    excludedFolders = excludedList,
                    minSizeBytes = appSettings.minFileSizeBytes,
                    scanScope = appSettings.scanScope
                ).collect { progress ->
                    _scanProgress.value = progress
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background scan error", e)
            } finally {
                _isScanning.value = false
            }
        }
    }

    private fun scanMediaStore(
        minSizeBytes: Long,
        excludedFolders: List<String>,
        scannedPaths: MutableSet<String>
    ): List<MediaFile> {
        val result = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        val uris = listOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )

        for (uri in uris) {
            try {
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)

                    if (dataCol == -1) return@use

                    while (cursor.moveToNext()) {
                        val path = cursor.getString(dataCol) ?: continue
                        if (path.isEmpty()) continue
                        val normalizedPath = try { File(path).canonicalPath } catch (e: Exception) { path }

                        if (!scannedPaths.add(normalizedPath)) continue

                        val file = File(path)
                        val sizeBytes = if (sizeCol != -1) cursor.getLong(sizeCol) else if (file.exists()) file.length() else 0L
                        if (sizeBytes < minSizeBytes) continue

                        val ext = file.extension.lowercase().ifEmpty {
                            path.substringAfterLast('.', "")
                        }
                        val mediaType = FileExtensionRegistry.getMediaType(ext) ?: continue
                        if (excludedFolders.any { path.startsWith(it) }) continue

                        val parentFolder = file.parentFile
                        val folderPath = parentFolder?.absolutePath ?: ""
                        val folderName = parentFolder?.name?.ifEmpty { "Storage" } ?: "Storage"
                        val fileName = if (nameCol != -1 && !cursor.getString(nameCol).isNullOrEmpty()) cursor.getString(nameCol) else file.name
                        val dateMod = if (dateCol != -1) cursor.getLong(dateCol) * 1000L else if (file.exists()) file.lastModified() else System.currentTimeMillis()

                        result.add(
                            MediaFile(
                                path = path,
                                fileName = fileName,
                                folderPath = folderPath,
                                folderName = folderName,
                                sizeBytes = sizeBytes,
                                dateModified = dateMod,
                                dateTaken = dateMod,
                                width = 0,
                                height = 0,
                                durationMs = 0L,
                                extension = ext,
                                mediaType = mediaType,
                                isHidden = fileName.startsWith(".")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "MediaStore query error", e)
            }
        }
        return result
    }

    fun scanFileSystem(
        excludedFolders: List<String> = emptyList(),
        minSizeBytes: Long = 0,
        scanScope: ScanScope = ScanScope.MEDIA_FOLDERS
    ): Flow<ScanProgress> = flow {
        val rootDir = Environment.getExternalStorageDirectory()

        val scannedPaths = mutableSetOf<String>()
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

        // 1. Initial High-Speed MediaStore ContentResolver Pass
        val mediaStoreFiles = scanMediaStore(minSizeBytes, excludedFolders, scannedPaths)
        if (mediaStoreFiles.isNotEmpty()) {
            mediaStoreFiles.chunked(BATCH_SIZE).forEach { chunk ->
                mediaRepository.insertDiscoveredFiles(chunk)
            }
            mediaStoreFiles.forEach { file ->
                mediaTotal++
                scannedTotal++
                when (file.mediaType) {
                    MediaType.PHOTO -> { photosTotal++; photosSize += file.sizeBytes }
                    MediaType.VIDEO -> { videosTotal++; videosSize += file.sizeBytes }
                    MediaType.AUDIO -> { audioTotal++; audioSize += file.sizeBytes }
                }
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
                    currentPath = "MediaStore Indexing",
                    isComplete = false
                )
            )
        }

        if (rootDir == null || !rootDir.exists()) {
            emit(ScanProgress(isComplete = true))
            return@flow
        }

        // 2. Multi-Root Filesystem Crawler (Targeted media folders vs Full deep crawl)
        val seedRoots = if (scanScope == ScanScope.MEDIA_FOLDERS) {
            listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                File(rootDir, "Android/media"),
                File(rootDir, "WhatsApp/Media"),
                File(rootDir, "Telegram")
            ).filter { it.exists() && it.isDirectory }
        } else {
            listOfNotNull(
                rootDir,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                File(rootDir, "Android/media"),
                File(rootDir, "WhatsApp/Media"),
                File(rootDir, "Telegram")
            ).filter { it.exists() && it.isDirectory }
        }

        val visitedDirs = mutableSetOf<String>()
        val queue = ArrayDeque<Pair<File, Int>>() // Directory and Depth
        seedRoots.forEach { dir ->
            val dirPath = try { dir.canonicalPath } catch (e: Exception) { dir.absolutePath }
            if (visitedDirs.add(dirPath)) {
                queue.add(Pair(dir, 0))
            }
        }

        val trashFolderPath = File(rootDir, ".PhoneCleaner/trash").absolutePath
        val maxDepth = if (scanScope == ScanScope.MEDIA_FOLDERS) 5 else 7

        while (queue.isNotEmpty()) {
            val (currentDir, depth) = queue.poll() ?: continue

            val currentPath = currentDir.absolutePath
            if (currentPath.startsWith(trashFolderPath)) continue
            if (excludedFolders.any { currentPath.startsWith(it) }) continue
            if (currentPath.startsWith("/proc") || currentPath.startsWith("/sys") || currentPath.startsWith("/dev")) continue

            // Prune heavy non-media directories
            val dirNameLower = currentDir.name.lowercase()
            if (dirNameLower == "android" && currentPath.endsWith("/android") && scanScope == ScanScope.DEEP_STORAGE) {
                // Don't crawl Android/data or Android/obb (OS blocks them & causes heavy IO timeouts)
                val androidMedia = File(currentDir, "media")
                if (androidMedia.exists() && androidMedia.isDirectory) {
                    val p = try { androidMedia.canonicalPath } catch (e: Exception) { androidMedia.absolutePath }
                    if (visitedDirs.add(p)) queue.add(Pair(androidMedia, depth + 1))
                }
                continue
            }
            if (dirNameLower in listOf(".thumbnails", ".cache", "cache", ".git", ".gradle", "node_modules", "tmp")) {
                continue
            }

            val files = try {
                currentDir.listFiles()
            } catch (e: Exception) {
                null
            } ?: continue

            for (file in files) {
                if (file.isDirectory) {
                    if (depth < maxDepth) {
                        val subDirPath = try { file.canonicalPath } catch (e: Exception) { file.absolutePath }
                        if (!subDirPath.startsWith(trashFolderPath) && visitedDirs.add(subDirPath)) {
                            queue.add(Pair(file, depth + 1))
                        }
                    }
                } else if (file.isFile && file.exists()) {
                    val normalizedFilePath = try { file.canonicalPath } catch (e: Exception) { file.absolutePath }
                    if (!scannedPaths.add(normalizedFilePath)) continue

                    scannedTotal++
                    val fileLength = file.length()
                    if (fileLength <= 0) continue

                    if (fileLength >= minSizeBytes) {
                        val ext = file.extension.lowercase()
                        val mediaType = FileExtensionRegistry.getMediaType(ext)
                        if (mediaType != null) {
                            val length = fileLength
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

                    // Cooperative CPU yield every 100 files
                    if (scannedTotal % 100 == 0) {
                        yield()
                    }

                    val now = System.currentTimeMillis()
                    // Throttle UI progress emission to at least 400ms to preserve 120 FPS
                    if (now - lastEmitTime >= 400) {
                        lastEmitTime = now
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
