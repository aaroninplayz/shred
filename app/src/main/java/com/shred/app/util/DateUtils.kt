package com.shred.app.util

import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.SortOrder
import java.util.Calendar

object DateUtils {

    private val DATE_TIME_COMPACT = Regex("""(?i)(?:IMG_|PXL_|VID_|Screenshot_|WP_)?(\d{4})(\d{2})(\d{2})[-_](\d{2})(\d{2})(\d{2})""")
    private val DATE_TIME_HYPHEN = Regex("""(?i)(?:IMG_|PXL_|VID_|Screenshot_|WP_)?(\d{4})-(\d{2})-(\d{2})[-_](\d{2})-(\d{2})-(\d{2})""")
    private val DATE_ONLY = Regex("""(?i)(?:IMG_|PXL_|VID_|Screenshot_|WP_)?(\d{4})(\d{2})(\d{2})""")

    /**
     * Parses standard camera and system image filenames into epoch milliseconds.
     * Examples: IMG_20241021_233000.jpg, 20241021_233000.jpg, Screenshot_20241021-233000.png, PXL_20241021_...
     */
    fun parseDateFromFilename(fileName: String): Long {
        try {
            val matchCompact = DATE_TIME_COMPACT.find(fileName)
            if (matchCompact != null) {
                val (y, m, d, h, min, s) = matchCompact.destructured
                val cal = Calendar.getInstance()
                cal.set(y.toInt(), m.toInt() - 1, d.toInt(), h.toInt(), min.toInt(), s.toInt())
                cal.set(Calendar.MILLISECOND, 0)
                val time = cal.timeInMillis
                if (time in 946684800000L..4102444800000L) { // 2000 to 2100
                    return time
                }
            }

            val matchHyphen = DATE_TIME_HYPHEN.find(fileName)
            if (matchHyphen != null) {
                val (y, m, d, h, min, s) = matchHyphen.destructured
                val cal = Calendar.getInstance()
                cal.set(y.toInt(), m.toInt() - 1, d.toInt(), h.toInt(), min.toInt(), s.toInt())
                cal.set(Calendar.MILLISECOND, 0)
                val time = cal.timeInMillis
                if (time in 946684800000L..4102444800000L) {
                    return time
                }
            }

            val matchDateOnly = DATE_ONLY.find(fileName)
            if (matchDateOnly != null) {
                val (y, m, d) = matchDateOnly.destructured
                val cal = Calendar.getInstance()
                cal.set(y.toInt(), m.toInt() - 1, d.toInt(), 12, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val time = cal.timeInMillis
                if (time in 946684800000L..4102444800000L) {
                    return time
                }
            }
        } catch (_: Exception) {}
        return 0L
    }

    /**
     * Resolves the canonical timestamp for ordering.
     * Prioritizes camera capture time (dateTaken), then filename timestamp (for camera shots),
     * and falls back to latest modified timestamp.
     */
    fun getEffectiveTimestamp(file: MediaFile): Long {
        var ts = if (file.dateTaken > 0L) {
            if (file.dateTaken < 10_000_000_000L) file.dateTaken * 1000L else file.dateTaken
        } else 0L

        // If dateTaken was missing or defaulted to dateModified during previous scan, check filename pattern
        if (ts <= 0L || file.dateTaken == file.dateModified) {
            val parsed = parseDateFromFilename(file.fileName)
            if (parsed > 0L) {
                ts = parsed
            }
        }

        // Final fallback to dateModified
        if (ts <= 0L) {
            ts = if (file.dateModified > 0L) {
                if (file.dateModified < 10_000_000_000L) file.dateModified * 1000L else file.dateModified
            } else 0L
        }
        return ts
    }

    /**
     * Strictly sorts media files according to the chosen SortOrder.
     */
    fun sortMediaFiles(files: List<MediaFile>, sortOrder: SortOrder): List<MediaFile> {
        return when (sortOrder) {
            SortOrder.NEWEST_FIRST -> files.sortedWith(
                compareByDescending<MediaFile> { getEffectiveTimestamp(it) }
                    .thenByDescending { it.dateModified }
                    .thenByDescending { it.id }
            )
            SortOrder.OLDEST_FIRST -> files.sortedWith(
                compareBy<MediaFile> { getEffectiveTimestamp(it) }
                    .thenBy { it.dateModified }
                    .thenBy { it.id }
            )
            SortOrder.LARGEST_FIRST -> files.sortedWith(
                compareByDescending<MediaFile> { it.sizeBytes }
                    .thenByDescending { it.id }
            )
            SortOrder.SMALLEST_FIRST -> files.sortedWith(
                compareBy<MediaFile> { it.sizeBytes }
                    .thenBy { it.id }
            )
            SortOrder.RANDOM -> files.shuffled()
        }
    }
}
