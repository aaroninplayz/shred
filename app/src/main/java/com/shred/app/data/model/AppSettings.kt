package com.shred.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: ThemeMode = ThemeMode.MONOCHROME,
    val sortOrder: SortOrder = SortOrder.OLDEST_FIRST,
    val progressBarStyle: ProgressBarStyle = ProgressBarStyle.MINIMAL_COUNTER,
    val trashRetentionHours: Int = 24 * 7, // 7 days default (1 to 1440 hours / 60 days)
    val autoResetEnabled: Boolean = true,
    val autoResetDays: Int = 14, // 7 to 30 days
    val hapticsEnabled: Boolean = true,
    val hapticIntensity: Float = 0.8f, // 0.1 to 1.0
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.7f, // 0.0 to 1.0
    val loadingAnimation: LoadingAnimation = LoadingAnimation.TYPEWRITER,
    val transitionStyle: TransitionStyle = TransitionStyle.FADE_TO_BLACK,
    val scanMode: ScanMode = ScanMode.EVERY_LAUNCH,
    val minFileSizeBytes: Long = 0,
    val excludedFolders: String = "[]", // JSON array of folder paths
    val loadingPhrases: String = "[\"SHREDDING DISK ENTROPY...\", \"RECLAIMING LOST MEGABYTES...\", \"PURGING ARTIFACTS...\", \"CRAWLING HIDDEN SECTORS...\", \"ZEROING IN ON JUNK...\"]"
)
