package com.shred.app.data.repository

import com.shred.app.data.db.AppSettingsDao
import com.shred.app.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dao: AppSettingsDao
) {
    fun getSettingsFlow(): Flow<AppSettings> = dao.getSettingsFlow().map { it ?: AppSettings() }
    suspend fun getSettings(): AppSettings = dao.getSettings() ?: AppSettings().also {
        dao.insertOrUpdate(it)
    }

    suspend fun updateSettings(settings: AppSettings) {
        dao.insertOrUpdate(settings)
    }
}
