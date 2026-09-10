package com.shred.app

import com.shred.app.util.WaveformSampler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveformSamplerTest {

    @Test
    fun testWaveformSamplingFallback() = runBlocking {
        val bars = WaveformSampler.sampleWaveform("non_existent_file.mp3", barCount = 40)
        assertEquals(40, bars.size)
        bars.forEach { amplitude ->
            assertTrue("Amplitude $amplitude should be >= 0.15f", amplitude >= 0.15f)
            assertTrue("Amplitude $amplitude should be <= 1.0f", amplitude <= 1.0f)
        }
    }
}
