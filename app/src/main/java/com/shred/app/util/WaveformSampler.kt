package com.shred.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.math.abs
import kotlin.random.Random

object WaveformSampler {

    suspend fun sampleWaveform(filePath: String, barCount: Int = 40): List<Float> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists() || file.length() < 100) {
                return@withContext generateFallbackBars(filePath, barCount)
            }

            val fileSize = file.length()
            val step = (fileSize / barCount).coerceAtLeast(1)
            val buffer = ByteArray(256)
            val amplitudes = mutableListOf<Float>()

            FileInputStream(file).use { fis ->
                for (i in 0 until barCount) {
                    val offset = i * step
                    fis.channel.position(offset)
                    val read = fis.read(buffer)
                    if (read > 0) {
                        var sum = 0L
                        for (b in 0 until read) {
                            sum += abs(buffer[b].toInt())
                        }
                        val avg = (sum.toFloat() / read) / 128.0f
                        amplitudes.add(avg.coerceIn(0.15f, 1.0f))
                    } else {
                        amplitudes.add(0.2f)
                    }
                }
            }
            amplitudes
        } catch (e: Exception) {
            generateFallbackBars(filePath, barCount)
        }
    }

    private fun generateFallbackBars(seedStr: String, count: Int): List<Float> {
        val seed = seedStr.hashCode().toLong()
        val rng = Random(seed)
        return List(count) {
            rng.nextFloat().coerceIn(0.15f, 1.0f)
        }
    }
}
