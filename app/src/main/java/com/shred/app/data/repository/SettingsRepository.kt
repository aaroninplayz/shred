package com.shred.app.data.repository

import com.shred.app.data.db.AppSettingsDao
import com.shred.app.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dao: AppSettingsDao
) {
    fun getSettingsFlow(): Flow<AppSettings> = dao.getSettingsFlow().map {
        it ?: AppSettings()
    }
    suspend fun getSettings(): AppSettings {
        val existing = dao.getSettings()
        return if (existing == null) {
            AppSettings().also { dao.insertOrUpdate(it) }
        } else {
            var updated = existing
            if (existing.themeMode == com.shred.app.data.model.ThemeMode.BRUTALISM) {
                updated = updated.copy(themeMode = com.shred.app.data.model.ThemeMode.MINIMALISM)
            }
            if (existing.trashRetentionHours == 168 || existing.trashRetentionHours == 2) {
                updated = updated.copy(trashRetentionHours = 1)
            }
            if (updated != existing) {
                dao.insertOrUpdate(updated)
            }
            updated
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        dao.insertOrUpdate(settings)
    }
}
