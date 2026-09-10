package com.shred.app.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.data.model.ScanProgress
import com.shred.app.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.ArrayDeque

import androidx.exifinterface.media.ExifInterface

class MediaScanner(
    private val context: Context,
    private val mediaRepository: MediaRepository
) {
    companion object {
        private const val TAG = "MediaScanner"
        private const val BATCH_SIZE = 100
    }

    fun scanFileSystem(excludedFolders: List<String> = emptyList(), minSizeBytes: Long = 0): Flow<ScanProgress> = flow {
        val rootDir = Environment.getExternalStorageDirectory()
        if (rootDir == null || !rootDir.exists()) {
            emit(ScanProgress(isComplete = true))
            return@flow
        }

        var scannedTotal = 0
        var mediaTotal = 0
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
                } else if (file.isFile && file.length() >= minSizeBytes) {
                    val ext = file.extension.lowercase()
                    val mediaType = FileExtensionRegistry.getMediaType(ext)
                    if (mediaType != null) {
                        mediaTotal++
                        val isHidden = file.name.startsWith(".") || file.parent?.contains("/.") == true
                        val parentFolder = file.parentFile
                        val folderPath = parentFolder?.absolutePath ?: rootDir.absolutePath
                        val folderName = parentFolder?.name?.ifEmpty { "Storage" } ?: "Storage"

                        var width = 0
                        var height = 0
                        var durationMs = 0L
                        var dateTaken = file.lastModified()

                        // Extract rich metadata based on type
                        try {
                            when (mediaType) {
                                MediaType.PHOTO -> {
                                    if (ext in setOf("jpg", "jpeg", "png", "webp", "heic", "heif", "dng", "raw")) {
                                        val exif = ExifInterface(file.absolutePath)
                                        width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                                        height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
                                        val exifDate = exif.dateTime
                                        if (exifDate != null && exifDate > 0) {
                                            dateTaken = exifDate
                                        }
                                    }
                                }
                                MediaType.VIDEO,
                                MediaType.AUDIO -> {
                                    MediaMetadataRetriever().use { mmr ->
                                        try {
                                            mmr.setDataSource(file.absolutePath)
                                            val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                            durationMs = durStr?.toLongOrNull() ?: 0L
                                            if (mediaType == MediaType.VIDEO) {
                                                val wStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                                                val hStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                                                width = wStr?.toIntOrNull() ?: 0
                                                height = hStr?.toIntOrNull() ?: 0
                                            }
                                        } catch (e: Exception) {
                                            // Handle corrupted video/audio metadata gracefully
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error extracting metadata for ${file.name}", e)
                        }

                        val mediaFile = MediaFile(
                            path = file.absolutePath,
                            fileName = file.name,
                            folderPath = folderPath,
                            folderName = folderName,
                            sizeBytes = file.length(),
                            dateModified = file.lastModified(),
                            dateTaken = dateTaken,
                            width = width,
                            height = height,
                            durationMs = durationMs,
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

                if (scannedTotal % 50 == 0) {
                    emit(
                        ScanProgress(
                            scannedCount = scannedTotal,
                            discoveredMediaCount = mediaTotal,
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
                currentPath = "Done",
                isComplete = true
            )
        )
    }.flowOn(Dispatchers.IO)
}
