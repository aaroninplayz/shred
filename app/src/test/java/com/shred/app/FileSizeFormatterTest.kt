package com.shred.app

import com.shred.app.util.FileSizeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class FileSizeFormatterTest {

    @Test
    fun testFormatBytes() {
        assertEquals("0 B", FileSizeFormatter.format(0))
        assertEquals("500 B", FileSizeFormatter.format(500))
        assertEquals("1 KB", FileSizeFormatter.format(1024))
        assertEquals("1.5 MB", FileSizeFormatter.format((1.5 * 1024 * 1024).toLong()))
        assertEquals("2.4 GB", FileSizeFormatter.format((2.4 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testFormatDuration() {
        assertEquals("0:00", FileSizeFormatter.formatDuration(0))
        assertEquals("0:45", FileSizeFormatter.formatDuration(45000))
        assertEquals("2:30", FileSizeFormatter.formatDuration(150000))
        assertEquals("65:05", FileSizeFormatter.formatDuration(3905000))
    }
}
