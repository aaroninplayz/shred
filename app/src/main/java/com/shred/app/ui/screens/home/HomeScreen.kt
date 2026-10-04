package com.shred.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.FormatTypeStat
import com.shred.app.data.model.MediaType
import com.shred.app.ui.components.CardDeck
import com.shred.app.ui.components.DeckMode
import com.shred.app.ui.components.SkeletonCardLoading
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter

data class CategoryCardItemData(
    val mediaType: MediaType,
    val title: String,
    val categoryNumber: String,
    val count: Int,
    val sizeFormatted: String,
    val formatStats: List<FormatTypeStat> = emptyList()
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSwipe: (MediaType) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToTrash: () -> Unit = {},
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val photoFormatStats by viewModel.photoFormatStats.collectAsState()
    val videoFormatStats by viewModel.videoFormatStats.collectAsState()
    val audioFormatStats by viewModel.audioFormatStats.collectAsState()
    val colors = LocalShredColors.current

    var selectedFormatItem by remember { mutableStateOf<CategoryCardItemData?>(null) }
    val categoryOrder by viewModel.categoryOrder.collectAsState()

    val categoryItems = categoryOrder.map { type ->
        when (type) {
            MediaType.PHOTO -> CategoryCardItemData(
                mediaType = MediaType.PHOTO,
                title = "PHOTOS",
                categoryNumber = "01",
                count = state.photoCount,
                sizeFormatted = FileSizeFormatter.format(state.photoSize),
                formatStats = photoFormatStats
            )
            MediaType.VIDEO -> CategoryCardItemData(
                mediaType = MediaType.VIDEO,
                title = "VIDEOS",
                categoryNumber = "02",
                count = state.videoCount,
                sizeFormatted = FileSizeFormatter.format(state.videoSize),
                formatStats = videoFormatStats
            )
            MediaType.AUDIO -> CategoryCardItemData(
                mediaType = MediaType.AUDIO,
                title = "AUDIO",
                categoryNumber = "03",
                count = state.audioCount,
                sizeFormatted = FileSizeFormatter.format(state.audioSize),
                formatStats = audioFormatStats
            )
        }
    }

    val isGlass = colors.isGlass
    val pillShape = if (colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isGlass) Color.Transparent else colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Top Header Bar
        val headerShape = if (isGlass) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isGlass) {
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .shadow(8.dp, headerShape, ambientColor = Color(0x3300F2FE), spotColor = Color(0x22FF007F))
                            .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), headerShape)
                    } else if (colors.borderWidth > 0.dp) {
                        Modifier.border(colors.borderWidth, colors.borderSubtle)
                    } else Modifier
                )
                .background(colors.surface, headerShape)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S H R E D",
                    fontFamily = if (isGlass) FontFamily.Default else if (colors.cardCornerRadius > 0.dp) FontFamily.Serif else FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = if (colors.cardCornerRadius > 0.dp) 2.sp else 4.sp,
                    color = colors.textPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Favorites Button
                    Box(
                        modifier = Modifier
                            .then(
                                if (isGlass) {
                                    Modifier
                                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), pillShape)
                                        .background(colors.cardBackground, pillShape)
                                } else {
                                    Modifier.background(colors.invertedFill, pillShape)
                                }
                            )
                            .clickable { onNavigateToFavorites() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "FAVORITES (${state.favoritesCount})",
                            fontFamily = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isGlass) colors.accent else colors.invertedText
                        )
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .then(
                                if (isGlass) {
                                    Modifier
                                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), pillShape)
                                        .background(colors.cardBackground, pillShape)
                                } else {
                                    Modifier.background(colors.invertedFill, pillShape)
                                }
                            )
                            .clickable { onNavigateToSettings() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SETTINGS",
                            fontFamily = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isGlass) colors.accent else colors.invertedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-status Row: Staged Storage & Live Scanning Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STAGED: ${FileSizeFormatter.format(state.totalDeletedSize)} ↗",
                    fontFamily = if (colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.clickable { onNavigateToTrash() }
                )

                if (state.isScanning) {
                    Text(
                        text = "SCANNING (${state.scannedCount})...",
                        fontFamily = if (colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = colors.accent
                    )
                }
            }
        }

        // 2. Center Category Card Deck
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val totalFiles = state.photoCount + state.videoCount + state.audioCount
            if (state.isScanning && totalFiles == 0) {
                SkeletonCardLoading()
            } else {
                CardDeck(
                    items = categoryItems,
                    deckMode = DeckMode.BROWSE,
                    keySelector = { it.mediaType },
                    onItemTapped = { item ->
                        onNavigateToSwipe(item.mediaType)
                    },
                    onItemLoop = { item ->
                        viewModel.reorderCategory(item.mediaType)
                    },
                    cardContent = { item, isTopCard ->
                        CategoryCardContent(
                            item = item,
                            isTopCard = isTopCard,
                            onOpenDistribution = { selectedFormatItem = it }
                        )
                    }
                )
            }
        }

        if (selectedFormatItem != null) {
            FormatDistributionDialog(
                item = selectedFormatItem!!,
                onDismiss = { selectedFormatItem = null }
            )
        }
    }
}

@Composable
private fun CategoryCardContent(
    item: CategoryCardItemData,
    isTopCard: Boolean,
    onOpenDistribution: (CategoryCardItemData) -> Unit
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(colors.cardCornerRadius)
    val innerShape = if (isGlass) RoundedCornerShape(12.dp) else if (colors.cardCornerRadius > 0.dp) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace

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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar: Category Index Only (01, 02, 03) - Clean, zero badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.categoryNumber,
                    fontFamily = font,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    color = colors.textSecondary
                )
            }

            // 2. Middle Body: Clean Prominent Typography
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                // Category Title (Massive bold lettering, zero subtitle)
                Text(
                    text = item.title,
                    fontFamily = font,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    letterSpacing = 3.sp,
                    color = colors.textPrimary,
                    lineHeight = 44.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Typographic Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (colors.borderWidth > 0.dp) 2.dp else 1.dp)
                        .background(colors.borderSubtle)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Metric: Giant Item Count
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${item.count}",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 58.sp,
                        color = colors.accent,
                        lineHeight = 58.sp
                    )
                    Text(
                        text = "ITEMS",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Typographic Specs Breakdown (Click to open Pie Chart storage distribution)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(innerShape)
                        .clickable { onOpenDistribution(item) }
                        .background(colors.surface, innerShape)
                        .border(1.dp, colors.borderSubtle, innerShape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL SIZE",
                            fontFamily = font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = item.sizeFormatted,
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FORMAT TYPE",
                            fontFamily = font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = colors.textSecondary
                        )
                        val formatsText = if (item.formatStats.isNotEmpty()) {
                            item.formatStats
                                .map { it.extension.uppercase() }
                                .filter { it.isNotBlank() }
                                .distinct()
                                .joinToString(", ")
                        } else {
                            "NONE"
                        }
                        Text(
                            text = formatsText,
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = colors.accent,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(start = 12.dp)
                        )
                    }
                }

                // If Audio, embed the clean centered audio waveform visualizer
                if (item.mediaType == MediaType.AUDIO) {
                    Spacer(modifier = Modifier.height(14.dp))
                    AudioVisualizerView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatDistributionDialog(
    item: CategoryCardItemData,
    onDismiss: () -> Unit
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    val dialogShape = RoundedCornerShape(if (isGlass) 24.dp else if (colors.cardCornerRadius > 0.dp) 16.dp else 0.dp)
    val innerShape = if (isGlass) RoundedCornerShape(12.dp) else if (colors.cardCornerRadius > 0.dp) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)

    val totalBytes = item.formatStats.sumOf { it.totalSizeBytes }

    val sliceColors = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF007F), // Neon Magenta
        Color(0xFFFFD600), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFF7C4DFF), // Electric Purple
        Color(0xFFFF6D00), // Orange
        Color(0xFF00B0FF), // Bright Blue
        Color(0xFFFF3D00)  // Red-Orange
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(dialogShape)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click inside dialog
                    )
                    .then(
                        if (isGlass) {
                            Modifier
                                .shadow(24.dp, dialogShape, ambientColor = Color(0x4400F2FE), spotColor = Color(0x33FF007F))
                                .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), dialogShape)
                        } else {
                            Modifier.border(
                                width = if (colors.borderWidth > 0.dp) colors.borderWidth else 1.5.dp,
                                color = colors.border,
                                shape = dialogShape
                            )
                        }
                    )
                    .background(colors.cardBackground, dialogShape)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Title Only (Zero top X button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.title} BREAKDOWN",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.5.sp,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (item.formatStats.isEmpty() || totalBytes == 0L) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO FORMAT DATA FOUND",
                                fontFamily = font,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    } else {
                        // Donut Pie Chart Canvas
                        Box(
                            modifier = Modifier.size(170.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                val strokeWidth = 26.dp.toPx()
                                var startAngle = -90f

                                for (i in item.formatStats.indices) {
                                    val stat = item.formatStats[i]
                                    val sweep = (stat.totalSizeBytes.toFloat() / totalBytes.toFloat()) * 360f
                                    val color = sliceColors[i % sliceColors.size]

                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    startAngle += sweep
                                }
                            }

                            // Center Label
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = item.sizeFormatted,
                                    fontFamily = font,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "TOTAL",
                                    fontFamily = font,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Format Breakdown List
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.formatStats.forEachIndexed { i, stat ->
                                val color = sliceColors[i % sliceColors.size]
                                val percentage = if (totalBytes > 0) {
                                    (stat.totalSizeBytes.toDouble() / totalBytes.toDouble()) * 100
                                } else 0.0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.surface, innerShape)
                                        .border(1.dp, colors.borderSubtle, innerShape)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, CircleShape)
                                        )
                                        Text(
                                            text = ".${stat.extension.uppercase().ifBlank { "UNKNOWN" }}",
                                            fontFamily = font,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "(${stat.count})",
                                            fontFamily = font,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = colors.textSecondary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = FileSizeFormatter.format(stat.totalSizeBytes),
                                            fontFamily = font,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = String.format(java.util.Locale.US, "%.1f%%", percentage),
                                            fontFamily = font,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Close Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(innerShape)
                            .clickable { onDismiss() }
                            .background(colors.invertedFill, innerShape)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLOSE",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = colors.invertedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioVisualizerView(
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cornerRadius = if (isGlass) 12.dp else if (colors.cardCornerRadius > 0.dp) 8.dp else 0.dp
    val shape = RoundedCornerShape(cornerRadius)

    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .border(
                width = if (colors.borderWidth > 0.dp) colors.borderWidth else 1.dp,
                color = colors.borderSubtle,
                shape = shape
            )
            .background(colors.surface),
        contentAlignment = Alignment.Center
    ) {
        // Pure animated audio visualizer symmetrically expanding from the horizontal center axis
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.70f)
        ) {
            val barCount = 36
            val totalWidth = size.width
            val barWidth = (totalWidth / barCount) * 0.52f
            val spacing = (totalWidth - (barCount * barWidth)) / (barCount - 1)
            val centerY = size.height / 2f
            val maxBarHeight = size.height * 0.90f

            for (i in 0 until barCount) {
                val normalized = i.toFloat() / barCount
                // Multi-harmonic sine waves creating organic, rhythmic musical waveforms
                val w1 = kotlin.math.sin(phase * 2.5f + normalized * 6.28f)
                val w2 = kotlin.math.cos(phase * 1.8f - normalized * 4.2f)
                val w3 = kotlin.math.sin(phase * 3.6f + normalized * 8.5f)
                val combined = ((w1 + w2 + w3) / 3f + 1f) / 2f

                // Musical frequency envelope: natural audio bell-curve curve
                val envelope = kotlin.math.sin(normalized * kotlin.math.PI.toFloat()).coerceAtLeast(0.35f)
                val barHeight = (combined * maxBarHeight * envelope).coerceIn(10f, maxBarHeight)
                val halfHeight = barHeight / 2f

                val x = i * (barWidth + spacing) + barWidth / 2f

                if (isGlass) {
                    // Glassmorphism: Neon Cyan to Magenta gradient
                    drawLine(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color(0xFF00F2FE), Color(0xFFFF007F)),
                            startY = centerY - halfHeight,
                            endY = centerY + halfHeight
                        ),
                        start = Offset(x, centerY - halfHeight),
                        end = Offset(x, centerY + halfHeight),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                } else {
                    // Brutalism / Minimalism: Accent color with theme-appropriate cap
                    drawLine(
                        color = colors.accent,
                        start = Offset(x, centerY - halfHeight),
                        end = Offset(x, centerY + halfHeight),
                        strokeWidth = barWidth,
                        cap = if (colors.cardCornerRadius > 0.dp) StrokeCap.Round else StrokeCap.Square
                    )
                }
            }
        }
    }
}
