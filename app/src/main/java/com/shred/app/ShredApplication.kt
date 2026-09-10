package com.shred.app

import android.app.Application
import com.shred.app.data.db.AppDatabase
import com.shred.app.data.repository.MediaRepository
import com.shred.app.data.repository.SettingsRepository
import com.shred.app.scanner.MediaScanner
import com.shred.app.service.TrashCleanerWorker
import com.shred.app.util.HapticFeedbackHelper
import com.shred.app.util.SoundEffectHelper
import com.shred.app.util.TrashManager

class ShredApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var mediaRepository: MediaRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var scanner: MediaScanner
        private set

    lateinit var trashManager: TrashManager
        private set

    lateinit var hapticHelper: HapticFeedbackHelper
        private set

    lateinit var soundHelper: SoundEffectHelper
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        mediaRepository = MediaRepository(database.mediaFileDao())
        settingsRepository = SettingsRepository(database.appSettingsDao())
        scanner = MediaScanner(this, mediaRepository)
        trashManager = TrashManager(mediaRepository)
        hapticHelper = HapticFeedbackHelper(this)
        soundHelper = SoundEffectHelper()

        // Schedule periodic background trash cleanup
        TrashCleanerWorker.schedulePeriodic(this)
    }
}
