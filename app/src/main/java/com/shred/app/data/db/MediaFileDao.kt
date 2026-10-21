package com.shred.app.data.db

import androidx.room.*
import com.shred.app.data.model.FormatTypeStat
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(files: List<MediaFile>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(file: MediaFile): Long

    @Update
    suspend fun update(file: MediaFile)

    @Query("SELECT * FROM media_files WHERE path = :path LIMIT 1")
    suspend fun getByPath(path: String): MediaFile?

    @Query("SELECT * FROM media_files WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MediaFile?

    // Active unreviewed queue (not kept, not favorited, not deleted)
    @Query("SELECT * FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    fun getActiveQueueFlow(type: MediaType): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    suspend fun getActiveQueue(type: MediaType): List<MediaFile>

    @Query("SELECT * FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 LIMIT :limit")
    suspend fun getActiveQueueBatch(type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY 
            CASE 
                WHEN dateTaken > 0 THEN 
                    CASE WHEN dateTaken < 10000000000 THEN dateTaken * 1000 ELSE dateTaken END
                ELSE 
                    CASE WHEN dateModified < 10000000000 THEN dateModified * 1000 ELSE dateModified END
            END DESC,
            dateModified DESC,
            id DESC
        LIMIT :limit
    """)
    suspend fun getActiveQueueBatchNewest(type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY 
            CASE 
                WHEN dateTaken > 0 THEN 
                    CASE WHEN dateTaken < 10000000000 THEN dateTaken * 1000 ELSE dateTaken END
                ELSE 
                    CASE WHEN dateModified < 10000000000 THEN dateModified * 1000 ELSE dateModified END
            END ASC,
            dateModified ASC,
            id ASC
        LIMIT :limit
    """)
    suspend fun getActiveQueueBatchOldest(type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY sizeBytes DESC, id DESC
        LIMIT :limit
    """)
    suspend fun getActiveQueueBatchLargest(type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY sizeBytes ASC, id ASC
        LIMIT :limit
    """)
    suspend fun getActiveQueueBatchSmallest(type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getActiveQueueBatchRandom(type: MediaType, limit: Int = 300): List<MediaFile>

    // Favorites queue
    @Query("SELECT * FROM media_files WHERE isFavorited = 1 AND isDeleted = 0 ORDER BY dateModified DESC")
    fun getFavoritesFlow(): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE isFavorited = 1 AND isDeleted = 0 ORDER BY dateModified DESC")
    suspend fun getFavorites(): List<MediaFile>

    // Category collage preview thumbnails (random sample up to limit, default 25)
    @Query("SELECT * FROM media_files WHERE mediaType = :type AND isDeleted = 0 ORDER BY RANDOM() LIMIT :limit")
    fun getPreviewThumbnailsFlow(type: MediaType, limit: Int = 25): Flow<List<MediaFile>>

    // Counts and stats
    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    fun getActiveCountFlow(type: MediaType): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type AND isDeleted = 0")
    fun getTotalCountFlow(type: MediaType): Flow<Int>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    fun getActiveTotalSizeFlow(type: MediaType): Flow<Long>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM media_files WHERE mediaType = :type AND isDeleted = 0")
    fun getTotalSizeFlow(type: MediaType): Flow<Long>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM media_files WHERE isDeleted = 1")
    fun getTotalDeletedSizeFlow(): Flow<Long>

    @Query("SELECT COUNT(*) FROM media_files WHERE isDeleted = 1")
    fun getTotalDeletedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_files WHERE isFavorited = 1 AND isDeleted = 0")
    fun getFavoritesCountFlow(): Flow<Int>

    // Format type / extension breakdown
    @Query("SELECT LOWER(extension) AS extension, COUNT(*) AS count, COALESCE(SUM(sizeBytes), 0) AS totalSizeBytes FROM media_files WHERE mediaType = :type AND isDeleted = 0 GROUP BY LOWER(extension) ORDER BY totalSizeBytes DESC")
    fun getFormatTypeStatsFlow(type: MediaType): Flow<List<FormatTypeStat>>

    // Action handlers
    @Query("UPDATE media_files SET isKept = 1, keptTimestamp = :timestamp WHERE id = :id")
    suspend fun markAsKept(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE media_files SET isFavorited = 1, isKept = 0 WHERE id = :id")
    suspend fun markAsFavorite(id: Long)

    @Query("UPDATE media_files SET isFavorited = 0 WHERE id = :id")
    suspend fun unfavorite(id: Long)

    @Query("UPDATE media_files SET isDeleted = 1, deletedTimestamp = :timestamp, originalPathBeforeTrash = :origPath, path = :trashPath WHERE id = :id")
    suspend fun markAsDeleted(id: Long, timestamp: Long, origPath: String, trashPath: String)

    @Query("UPDATE media_files SET isKept = :isKept, isFavorited = :isFav, isDeleted = :isDel, path = :path WHERE id = :id")
    suspend fun restoreFile(id: Long, isKept: Boolean, isFav: Boolean, isDel: Boolean, path: String)

    // Folder batch actions
    @Query("UPDATE media_files SET isKept = 1, keptTimestamp = :timestamp WHERE folderPath = :folderPath AND isDeleted = 0")
    suspend fun markFolderAsKept(folderPath: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE media_files SET isFavorited = 1, isKept = 0 WHERE folderPath = :folderPath AND isDeleted = 0")
    suspend fun markFolderAsFavorite(folderPath: String)

    // Reset session (clears isKept so they reappear in queue)
    @Query("UPDATE media_files SET isKept = 0, keptTimestamp = 0 WHERE isKept = 1 AND isDeleted = 0 AND isFavorited = 0")
    suspend fun resetKeptSession()

    @Query("UPDATE media_files SET isKept = 0, keptTimestamp = 0 WHERE id = :id")
    suspend fun unkeep(id: Long)

    @Query("SELECT * FROM media_files WHERE mediaType = :type AND isKept = 1 AND isDeleted = 0 ORDER BY keptTimestamp DESC, dateModified DESC")
    fun getKeptFilesFlow(type: MediaType): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE isKept = 1 AND isDeleted = 0 ORDER BY keptTimestamp DESC, dateModified DESC")
    fun getAllKeptFilesFlow(): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 1 AND isDeleted = 0 ORDER BY keptTimestamp DESC, dateModified DESC")
    fun getKeptFilesInFolderFlow(folderPath: String, type: MediaType): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getTrashFilesFlow(): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE isDeleted = 1")
    suspend fun getAllTrashFiles(): List<MediaFile>

    // Auto-reset older than threshold timestamp
    @Query("UPDATE media_files SET isKept = 0, keptTimestamp = 0 WHERE isKept = 1 AND keptTimestamp < :olderThanTimestamp AND isDeleted = 0 AND isFavorited = 0")
    suspend fun autoResetExpiredKept(olderThanTimestamp: Long): Int

    // Get files staged for trash purge
    @Query("SELECT * FROM media_files WHERE isDeleted = 1 AND deletedTimestamp < :olderThanTimestamp")
    suspend fun getFilesForPermanentPurge(olderThanTimestamp: Long): List<MediaFile>

    @Query("DELETE FROM media_files WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT DISTINCT folderPath FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    suspend fun getDistinctFolders(type: MediaType): List<String>

    @Query("SELECT * FROM media_files WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    suspend fun getFilesInFolder(folderPath: String, type: MediaType): List<MediaFile>

    @Query("SELECT * FROM media_files WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 LIMIT :limit")
    suspend fun getFilesInFolderBatch(folderPath: String, type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY 
            CASE 
                WHEN dateTaken > 0 THEN 
                    CASE WHEN dateTaken < 10000000000 THEN dateTaken * 1000 ELSE dateTaken END
                ELSE 
                    CASE WHEN dateModified < 10000000000 THEN dateModified * 1000 ELSE dateModified END
            END DESC,
            dateModified DESC,
            id DESC
        LIMIT :limit
    """)
    suspend fun getFilesInFolderBatchNewest(folderPath: String, type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY 
            CASE 
                WHEN dateTaken > 0 THEN 
                    CASE WHEN dateTaken < 10000000000 THEN dateTaken * 1000 ELSE dateTaken END
                ELSE 
                    CASE WHEN dateModified < 10000000000 THEN dateModified * 1000 ELSE dateModified END
            END ASC,
            dateModified ASC,
            id ASC
        LIMIT :limit
    """)
    suspend fun getFilesInFolderBatchOldest(folderPath: String, type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY sizeBytes DESC, id DESC
        LIMIT :limit
    """)
    suspend fun getFilesInFolderBatchLargest(folderPath: String, type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY sizeBytes ASC, id ASC
        LIMIT :limit
    """)
    suspend fun getFilesInFolderBatchSmallest(folderPath: String, type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        SELECT * FROM media_files 
        WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0 
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getFilesInFolderBatchRandom(folderPath: String, type: MediaType, limit: Int = 300): List<MediaFile>

    @Query("""
        UPDATE media_files 
        SET dateTaken = :dateTaken,
            dateModified = :dateModified
        WHERE path = :path AND (dateTaken <= 0 OR dateTaken = dateModified)
    """)
    suspend fun updateDatesIfMissing(path: String, dateTaken: Long, dateModified: Long)

    @Query("""
        SELECT folderPath, folderName, COUNT(*) AS fileCount, COALESCE(SUM(sizeBytes), 0) AS totalSizeBytes, MIN(path) AS coverPath
        FROM media_files 
        WHERE mediaType = :type AND isDeleted = 0
        GROUP BY folderPath 
        ORDER BY fileCount DESC
    """)
    suspend fun getFolderSummaries(type: MediaType): List<com.shred.app.data.model.FolderSummary>

    @Query("""
        SELECT LOWER(extension)
        FROM media_files
        WHERE folderPath = :folderPath AND mediaType = :type AND isDeleted = 0
        GROUP BY LOWER(extension)
        ORDER BY COUNT(*) DESC
        LIMIT 4
    """)
    suspend fun getFolderExtensions(folderPath: String, type: MediaType): List<String>

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type AND isDeleted = 0")
    suspend fun getTotalCount(type: MediaType): Int

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type AND isKept = 1 AND isDeleted = 0")
    suspend fun getKeptCount(type: MediaType): Int

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type AND isFavorited = 1 AND isDeleted = 0")
    suspend fun getFavoritedCount(type: MediaType): Int

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type AND isDeleted = 1")
    suspend fun getDeletedCount(type: MediaType): Int

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM media_files WHERE isDeleted = 1")
    suspend fun getTotalDeletedSize(): Long

    @Query("SELECT COUNT(*) FROM media_files WHERE folderPath = :folderPath AND mediaType = :type AND isDeleted = 0")
    suspend fun getFolderTotalCount(folderPath: String, type: MediaType): Int

    @Query("SELECT COUNT(*) FROM media_files WHERE folderPath = :folderPath AND mediaType = :type AND isKept = 1 AND isDeleted = 0")
    suspend fun getFolderKeptCount(folderPath: String, type: MediaType): Int

    @Query("SELECT COUNT(*) FROM media_files WHERE folderPath = :folderPath AND mediaType = :type AND isFavorited = 1 AND isDeleted = 0")
    suspend fun getFolderFavoritedCount(folderPath: String, type: MediaType): Int

    @Query("SELECT COUNT(*) FROM media_files WHERE folderPath = :folderPath AND mediaType = :type AND isDeleted = 1")
    suspend fun getFolderDeletedCount(folderPath: String, type: MediaType): Int

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM media_files WHERE folderPath = :folderPath AND isDeleted = 1")
    suspend fun getFolderDeletedSize(folderPath: String): Long
}
