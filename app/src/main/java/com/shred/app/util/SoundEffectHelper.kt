package com.shred.app.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.shred.app.data.model.ActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundEffectHelper {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playActionSound(actionType: ActionType, enabled: Boolean, volume: Float) {
        if (!enabled || volume <= 0f) return
        scope.launch {
            when (actionType) {
                ActionType.DELETE -> playShredderSound(volume)
                ActionType.KEEP -> playKeepClick(volume)
                ActionType.FAVORITE -> playFavoriteChime(volume)
            }
        }
    }

    private fun playShredderSound(volume: Float) {
        val sampleRate = 22050
        val durationMs = 180
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        val vol = volume.coerceIn(0f, 1f)
        for (i in 0 until numSamples) {
            // White noise burst with low frequency motor hum
            val noise = (Random.nextFloat() * 2f - 1f) * 0.7f
            val hum = sin(2.0 * Math.PI * i * 65.0 / sampleRate).toFloat() * 0.3f
            val envelope = (1.0f - (i.toFloat() / numSamples)) // decaying envelope
            val sample = (noise + hum) * envelope * vol * Short.MAX_VALUE
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playRawPcm(buffer, sampleRate)
    }

    private fun playKeepClick(volume: Float) {
        val sampleRate = 22050
        val durationMs = 40
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        val vol = volume.coerceIn(0f, 1f)
        for (i in 0 until numSamples) {
            val freq = 1200.0 - (i.toFloat() / numSamples) * 600.0
            val tone = sin(2.0 * Math.PI * i * freq / sampleRate).toFloat()
            val envelope = (1.0f - (i.toFloat() / numSamples))
            val sample = tone * envelope * vol * Short.MAX_VALUE
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playRawPcm(buffer, sampleRate)
    }

    private fun playFavoriteChime(volume: Float) {
        val sampleRate = 22050
        val durationMs = 220
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        val vol = volume.coerceIn(0f, 1f)
        val freqs = doubleArrayOf(587.33, 880.0, 1174.66) // D5, A5, D6
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val tone1 = sin(2.0 * Math.PI * i * freqs[0] / sampleRate).toFloat()
            val tone2 = sin(2.0 * Math.PI * i * freqs[1] / sampleRate).toFloat()
            val tone3 = sin(2.0 * Math.PI * i * freqs[2] / sampleRate).toFloat()
            val mixed = (tone1 * 0.4f + tone2 * 0.4f + tone3 * 0.2f)
            val envelope = (1.0f - progress)
            val sample = mixed * envelope * vol * Short.MAX_VALUE
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playRawPcm(buffer, sampleRate)
    }

    private fun playRawPcm(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after playback finishes
            scope.launch {
                kotlinx.coroutines.delay(300)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            // Ignore audio initialization errors
        }
    }
}
