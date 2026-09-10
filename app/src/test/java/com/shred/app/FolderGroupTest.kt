package com.shred.app

import com.shred.app.data.model.FolderGroup
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderGroupTest {

    @Test
    fun testFolderGroupAggregation() {
        val files = listOf(
            MediaFile(
                id = 1,
                path = "/sdcard/CallRecordings/call1.mp3",
                fileName = "call1.mp3",
                folderPath = "/sdcard/CallRecordings",
                folderName = "CallRecordings",
                sizeBytes = 1024 * 1024,
                dateModified = 1000L,
                extension = "mp3",
                mediaType = MediaType.AUDIO
            ),
            MediaFile(
                id = 2,
                path = "/sdcard/CallRecordings/call2.mp3",
                fileName = "call2.mp3",
                folderPath = "/sdcard/CallRecordings",
                folderName = "CallRecordings",
                sizeBytes = 2 * 1024 * 1024,
                dateModified = 2000L,
                extension = "mp3",
                mediaType = MediaType.AUDIO
            )
        )

        val group = FolderGroup(
            folderPath = "/sdcard/CallRecordings",
            folderName = "CallRecordings",
            mediaType = MediaType.AUDIO,
            fileCount = files.size,
            totalSizeBytes = files.sumOf { it.sizeBytes },
            files = files
        )

        assertEquals(2, group.fileCount)
        assertEquals(3 * 1024 * 1024L, group.totalSizeBytes)
        assertEquals("CallRecordings", group.folderName)
    }
}
