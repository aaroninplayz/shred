package com.shred.app.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.shred.app.data.model.ActionType

class HapticFeedbackHelper(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerActionHaptic(actionType: ActionType, enabled: Boolean, intensity: Float) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return

        val scaledAmplitude = (255 * intensity.coerceIn(0.1f, 1.0f)).toInt().coerceIn(1, 255)

        try {
            when (actionType) {
                ActionType.DELETE -> {
                    // Heavy double crunch
                    val timings = longArrayOf(0, 35, 20, 50)
                    val amplitudes = intArrayOf(0, scaledAmplitude, 0, (scaledAmplitude * 0.9f).toInt())
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                }
                ActionType.KEEP -> {
                    // Soft crisp click
                    val effect = VibrationEffect.createOneShot(20, (scaledAmplitude * 0.6f).toInt().coerceAtLeast(1))
                    vibrator.vibrate(effect)
                }
                ActionType.FAVORITE -> {
                    // Triple rising pulse
                    val timings = longArrayOf(0, 15, 15, 25, 15, 35)
                    val amplitudes = intArrayOf(
                        0,
                        (scaledAmplitude * 0.4f).toInt().coerceAtLeast(1),
                        0,
                        (scaledAmplitude * 0.7f).toInt().coerceAtLeast(1),
                        0,
                        scaledAmplitude
                    )
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                }
            }
        } catch (e: Exception) {
            // Silently ignore on unsupported hardware
        }
    }

    fun triggerTick(enabled: Boolean, intensity: Float = 0.5f) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            val amp = (255 * intensity).toInt().coerceIn(1, 255)
            val effect = VibrationEffect.createOneShot(10, amp)
            vibrator.vibrate(effect)
        } catch (e: Exception) {}
    }
}
