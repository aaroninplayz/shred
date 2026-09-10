package com.shred.app.data.db

import androidx.room.*
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

    // Favorites queue
    @Query("SELECT * FROM media_files WHERE isFavorited = 1 AND isDeleted = 0 ORDER BY dateModified DESC")
    fun getFavoritesFlow(): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE isFavorited = 1 AND isDeleted = 0 ORDER BY dateModified DESC")
    suspend fun getFavorites(): List<MediaFile>

    // Counts and stats
    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    fun getActiveCountFlow(type: MediaType): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = :type")
    fun getTotalCountFlow(type: MediaType): Flow<Int>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM media_files WHERE mediaType = :type AND isKept = 0 AND isFavorited = 0 AND isDeleted = 0")
    fun getActiveTotalSizeFlow(type: MediaType): Flow<Long>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM media_files WHERE isDeleted = 1")
    fun getTotalDeletedSizeFlow(): Flow<Long>

    @Query("SELECT COUNT(*) FROM media_files WHERE isDeleted = 1")
    fun getTotalDeletedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_files WHERE isFavorited = 1 AND isDeleted = 0")
    fun getFavoritesCountFlow(): Flow<Int>

    // Action handlers
    @Query("UPDATE media_files SET isKept = 1, keptTimestamp = :timestamp WHERE id = :id")
    suspend fun markAsKept(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE media_files SET isFavorited = 1, isKept = 0 WHERE id = :id")
    suspend fun markAsFavorite(id: Long)

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
}
