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
    fun fromColorMode(value: ColorMode): String = value.name

    @TypeConverter
    fun toColorMode(value: String): ColorMode = try {
        ColorMode.valueOf(value)
    } catch (e: Exception) {
        ColorMode.SYSTEM
    }

    @TypeConverter
    fun fromScanMode(value: ScanMode): String = value.name

    @TypeConverter
    fun toScanMode(value: String): ScanMode = try {
        ScanMode.valueOf(value)
    } catch (e: Exception) {
        ScanMode.EVERY_LAUNCH
    }

    @TypeConverter
    fun fromScanScope(value: ScanScope): String = value.name

    @TypeConverter
    fun toScanScope(value: String): ScanScope = try {
        ScanScope.valueOf(value)
    } catch (e: Exception) {
        ScanScope.MEDIA_FOLDERS
    }
}
