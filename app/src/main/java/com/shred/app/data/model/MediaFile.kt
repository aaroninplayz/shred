package com.shred.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_files",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["mediaType"]),
        Index(value = ["isKept"]),
        Index(value = ["isFavorited"]),
        Index(value = ["isDeleted"]),
        Index(value = ["folderPath"])
    ]
)
data class MediaFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val fileName: String,
    val folderPath: String,
    val folderName: String,
    val sizeBytes: Long,
    val dateModified: Long,
    val dateTaken: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long = 0,
    val extension: String,
    val mediaType: MediaType,
    val isHidden: Boolean = false,
    val isFavorited: Boolean = false,
    val isKept: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long = 0,
    val originalPathBeforeTrash: String? = null,
    val keptTimestamp: Long = 0
)

data class FolderGroup(
    val folderPath: String,
    val folderName: String,
    val mediaType: MediaType,
    val fileCount: Int,
    val totalSizeBytes: Long,
    val files: List<MediaFile> = emptyList(),
    val formatTypes: String = "",
    val coverPath: String? = null
)

data class FolderSummary(
    val folderPath: String,
    val folderName: String,
    val fileCount: Int,
    val totalSizeBytes: Long,
    val coverPath: String?
)

data class FormatTypeStat(
    val extension: String,
    val count: Int,
    val totalSizeBytes: Long
)

data class ScanProgress(
    val scannedCount: Int = 0,
    val discoveredMediaCount: Int = 0,
    val discoveredPhotosCount: Int = 0,
    val discoveredVideosCount: Int = 0,
    val discoveredAudioCount: Int = 0,
    val discoveredPhotosSize: Long = 0L,
    val discoveredVideosSize: Long = 0L,
    val discoveredAudioSize: Long = 0L,
    val currentPath: String = "",
    val isComplete: Boolean = false
)
