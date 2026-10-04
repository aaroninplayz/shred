package com.shred.app.data.repository

import com.shred.app.data.db.MediaFileDao
import com.shred.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

class MediaRepository(
    private val dao: MediaFileDao
) {
    fun getActiveQueueFlow(type: MediaType): Flow<List<MediaFile>> = dao.getActiveQueueFlow(type)

    suspend fun getActiveQueue(type: MediaType): List<MediaFile> {
        return dao.getActiveQueue(type)
    }

    suspend fun getActiveQueueBatch(type: MediaType, limit: Int = 300): List<MediaFile> {
        return dao.getActiveQueueBatch(type, limit)
    }

    suspend fun getFilesInFolder(folderPath: String, type: MediaType): List<MediaFile> {
        return dao.getFilesInFolder(folderPath, type)
    }

    suspend fun getFilesInFolderBatch(folderPath: String, type: MediaType, limit: Int = 300): List<MediaFile> {
        return dao.getFilesInFolderBatch(folderPath, type, limit)
    }

    suspend fun getFolderSummaries(type: MediaType): List<FolderSummary> {
        return dao.getFolderSummaries(type)
    }

    suspend fun getFolderExtensions(folderPath: String, type: MediaType): List<String> {
        return dao.getFolderExtensions(folderPath, type)
    }

    suspend fun getTotalCount(type: MediaType): Int = dao.getTotalCount(type)
    suspend fun getKeptCount(type: MediaType): Int = dao.getKeptCount(type)
    suspend fun getFavoritedCount(type: MediaType): Int = dao.getFavoritedCount(type)
    suspend fun getDeletedCount(type: MediaType): Int = dao.getDeletedCount(type)
    suspend fun getTotalDeletedSize(): Long = dao.getTotalDeletedSize()

    suspend fun getFolderTotalCount(folderPath: String, type: MediaType): Int = dao.getFolderTotalCount(folderPath, type)
    suspend fun getFolderKeptCount(folderPath: String, type: MediaType): Int = dao.getFolderKeptCount(folderPath, type)
    suspend fun getFolderFavoritedCount(folderPath: String, type: MediaType): Int = dao.getFolderFavoritedCount(folderPath, type)
    suspend fun getFolderDeletedCount(folderPath: String, type: MediaType): Int = dao.getFolderDeletedCount(folderPath, type)
    suspend fun getFolderDeletedSize(folderPath: String): Long = dao.getFolderDeletedSize(folderPath)

    fun getFavoritesFlow(): Flow<List<MediaFile>> = dao.getFavoritesFlow()
    suspend fun getFavorites(): List<MediaFile> = dao.getFavorites()
    suspend fun getById(id: Long): MediaFile? = dao.getById(id)

    fun getPreviewThumbnailsFlow(type: MediaType, limit: Int = 25): Flow<List<MediaFile>> =
        dao.getPreviewThumbnailsFlow(type, limit)

    fun getActiveCountFlow(type: MediaType): Flow<Int> = dao.getActiveCountFlow(type)
    fun getTotalCountFlow(type: MediaType): Flow<Int> = dao.getTotalCountFlow(type)
    fun getActiveTotalSizeFlow(type: MediaType): Flow<Long> = dao.getActiveTotalSizeFlow(type)
    fun getTotalSizeFlow(type: MediaType): Flow<Long> = dao.getTotalSizeFlow(type)
    fun getTotalDeletedSizeFlow(): Flow<Long> = dao.getTotalDeletedSizeFlow()
    fun getTotalDeletedCountFlow(): Flow<Int> = dao.getTotalDeletedCountFlow()
    fun getFavoritesCountFlow(): Flow<Int> = dao.getFavoritesCountFlow()
    fun getFormatTypeStatsFlow(type: MediaType): Flow<List<FormatTypeStat>> = dao.getFormatTypeStatsFlow(type)

    fun getKeptFilesFlow(type: MediaType): Flow<List<MediaFile>> = dao.getKeptFilesFlow(type)
    fun getAllKeptFilesFlow(): Flow<List<MediaFile>> = dao.getAllKeptFilesFlow()
    fun getKeptFilesInFolderFlow(folderPath: String, type: MediaType): Flow<List<MediaFile>> = dao.getKeptFilesInFolderFlow(folderPath, type)
    fun getTrashFilesFlow(): Flow<List<MediaFile>> = dao.getTrashFilesFlow()
    suspend fun unkeep(id: Long) = dao.unkeep(id)
    suspend fun getAllTrashFiles(): List<MediaFile> = dao.getAllTrashFiles()

    suspend fun markAsKept(id: Long) = dao.markAsKept(id)
    suspend fun markAsFavorite(id: Long) = dao.markAsFavorite(id)
    suspend fun unfavorite(id: Long) = dao.unfavorite(id)
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
                    folderName = folderFile.name.ifEmpty { "Storage" },
                    mediaType = type,
                    fileCount = filesInFolder.size,
                    totalSizeBytes = filesInFolder.sumOf { it.sizeBytes },
                    files = filesInFolder
                )
            } else null
        }
    }
}
