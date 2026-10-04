package com.shred.app.data.model

enum class MediaType {
    PHOTO,
    VIDEO,
    AUDIO
}

enum class ActionType {
    KEEP,
    DELETE,
    FAVORITE
}

enum class ThemeMode {
    BRUTALISM,
    MINIMALISM,
    GLASSMORPHISM
}

enum class SortOrder {
    OLDEST_FIRST,
    NEWEST_FIRST,
    LARGEST_FIRST,
    SMALLEST_FIRST,
    RANDOM
}

enum class ColorMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class ScanMode {
    EVERY_LAUNCH,
    INCREMENTAL,
    MANUAL
}

enum class ScanScope {
    MEDIA_FOLDERS,
    DEEP_STORAGE
}

