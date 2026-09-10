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
    MONOCHROME,
    NEON,
    NEO_BRUTALISM
}

enum class SortOrder {
    OLDEST_FIRST,
    NEWEST_FIRST,
    LARGEST_FIRST,
    SMALLEST_FIRST,
    RANDOM
}

enum class ProgressBarStyle {
    MINIMAL_COUNTER,
    STORAGE_FREED,
    RATIO_BAR,
    NUMBERS_ONLY,
    HIDDEN
}

enum class LoadingAnimation {
    TYPEWRITER,
    FADE_IN,
    SLIDE_IN
}

enum class TransitionStyle {
    FADE_TO_BLACK,
    DIRECT,
    CROSS_DISSOLVE
}

enum class ScanMode {
    EVERY_LAUNCH,
    INCREMENTAL,
    MANUAL
}
