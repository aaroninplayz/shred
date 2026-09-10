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

@Composable
fun CardStack(
    files: List<MediaFile>,
    onSwiped: (ActionType, MediaFile) -> Unit,
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

            // Progressive offsets & tilt for brutalist depth
            val scale = 1f - (indexFromTop * 0.035f)
            val offsetY = (indexFromTop * 10).dp
            val offsetX = (indexFromTop * 6).dp
            val rotationZ = if (indexFromTop % 2 == 1) 2.5f else -1.5f * indexFromTop
            val alpha = when (indexFromTop) {
                0 -> 1.0f
                1 -> 0.75f
                2 -> 0.50f
                else -> 0.25f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .offset(x = offsetX, y = offsetY)
                    .scale(scale)
                    .graphicsLayer {
                        this.rotationZ = if (isTopCard) 0f else rotationZ
                    }
                    .alpha(alpha)
            ) {
                if (isTopCard) {
                    MediaCard(
                        file = file,
                        isTopCard = true,
                        onSwiped = { action -> onSwiped(action, file) },
                        onDragDelta = { _, _ -> }
                    )
                } else {
                    // Background cards preview
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.cardBackground)
                            .border(3.dp, colors.borderSubtle)
                    )
                }
            }
        }
    }
}
