package com.shred.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shred.app.data.model.ActionType
import com.shred.app.ui.theme.LocalShredAnimation
import com.shred.app.ui.theme.LocalShredColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class DeckMode {
    BROWSE,    // Left/Right swipes loop the deck infinitely (Categories, Folders)
    ACTION,    // Left=Delete, Right=Keep, Up=Favorite, Down=Flip (Files)
    FAVORITES  // Left/Right=Loop, Up=Unfavorite, Down=Flip (Favorites Vault)
}

private data class ExitingCardData<T>(
    val item: T,
    val initialX: Float,
    val targetX: Float
)

@Composable
fun <T> CardDeck(
    items: List<T>,
    deckMode: DeckMode,
    keySelector: (T) -> Any,
    onItemTapped: ((T) -> Unit)? = null,
    onItemAction: ((ActionType, T) -> Unit)? = null,
    onItemLoop: ((T) -> Unit)? = null,
    onUndo: (() -> Unit)? = null,
    cardContent: @Composable (item: T, isTopCard: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val animSpec = LocalShredAnimation.current
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()) return@BoxWithConstraints

        // Card dimensions: narrower width (78%) with classic 2:3 card ratio (0.66f), capped at 80% height
        val targetWidth = maxWidth * 0.78f
        val targetHeightFromWidth = targetWidth / 0.66f
        val maxAllowedHeight = maxHeight * 0.80f

        val (cardWidth, cardHeight) = if (targetHeightFromWidth > maxAllowedHeight) {
            Pair(maxAllowedHeight * 0.66f, maxAllowedHeight)
        } else {
            Pair(targetWidth, targetHeightFromWidth)
        }

        // Show up to 4 items in the visible stack
        val visibleItems = items.take(4).reversed()

        var topCardDragX by remember { mutableFloatStateOf(0f) }
        var exitingCard by remember { mutableStateOf<ExitingCardData<T>?>(null) }

        visibleItems.forEachIndexed { reversedIndex, item ->
            val indexFromTop = visibleItems.size - 1 - reversedIndex
            val isTopCard = indexFromTop == 0

            // Progressive visual hierarchy: 2nd card smoothly rises up as top card is swiped
            val swipeFraction = (abs(topCardDragX) / 280f).coerceIn(0f, 1f)
            val effectiveIndex = if (indexFromTop == 1) 1f - swipeFraction else indexFromTop.toFloat()

            val scale = 1f - (effectiveIndex * 0.045f)
            val offsetY = (effectiveIndex * 14).dp
            val offsetX = (effectiveIndex * 5).dp
            val rotationZ = if (indexFromTop == 1) 2.5f * (1f - swipeFraction) else if (indexFromTop % 2 == 1) 2.5f else -1.5f * indexFromTop
            val alpha = when {
                indexFromTop == 0 -> 1.0f
                indexFromTop == 1 -> 0.92f + (0.08f * swipeFraction)
                indexFromTop == 2 -> 0.65f
                else -> 0.40f
            }

            key(keySelector(item)) {
                if (deckMode == DeckMode.BROWSE && isTopCard) {
                    val animOffsetX = remember(keySelector(item)) { Animatable(0f) }
                    var dragOffsetX by remember(keySelector(item)) { mutableFloatStateOf(0f) }
                    var isDragging by remember(keySelector(item)) { mutableStateOf(false) }

                    val displayX = if (isDragging) dragOffsetX else animOffsetX.value
                    val browseRotation = (displayX / 48f).coerceIn(-18f, 18f)

                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .height(cardHeight)
                            .offset { IntOffset(displayX.roundToInt(), 0) }
                            .scale(scale)
                            .graphicsLayer {
                                this.rotationZ = browseRotation
                                transformOrigin = TransformOrigin.Center
                            }
                            .alpha(alpha)
                            .pointerInput(keySelector(item)) {
                                detectTapGestures(
                                    onTap = { onItemTapped?.invoke(item) }
                                )
                            }
                            .pointerInput(keySelector(item)) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDragging = true
                                        dragOffsetX = 0f
                                        topCardDragX = 0f
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        val finalX = dragOffsetX
                                        val threshold = 120f
                                        if (abs(finalX) > threshold) {
                                            val exitTarget = if (finalX > 0) 1400f else -1400f
                                            topCardDragX = 0f
                                            exitingCard = ExitingCardData(item, finalX, exitTarget)
                                            onItemLoop?.invoke(item)
                                        } else {
                                            scope.launch {
                                                animOffsetX.snapTo(finalX)
                                                animOffsetX.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                                        stiffness = androidx.compose.animation.core.Spring.StiffnessHigh
                                                    )
                                                ) {
                                                    topCardDragX = this.value
                                                }
                                                topCardDragX = 0f
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                        val finalX = dragOffsetX
                                        scope.launch {
                                            animOffsetX.snapTo(finalX)
                                            animOffsetX.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                                    stiffness = androidx.compose.animation.core.Spring.StiffnessHigh
                                                )
                                            ) {
                                                topCardDragX = this.value
                                            }
                                            topCardDragX = 0f
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX += dragAmount.x
                                        topCardDragX = dragOffsetX
                                    }
                                )
                            }
                    ) {
                        cardContent(item, true)
                    }
                } else {
                    // Normal stack layer (clean, crisp, no blur, next card ready)
                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .height(cardHeight)
                            .offset(x = offsetX, y = offsetY)
                            .scale(scale)
                            .graphicsLayer {
                                this.rotationZ = if (isTopCard) 0f else rotationZ
                            }
                            .alpha(alpha)
                    ) {
                        cardContent(item, isTopCard)
                    }
                }
            }
        }

        // Overlay layer for the exiting card flying off screen
        exitingCard?.let { exit ->
            val exitAnim = remember(exit.item) { Animatable(exit.initialX) }
            LaunchedEffect(exit.item) {
                exitAnim.animateTo(
                    targetValue = exit.targetX,
                    animationSpec = tween(120, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                )
                exitingCard = null
            }
            val exitRotation = (exitAnim.value / 45f).coerceIn(-24f, 24f)
            val exitAlpha = (1f - (abs(exitAnim.value) / 1200f)).coerceIn(0.1f, 1f)

            Box(
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .offset { IntOffset(exitAnim.value.roundToInt(), 0) }
                    .graphicsLayer {
                        rotationZ = exitRotation
                        transformOrigin = TransformOrigin.Center
                    }
                    .alpha(exitAlpha)
            ) {
                cardContent(exit.item, false)
            }
        }
    }
}
