package com.shred.app

import com.shred.app.data.model.MediaType
import com.shred.app.scanner.FileExtensionRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FileExtensionRegistryTest {

    @Test
    fun testPhotoExtensions() {
        assertEquals(MediaType.PHOTO, FileExtensionRegistry.getMediaType("jpg"))
        assertEquals(MediaType.PHOTO, FileExtensionRegistry.getMediaType("JPEG"))
        assertEquals(MediaType.PHOTO, FileExtensionRegistry.getMediaType(".png"))
        assertEquals(MediaType.PHOTO, FileExtensionRegistry.getMediaType("heic"))
        assertEquals(MediaType.PHOTO, FileExtensionRegistry.getMediaType("raw"))
        assertEquals(MediaType.PHOTO, FileExtensionRegistry.getMediaType("dng"))
    }

    @Test
    fun testVideoExtensions() {
        assertEquals(MediaType.VIDEO, FileExtensionRegistry.getMediaType("mp4"))
        assertEquals(MediaType.VIDEO, FileExtensionRegistry.getMediaType("MKV"))
        assertEquals(MediaType.VIDEO, FileExtensionRegistry.getMediaType(".webm"))
        assertEquals(MediaType.VIDEO, FileExtensionRegistry.getMediaType("mov"))
        assertEquals(MediaType.VIDEO, FileExtensionRegistry.getMediaType("3gp"))
    }

    @Test
    fun testAudioExtensions() {
        assertEquals(MediaType.AUDIO, FileExtensionRegistry.getMediaType("mp3"))
        assertEquals(MediaType.AUDIO, FileExtensionRegistry.getMediaType("FLAC"))
        assertEquals(MediaType.AUDIO, FileExtensionRegistry.getMediaType(".opus"))
        assertEquals(MediaType.AUDIO, FileExtensionRegistry.getMediaType("wav"))
        assertEquals(MediaType.AUDIO, FileExtensionRegistry.getMediaType("m4a"))
    }

    @Test
    fun testUnknownExtensions() {
        assertNull(FileExtensionRegistry.getMediaType("txt"))
        assertNull(FileExtensionRegistry.getMediaType("pdf"))
        assertNull(FileExtensionRegistry.getMediaType("exe"))
    }
}
