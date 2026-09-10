package com.shred.app.scanner

import com.shred.app.data.model.MediaType

object FileExtensionRegistry {
    val PHOTO_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif",
        "svg", "tiff", "tif", "raw", "cr2", "nef", "arw", "dng"
    )

    val VIDEO_EXTENSIONS = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "3gp",
        "m4v", "mpeg", "mpg", "ts"
    )

    val AUDIO_EXTENSIONS = setOf(
        "mp3", "aac", "flac", "wav", "ogg", "m4a", "wma", "opus",
        "amr", "aiff"
    )

    fun getMediaType(extension: String): MediaType? {
        val ext = extension.lowercase().removePrefix(".")
        return when {
            PHOTO_EXTENSIONS.contains(ext) -> MediaType.PHOTO
            VIDEO_EXTENSIONS.contains(ext) -> MediaType.VIDEO
            AUDIO_EXTENSIONS.contains(ext) -> MediaType.AUDIO
            else -> null
        }
    }
}
