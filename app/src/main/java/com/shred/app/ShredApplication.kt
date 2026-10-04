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

import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ShredApplication : Application(), ImageLoaderFactory {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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
        try {
            TrashCleanerWorker.schedulePeriodic(this)
        } catch (e: Exception) {
            android.util.Log.e("ShredApplication", "Could not schedule TrashCleanerWorker", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                coil.memory.MemoryCache.Builder(this)
                    .maxSizePercent(0.25)  // 25% of available heap
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)  // 100MB disk cache
                    .build()
            }
            .crossfade(200)
            .build()
    }

    fun triggerBackgroundScan() {
        scanner.startBackgroundScan(applicationScope, settingsRepository)
    }
}

