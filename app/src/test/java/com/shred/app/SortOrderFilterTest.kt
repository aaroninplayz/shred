package com.shred.app

import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.data.model.SortOrder
import com.shred.app.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SortOrderFilterTest {

    private fun createDummy(
        id: Long,
        name: String,
        sizeBytes: Long,
        dateModified: Long,
        dateTaken: Long = 0L
    ) = MediaFile(
        id = id,
        path = "/storage/emulated/0/DCIM/Camera/$name",
        fileName = name,
        folderPath = "/storage/emulated/0/DCIM/Camera",
        folderName = "Camera",
        sizeBytes = sizeBytes,
        dateModified = dateModified,
        dateTaken = dateTaken,
        extension = "jpg",
        mediaType = MediaType.PHOTO
    )

    @Test
    fun testFilenameDateParsing() {
        val t1 = DateUtils.parseDateFromFilename("IMG_20240501_143000.jpg")
        val t2 = DateUtils.parseDateFromFilename("IMG_20240501_153000.jpg")
        assertTrue("Later time should produce greater epoch millis", t2 > t1)

        val px = DateUtils.parseDateFromFilename("PXL_20231120_081234.jpg")
        assertTrue("Pixel photo should parse valid timestamp", px > 0L)

        val ss = DateUtils.parseDateFromFilename("Screenshot_20240214-180000.png")
        assertTrue("Screenshot should parse valid timestamp", ss > 0L)
    }

    @Test
    fun testNewestFirstPrioritizesCameraCaptureDate() {
        // Photo 1: Captured 2024-05-10, modified earlier
        val photo1 = createDummy(1, "IMG_20240510_100000.jpg", 2000L, 1715000000000L, 1715335200000L)
        // Photo 2: Captured 2024-05-12 (newer capture!)
        val photo2 = createDummy(2, "IMG_20240512_100000.jpg", 1500L, 1715100000000L, 1715508000000L)
        // Photo 3: Captured 2024-05-01 (oldest capture)
        val photo3 = createDummy(3, "IMG_20240501_100000.jpg", 3000L, 1714500000000L, 1714557600000L)

        val sorted = DateUtils.sortMediaFiles(listOf(photo1, photo3, photo2), SortOrder.NEWEST_FIRST)
        assertEquals(listOf(2L, 1L, 3L), sorted.map { it.id })
    }

    @Test
    fun testOldestFirstOrdersChronologicallyAscending() {
        val photo1 = createDummy(1, "photo_2021.jpg", 1000L, 1610000000000L, 1610000000000L)
        val photo2 = createDummy(2, "photo_2023.jpg", 1000L, 1680000000000L, 1680000000000L)
        val photo3 = createDummy(3, "photo_2020.jpg", 1000L, 1580000000000L, 1580000000000L)

        val sorted = DateUtils.sortMediaFiles(listOf(photo1, photo2, photo3), SortOrder.OLDEST_FIRST)
        assertEquals(listOf(3L, 1L, 2L), sorted.map { it.id })
    }

    @Test
    fun testLargestFirstOrdersByFileSizeDescending() {
        val small = createDummy(1, "small.jpg", 1024L, 1000L)
        val huge = createDummy(2, "huge.jpg", 100 * 1024 * 1024L, 1000L)
        val medium = createDummy(3, "medium.jpg", 10 * 1024 * 1024L, 1000L)

        val sorted = DateUtils.sortMediaFiles(listOf(small, medium, huge), SortOrder.LARGEST_FIRST)
        assertEquals(listOf(2L, 3L, 1L), sorted.map { it.id })
    }

    @Test
    fun testSmallestFirstOrdersByFileSizeAscending() {
        val small = createDummy(1, "small.jpg", 1024L, 1000L)
        val huge = createDummy(2, "huge.jpg", 100 * 1024 * 1024L, 1000L)
        val medium = createDummy(3, "medium.jpg", 10 * 1024 * 1024L, 1000L)

        val sorted = DateUtils.sortMediaFiles(listOf(huge, small, medium), SortOrder.SMALLEST_FIRST)
        assertEquals(listOf(1L, 3L, 2L), sorted.map { it.id })
    }
}
