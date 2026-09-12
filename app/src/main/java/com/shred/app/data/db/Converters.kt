package com.shred.app.data.db

import androidx.room.TypeConverter
import com.shred.app.data.model.*

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = try {
        MediaType.valueOf(value)
    } catch (e: Exception) {
        MediaType.PHOTO
    }

    @TypeConverter
    fun fromThemeMode(value: ThemeMode): String = value.name

    @TypeConverter
    fun toThemeMode(value: String): ThemeMode = try {
        ThemeMode.valueOf(value)
    } catch (e: Exception) {
        ThemeMode.BRUTALISM
    }

    @TypeConverter
    fun fromSortOrder(value: SortOrder): String = value.name

    @TypeConverter
    fun toSortOrder(value: String): SortOrder = try {
        SortOrder.valueOf(value)
    } catch (e: Exception) {
        SortOrder.OLDEST_FIRST
    }

    @TypeConverter
    fun fromProgressBarStyle(value: ProgressBarStyle): String = value.name

    @TypeConverter
    fun toProgressBarStyle(value: String): ProgressBarStyle = try {
        ProgressBarStyle.valueOf(value)
    } catch (e: Exception) {
        ProgressBarStyle.MINIMAL_COUNTER
    }

    @TypeConverter
    fun fromLoadingAnimation(value: LoadingAnimation): String = value.name

    @TypeConverter
    fun toLoadingAnimation(value: String): LoadingAnimation = try {
        LoadingAnimation.valueOf(value)
    } catch (e: Exception) {
        LoadingAnimation.TYPEWRITER
    }

    @TypeConverter
    fun fromTransitionStyle(value: TransitionStyle): String = value.name

    @TypeConverter
    fun toTransitionStyle(value: String): TransitionStyle = try {
        TransitionStyle.valueOf(value)
    } catch (e: Exception) {
        TransitionStyle.FADE_TO_BLACK
    }

    @TypeConverter
    fun fromScanMode(value: ScanMode): String = value.name

    @TypeConverter
    fun toScanMode(value: String): ScanMode = try {
        ScanMode.valueOf(value)
    } catch (e: Exception) {
        ScanMode.EVERY_LAUNCH
    }
}
