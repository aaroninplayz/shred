package com.shred.app.ui.screens.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

@Composable
fun ZoomableBox(
    isZoomMode: Boolean,
    onToggleZoom: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    LaunchedEffect(isZoomMode) {
        if (!isZoomMode) {
            scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
            offsetXAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
            offsetYAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
        } else {
            scaleAnim.animateTo(2.6f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isZoomMode) {
                if (isZoomMode) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scope.launch {
                            val newScale = (scaleAnim.value * zoom).coerceIn(0.92f, 6.0f)
                            scaleAnim.snapTo(newScale)

                            val maxOffsetX = (size.width * (newScale - 1f) / 2f).coerceAtLeast(0f)
                            val maxOffsetY = (size.height * (newScale - 1f) / 2f).coerceAtLeast(0f)

                            val nextX = (offsetXAnim.value + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                            val nextY = (offsetYAnim.value + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            offsetXAnim.snapTo(nextX)
                            offsetYAnim.snapTo(nextY)
                        }
                    }
                }
            }
            .pointerInput(isZoomMode) {
                detectTapGestures(
                    onDoubleTap = {
                        onToggleZoom()
                    }
                )
            }
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                translationX = offsetXAnim.value
                translationY = offsetYAnim.value
            }
    ) {
        content()
    }
}
