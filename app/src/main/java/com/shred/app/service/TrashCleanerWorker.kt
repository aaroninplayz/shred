package com.shred.app.service

import android.content.Context
import androidx.work.*
import com.shred.app.data.db.AppDatabase
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import com.shred.app.util.TrashManager
import java.util.concurrent.TimeUnit

class TrashCleanerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val settingsRepo = SettingsRepository(db.appSettingsDao())
            val mediaRepo = MediaRepository(db.mediaFileDao())
            val trashManager = TrashManager(mediaRepo)

            val settings = settingsRepo.getSettings()
            trashManager.purgeExpiredFiles(settings.trashRetentionHours)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "TrashCleanerWorker"

        fun schedulePeriodic(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<TrashCleanerWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
