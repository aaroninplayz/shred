package com.shred.app.data.repository

import com.shred.app.data.db.MediaFileDao
import com.shred.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

class MediaRepository(
    private val dao: MediaFileDao
) {
    fun getActiveQueueFlow(type: MediaType): Flow<List<MediaFile>> = dao.getActiveQueueFlow(type)
    suspend fun getActiveQueue(type: MediaType): List<MediaFile> = dao.getActiveQueue(type)

    fun getFavoritesFlow(): Flow<List<MediaFile>> = dao.getFavoritesFlow()
    suspend fun getFavorites(): List<MediaFile> = dao.getFavorites()

    fun getActiveCountFlow(type: MediaType): Flow<Int> = dao.getActiveCountFlow(type)
    fun getTotalCountFlow(type: MediaType): Flow<Int> = dao.getTotalCountFlow(type)
    fun getActiveTotalSizeFlow(type: MediaType): Flow<Long> = dao.getActiveTotalSizeFlow(type)
    fun getTotalDeletedSizeFlow(): Flow<Long> = dao.getTotalDeletedSizeFlow()
    fun getTotalDeletedCountFlow(): Flow<Int> = dao.getTotalDeletedCountFlow()
    fun getFavoritesCountFlow(): Flow<Int> = dao.getFavoritesCountFlow()

    suspend fun markAsKept(id: Long) = dao.markAsKept(id)
    suspend fun markAsFavorite(id: Long) = dao.markAsFavorite(id)
    suspend fun markAsDeleted(id: Long, origPath: String, trashPath: String) =
        dao.markAsDeleted(id, System.currentTimeMillis(), origPath, trashPath)

    suspend fun restoreFile(file: MediaFile, origPath: String) =
        dao.restoreFile(file.id, isKept = false, isFav = false, isDel = false, path = origPath)

    suspend fun markFolderAsKept(folderPath: String) = dao.markFolderAsKept(folderPath)
    suspend fun markFolderAsFavorite(folderPath: String) = dao.markFolderAsFavorite(folderPath)

    suspend fun resetKeptSession() = dao.resetKeptSession()

    suspend fun autoResetExpiredKept(daysThreshold: Int): Int {
        val cutoff = System.currentTimeMillis() - (daysThreshold.toLong() * 24 * 60 * 60 * 1000)
        return dao.autoResetExpiredKept(cutoff)
    }

    suspend fun getFilesForPermanentPurge(hoursThreshold: Int): List<MediaFile> {
        val cutoff = System.currentTimeMillis() - (hoursThreshold.toLong() * 60 * 60 * 1000)
        return dao.getFilesForPermanentPurge(cutoff)
    }

    suspend fun deletePermanently(id: Long) = dao.deletePermanently(id)

    suspend fun insertDiscoveredFiles(files: List<MediaFile>) = dao.insertAll(files)

    suspend fun getFolderGroups(type: MediaType): List<FolderGroup> {
        val folders = dao.getDistinctFolders(type)
        return folders.mapNotNull { folderPath ->
            val filesInFolder = dao.getFilesInFolder(folderPath, type)
            if (filesInFolder.isNotEmpty()) {
                val folderFile = File(folderPath)
                FolderGroup(
                    folderPath = folderPath,
                    folderName = folderFile.name.ifEmpty { "Root" },
                    mediaType = type,
                    fileCount = filesInFolder.size,
                    totalSizeBytes = filesInFolder.sumOf { it.sizeBytes },
                    files = filesInFolder
                )
            } else null
        }
    }
}
