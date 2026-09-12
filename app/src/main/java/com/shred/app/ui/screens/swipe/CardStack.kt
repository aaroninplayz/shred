package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.MediaFile
import com.shred.app.ui.theme.LocalShredColors

import androidx.compose.runtime.key

@Composable
fun CardStack(
    files: List<MediaFile>,
    onSwiped: (ActionType, MediaFile) -> Unit,
    onFolderLongPress: (() -> Unit)? = null,
    onZoomModeChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Take up to 4 items for the stack
        val visibleFiles = files.take(4).reversed()

        visibleFiles.forEachIndexed { reversedIndex, file ->
            val indexFromTop = visibleFiles.size - 1 - reversedIndex
            val isTopCard = indexFromTop == 0

            // Progressive depth, scale & blur for brutalist card stack
            val scale = 1f - (indexFromTop * 0.04f)
            val offsetY = (indexFromTop * 14).dp
            val offsetX = (indexFromTop * 6).dp
            val rotationZ = if (indexFromTop % 2 == 1) 2.5f else -1.5f * indexFromTop
            val alpha = when (indexFromTop) {
                0 -> 1.0f
                1 -> 0.85f
                2 -> 0.65f
                else -> 0.45f
            }
            val blurRadius = (indexFromTop * 6).dp

            key(file.id) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.86f)
                        .aspectRatio(0.72f)
                        .offset(x = offsetX, y = offsetY)
                        .scale(scale)
                        .graphicsLayer {
                            this.rotationZ = if (isTopCard) 0f else rotationZ
                        }
                        .alpha(alpha)
                        .then(
                            if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier
                        )
                ) {
                    MediaCard(
                        file = file,
                        isTopCard = isTopCard,
                        onSwiped = { action -> if (isTopCard) onSwiped(action, file) },
                        onDragDelta = { _, _ -> },
                        onFolderLongPress = onFolderLongPress,
                        onZoomModeChanged = { isZoomed ->
                            if (isTopCard) onZoomModeChanged?.invoke(isZoomed)
                        }
                    )
                }
            }
        }
    }
}
