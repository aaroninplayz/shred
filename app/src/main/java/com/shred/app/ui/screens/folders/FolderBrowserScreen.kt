package com.shred.app.ui.screens.folders

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shred.app.data.model.FolderGroup
import com.shred.app.data.model.MediaType
import com.shred.app.ui.components.CardDeck
import com.shred.app.ui.components.DeckMode
import com.shred.app.ui.components.SkeletonCardLoading
import com.shred.app.ui.components.interactiveMultiZoomGesture
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter

enum class FolderViewMode {
    DECK,
    GRID
}

/**
 * Continuous interactive 2-finger pinch gesture detector.
 * Only tracks when active pointers >= 2 so single-finger scrolling is 100% native.
 */
fun Modifier.interactivePinchGesture(
    onTriggerPinchOut: () -> Unit,
    onTriggerPinchIn: () -> Unit,
    onScaleChange: (Float) -> Unit,
    onGestureEnd: () -> Unit
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var initDist: Float? = null
        var triggered = false

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

                        if (!triggered) {
                            if (currentScale < 0.82f) {
                                triggered = true
                                activePointers.forEach { it.consume() }
                                onTriggerPinchOut()
                            } else if (currentScale > 1.20f) {
                                triggered = true
                                activePointers.forEach { it.consume() }
                                onTriggerPinchIn()
                            }
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

@Composable
fun FolderBrowserScreen(
    viewModel: FolderBrowserViewModel,
    onNavigateToSwipe: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalShredColors.current

    val folderOrder = state.folders

    var viewMode by remember { mutableStateOf(FolderViewMode.DECK) }
    var gridColumns by remember { mutableIntStateOf(2) } // 2, 3, or 4
    var interactiveScale by remember { mutableFloatStateOf(1f) }

    val animatedScale by animateFloatAsState(
        targetValue = interactiveScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "pinchScaleAnim"
    )

    val isGlass = colors.isGlass
    val headerShape = if (isGlass) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp)

    val categoryTitle = when (viewModel.mediaType) {
        MediaType.PHOTO -> "PHOTOS FOLDER"
        MediaType.VIDEO -> "VIDEOS FOLDER"
        MediaType.AUDIO -> "AUDIO FOLDER"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isGlass) Color.Transparent else colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header Bar (Pure typography, no toggle buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isGlass) {
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .shadow(8.dp, headerShape, ambientColor = Color(0x3300F2FE), spotColor = Color(0x22FF007F))
                            .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), headerShape)
                    } else if (colors.borderWidth > 0.dp) {
                        Modifier.border(2.dp, colors.borderSubtle)
                    } else Modifier
                )
                .background(colors.surface, headerShape)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = categoryTitle,
                    fontFamily = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = colors.textPrimary
                )
            }

            val pillShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)
            Box(
                modifier = Modifier
                    .then(
                        if (isGlass) {
                            Modifier
                                .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), pillShape)
                                .background(colors.cardBackground, pillShape)
                        } else {
                            Modifier
                                .background(colors.surface, pillShape)
                                .border(1.dp, colors.borderSubtle, pillShape)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (viewMode == FolderViewMode.DECK) "${folderOrder.size} DIRS" else "${gridColumns}× GRID",
                    fontFamily = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.accent
                )
            }
        }

        // Center Area: Animated transition with live interactive pinch scale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                SkeletonCardLoading()
            } else if (folderOrder.isEmpty()) {
                val emptyShape = RoundedCornerShape(colors.cardCornerRadius)
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.86f)
                        .aspectRatio(0.72f)
                        .then(
                            if (isGlass) {
                                Modifier
                                    .shadow(12.dp, emptyShape, ambientColor = Color(0x3300F2FE), spotColor = Color(0x22FF007F))
                                    .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), emptyShape)
                            } else {
                                Modifier.border(colors.borderWidth, colors.border, emptyShape)
                            }
                        )
                        .background(colors.cardBackground, emptyShape)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO FOLDERS FOUND",
                            fontFamily = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No files detected for ${viewModel.mediaType.name}",
                            fontFamily = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        fadeIn(tween(180)) + scaleIn(initialScale = 0.95f, animationSpec = tween(180)) togetherWith
                            fadeOut(tween(150)) + scaleOut(targetScale = 1.05f, animationSpec = tween(150))
                    },
                    label = "viewModeContent"
                ) { currentMode ->
                    if (currentMode == FolderViewMode.DECK) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .interactiveMultiZoomGesture(
                                    onZoomOut = {
                                        viewMode = FolderViewMode.GRID
                                        gridColumns = 2
                                        interactiveScale = 1f
                                    },
                                    onZoomIn = {},
                                    onScaleChange = { scale ->
                                        interactiveScale = scale.coerceIn(0.75f, 1.05f)
                                    },
                                    onGestureEnd = {
                                        interactiveScale = 1f
                                    }
                                )
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    alpha = (animatedScale - 0.3f).coerceIn(0.4f, 1f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CardDeck(
                                items = folderOrder,
                                deckMode = DeckMode.BROWSE,
                                keySelector = { it.folderPath },
                                onItemTapped = { group ->
                                    viewModel.focusFolder(group.folderPath)
                                    onNavigateToSwipe(group.folderPath)
                                },
                                onItemLoop = { group ->
                                    viewModel.reorderFolder(group.folderPath)
                                },
                                cardContent = { group, isTopCard ->
                                    FolderCardContent(
                                        group = group,
                                        isTopCard = isTopCard,
                                        onFolderClick = {
                                            viewModel.focusFolder(group.folderPath)
                                            onNavigateToSwipe(group.folderPath)
                                        }
                                    )
                                }
                            )
                        }
                    } else {
                        // Modular Grid View with continuous interactive multi-zoom (2 -> 3 -> 4 columns)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            modifier = Modifier
                                .fillMaxSize()
                                .interactiveMultiZoomGesture(
                                    onZoomOut = {
                                        if (gridColumns < 4) {
                                            gridColumns++
                                        }
                                        interactiveScale = 1f
                                    },
                                    onZoomIn = {
                                        if (gridColumns > 2) {
                                            gridColumns--
                                        } else {
                                            viewMode = FolderViewMode.DECK
                                        }
                                        interactiveScale = 1f
                                    },
                                    onScaleChange = { scale ->
                                        interactiveScale = scale.coerceIn(0.92f, 1.25f)
                                    },
                                    onGestureEnd = {
                                        interactiveScale = 1f
                                    }
                                )
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    alpha = (1.8f - animatedScale).coerceIn(0.4f, 1f)
                                },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = folderOrder,
                                key = { it.folderPath }
                            ) { group ->
                                FolderGridModule(
                                    group = group,
                                    onFolderClick = {
                                        viewModel.focusFolder(group.folderPath)
                                        onNavigateToSwipe(group.folderPath)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCardContent(
    group: FolderGroup,
    isTopCard: Boolean,
    onFolderClick: () -> Unit
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val context = LocalContext.current
    val cardShape = RoundedCornerShape(colors.cardCornerRadius)
    val innerShape = if (isGlass) RoundedCornerShape(12.dp) else if (colors.cardCornerRadius > 0.dp) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    val coverPath = group.coverPath ?: group.files.firstOrNull()?.path
    val firstFile = group.files.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(12.dp, cardShape, ambientColor = Color(0x3300F2FE), spotColor = Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier
                        .shadow(colors.shadowElevation, cardShape)
                        .border(
                            width = colors.borderWidth,
                            color = if (colors.borderWidth > 0.dp) colors.border else Color.Transparent,
                            shape = cardShape
                        )
                } else {
                    Modifier.border(
                        width = colors.borderWidth,
                        color = if (colors.borderWidth > 0.dp) colors.border else Color.Transparent,
                        shape = cardShape
                    )
                }
            )
            .background(colors.cardBackground, shape = cardShape)
            .clickable { onFolderClick() }
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Folder Card Header (Prominent, fully visible folder name with NO truncation or rolling text)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = group.folderName.uppercase(),
                    fontFamily = font,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp,
                    color = colors.textPrimary,
                    softWrap = true,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.folderPath,
                    fontFamily = font,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.textSecondary
                )
            }

            // Center Preview: One large representative image / preview from this folder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .clip(innerShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, innerShape),
                contentAlignment = Alignment.Center
            ) {
                if (coverPath != null) {
                    when (group.mediaType) {
                        MediaType.PHOTO, MediaType.VIDEO -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(coverPath)
                                    .size(600, 600)
                                    .crossfade(100)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        MediaType.AUDIO -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "AUDIO TRACK",
                                    fontFamily = font,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp,
                                    color = colors.accent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = firstFile?.fileName ?: (coverPath?.substringAfterLast('/') ?: group.folderName),
                                    fontFamily = font,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    Text("EMPTY", fontFamily = font, fontSize = 12.sp, color = colors.textSecondary)
                }
            }

            // Card Footer: Document type breakdown and total size row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, innerShape)
                    .border(1.dp, colors.borderSubtle, innerShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "FORMAT TYPE",
                        fontFamily = font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = group.formatTypes,
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = colors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${group.fileCount} FILES",
                        fontFamily = font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = FileSizeFormatter.format(group.totalSizeBytes),
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderGridModule(
    group: FolderGroup,
    onFolderClick: () -> Unit
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (colors.cardCornerRadius > 0.dp) colors.cardCornerRadius else 8.dp)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    val context = LocalContext.current
    val coverPath = group.coverPath ?: group.files.firstOrNull()?.path

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier.border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else {
                    Modifier.border(1.dp, colors.borderSubtle, cardShape)
                }
            )
            .background(colors.cardBackground, cardShape)
            .clip(cardShape)
            .clickable { onFolderClick() }
    ) {
        // Thumbnail Image with Skeleton placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f)
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            if (coverPath != null) {
                when (group.mediaType) {
                    MediaType.PHOTO, MediaType.VIDEO -> {
                        coil.compose.SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(coverPath)
                                .size(320, 320)
                                .crossfade(true)
                                .memoryCacheKey("folder_thumb_${group.folderPath}")
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colors.cardBackground.copy(alpha = 0.5f))
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    MediaType.AUDIO -> {
                        Text(
                            text = "AUDIO",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            color = colors.accent
                        )
                    }
                }
            } else {
                Text(
                    text = "EMPTY",
                    fontFamily = font,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Details Section (Prominent typography, full name visible without rolling text)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = group.folderName.uppercase(),
                fontFamily = font,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                maxLines = 2,
                softWrap = true,
                lineHeight = 15.sp,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = group.formatTypes,
                fontFamily = font,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${group.fileCount} FILES",
                    fontFamily = font,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = FileSizeFormatter.format(group.totalSizeBytes),
                    fontFamily = font,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = colors.textPrimary
                )
            }
        }
    }
}
