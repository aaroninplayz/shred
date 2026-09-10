package com.shred.app.util

import android.os.Environment
import android.util.Log
import com.shred.app.data.model.MediaFile
import com.shred.app.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TrashManager(
    private val repository: MediaRepository
) {
    companion object {
        private const val TAG = "TrashManager"
        private const val TRASH_DIR_NAME = ".PhoneCleaner/trash"
    }

    private fun getTrashDirectory(): File {
        val root = Environment.getExternalStorageDirectory()
        val trashDir = File(root, TRASH_DIR_NAME)
        if (!trashDir.exists()) {
            trashDir.mkdirs()
            // Create .nomedia so gallery apps ignore it
            val noMedia = File(trashDir, ".nomedia")
            if (!noMedia.exists()) {
                try {
                    noMedia.createNewFile()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not create .nomedia", e)
                }
            }
        }
        return trashDir
    }

    suspend fun moveToTrash(mediaFile: MediaFile): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(mediaFile.path)
            if (!sourceFile.exists()) return@withContext false

            val trashDir = getTrashDirectory()
            val targetFileName = "${System.currentTimeMillis()}_${mediaFile.fileName}"
            val targetFile = File(trashDir, targetFileName)

            val moved = sourceFile.renameTo(targetFile)
            if (moved) {
                repository.markAsDeleted(mediaFile.id, origPath = mediaFile.path, trashPath = targetFile.absolutePath)
                true
            } else {
                // Fallback copy & delete
                sourceFile.copyTo(targetFile, overwrite = true)
                sourceFile.delete()
                repository.markAsDeleted(mediaFile.id, origPath = mediaFile.path, trashPath = targetFile.absolutePath)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move file to trash: ${mediaFile.path}", e)
            false
        }
    }

    suspend fun restoreFromTrash(mediaFile: MediaFile): Boolean = withContext(Dispatchers.IO) {
        try {
            val origPath = mediaFile.originalPathBeforeTrash ?: return@withContext false
            val currentTrashFile = File(mediaFile.path)
            val targetOriginalFile = File(origPath)

            targetOriginalFile.parentFile?.mkdirs()
            val moved = currentTrashFile.renameTo(targetOriginalFile)
            if (moved || (!currentTrashFile.exists() && targetOriginalFile.exists())) {
                repository.restoreFile(mediaFile, origPath)
                true
            } else {
                currentTrashFile.copyTo(targetOriginalFile, overwrite = true)
                currentTrashFile.delete()
                repository.restoreFile(mediaFile, origPath)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore file from trash: ${mediaFile.path}", e)
            false
        }
    }

    suspend fun purgeExpiredFiles(retentionHours: Int): Int = withContext(Dispatchers.IO) {
        val expiredFiles = repository.getFilesForPermanentPurge(retentionHours)
        var purgedCount = 0
        for (file in expiredFiles) {
            try {
                val diskFile = File(file.path)
                if (diskFile.exists()) {
                    diskFile.delete()
                }
                repository.deletePermanently(file.id)
                purgedCount++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to purge file: ${file.path}", e)
            }
        }
        purgedCount
    }
}
