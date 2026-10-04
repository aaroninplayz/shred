package com.shred.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: ThemeMode = ThemeMode.MINIMALISM,
    val colorMode: ColorMode = ColorMode.SYSTEM,
    val sortOrder: SortOrder = SortOrder.OLDEST_FIRST,
    val trashRetentionHours: Int = 1, // 1 hour default (1 to 1440 hours / 60 days)
    val autoResetEnabled: Boolean = true,
    val autoResetDays: Int = 14, // 7 to 30 days
    val hapticsEnabled: Boolean = true,
    val hapticIntensity: Float = 0.8f, // 0.1 to 1.0
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.7f, // 0.0 to 1.0
    val scanMode: ScanMode = ScanMode.EVERY_LAUNCH,
    val scanScope: ScanScope = ScanScope.MEDIA_FOLDERS,
    val minFileSizeBytes: Long = 0,
    val excludedFolders: String = "[]", // JSON array of folder paths
    val loadingPhrases: String = "[\"SHREDDING DISK ENTROPY...\", \"RECLAIMING LOST MEGABYTES...\", \"PURGING ARTIFACTS...\", \"CRAWLING HIDDEN SECTORS...\", \"ZEROING IN ON JUNK...\"]"
)
