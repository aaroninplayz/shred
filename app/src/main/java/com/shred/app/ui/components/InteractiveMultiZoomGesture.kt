package com.shred.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Continuous interactive multi-zoom gesture detector for 2-finger pinch.
 * When fingers pinch closer together (< 0.78 scale), onZoomOut is triggered (zooming out to Grid / more columns).
 * When fingers spread further apart (> 1.25 scale), onZoomIn is triggered (zooming in to fewer columns / Cards).
 * When 1 finger is down, native list/grid scroll operates with zero interference.
 */
fun Modifier.interactiveMultiZoomGesture(
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onScaleChange: (Float) -> Unit = {},
    onGestureEnd: () -> Unit = {}
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var initDist: Float? = null

        do {
            val event = awaitPointerEvent()
            val activePointers = event.changes.filter { it.pressed }

            if (activePointers.size >= 2) {
                val p0 = activePointers[0].position
                val p1 = activePointers[1].position
                val dist = (p0 - p1).getDistance()

                if (initDist == null) {
                    initDist = dist
                } else {
                    val base = initDist ?: dist
                    if (base > 30f) {
                        val currentScale = dist / base
                        onScaleChange(currentScale)

                        if (currentScale < 0.78f) {
                            activePointers.forEach { it.consume() }
                            initDist = dist
                            onZoomOut()
                        } else if (currentScale > 1.25f) {
                            activePointers.forEach { it.consume() }
                            initDist = dist
                            onZoomIn()
                        }
                    }
                }
            } else if (activePointers.size < 2) {
                initDist = null
            }
        } while (event.changes.any { it.pressed })

        onGestureEnd()
    }
}
