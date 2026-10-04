package com.shred.app.ui.screens.trash

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.ui.components.CardDeck
import com.shred.app.ui.components.DeckMode
import com.shred.app.ui.components.VaultGridThumbnailItem
import com.shred.app.ui.components.VaultMediaCard
import com.shred.app.ui.components.interactiveMultiZoomGesture
import com.shred.app.ui.screens.kept.BackgroundVaultCardThumbnail
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeDeleteRed
import com.shred.app.ui.theme.SwipeKeepGreen
import com.shred.app.util.FileSizeFormatter

enum class VaultViewMode {
    CARDS,
    GRID
}

@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onNavigateBack: () -> Unit
) {
    val items by viewModel.trashItems.collectAsState()
    val totalSize by viewModel.totalTrashSize.collectAsState()
    val retentionHours by viewModel.retentionHours.collectAsState()
    val isPurgeDialogVisible by viewModel.isPurgeDialogVisible.collectAsState()

    var viewMode by remember { mutableStateOf(VaultViewMode.CARDS) }
    var gridColumns by remember { mutableIntStateOf(2) } // 2, 3, or 4
    var interactiveScale by remember { mutableFloatStateOf(1f) }

    val animatedScale by animateFloatAsState(
        targetValue = interactiveScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "trashScaleAnim"
    )

    val colors = LocalShredColors.current
    val context = LocalContext.current

    val isGlass = colors.isGlass
    val headerShape = if (isGlass) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp)
    val pillShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isGlass) Color.Transparent else colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header Bar
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
                Column {
                    Text(
                        text = "STAGING VAULT",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = SwipeDeleteRed
                    )
                    Text(
                        text = "${items.size} FILES • ${FileSizeFormatter.format(totalSize)}",
                        fontFamily = font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // View Mode Pill (CARDS or 2x/3x/4x GRID)
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
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (viewMode == VaultViewMode.CARDS) "CARDS" else "${gridColumns}× GRID",
                        fontFamily = font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.accent
                    )
                }

                if (items.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(SwipeDeleteRed.copy(alpha = 0.15f), pillShape)
                            .border(1.dp, SwipeDeleteRed, pillShape)
                            .clickable { viewModel.showPurgeDialog() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "PURGE ALL",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = SwipeDeleteRed
                        )
                    }
                }
            }
        }

        // Auto-purge notice strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FILES HELD IN STAGING • AUTO-PURGED AFTER ${retentionHours}H",
                fontFamily = font,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = colors.textSecondary
            )
        }

        // Swiping Area (Cards vs Multi-Column Grid)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (items.isEmpty()) {
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
                            text = if (isGlass) "STAGING VAULT EMPTY" else "STAGING EMPTY ///",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO FILES CURRENTLY STAGED FOR PURGE. FILES DISCARDED DURING REVIEW SIT SAFELY HERE BEFORE AUTO-PURGE. SWIPE UP ON ANY CARD TO RESTORE.",
                            fontFamily = font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        val returnShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .then(
                                    if (isGlass) {
                                        Modifier
                                            .shadow(8.dp, returnShape, ambientColor = Color(0x3300F2FE), spotColor = Color(0x22FF007F))
                                            .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), returnShape)
                                            .background(colors.cardBackground, returnShape)
                                    } else {
                                        Modifier.background(colors.invertedFill, returnShape)
                                    }
                                )
                                .clickable { onNavigateBack() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BACK TO REVIEW",
                                fontFamily = font,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (isGlass) colors.accent else colors.invertedText
                            )
                        }
                    }
                }
            } else {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        fadeIn(tween(180)) + scaleIn(initialScale = 0.95f, animationSpec = tween(180)) togetherWith
                            fadeOut(tween(150)) + scaleOut(targetScale = 1.05f, animationSpec = tween(150))
                    },
                    label = "vaultViewModeContent"
                ) { currentMode ->
                    if (currentMode == VaultViewMode.CARDS) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .interactiveMultiZoomGesture(
                                    onZoomOut = {
                                        viewMode = VaultViewMode.GRID
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
                                items = items,
                                deckMode = DeckMode.FAVORITES,
                                keySelector = { it.id },
                                cardContent = { file, isTopCard ->
                                    if (isTopCard) {
                                        VaultMediaCard(
                                            file = file,
                                            isTopCard = true,
                                            actionColor = SwipeKeepGreen,
                                            onLoop = { viewModel.loopCard(file) },
                                            onActionUp = { viewModel.restoreFile(file) }
                                        )
                                    } else {
                                        BackgroundVaultCardThumbnail(file = file, context = context)
                                    }
                                }
                            )
                        }
                    } else {
                        // Multi-Column Grid View with dynamic pinch density (2 -> 3 -> 4 columns)
                        // Single-finger scroll operates with 100% native smooth velocity
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
                                            viewMode = VaultViewMode.CARDS
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
                                },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = items,
                                key = { it.id }
                            ) { file ->
                                VaultGridThumbnailItem(
                                    file = file,
                                    onClick = {
                                        viewModel.focusItem(file)
                                        viewMode = VaultViewMode.CARDS
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Purge confirmation dialog
    if (isPurgeDialogVisible) {
        val dialogShape = RoundedCornerShape(colors.cardCornerRadius)
        AlertDialog(
            onDismissRequest = { viewModel.dismissPurgeDialog() },
            title = {
                Text(
                    text = "PURGE STAGED FILES",
                    fontFamily = font,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SwipeDeleteRed
                )
            },
            text = {
                Text(
                    text = "PERMANENTLY DELETE ALL ${items.size} FILES (${FileSizeFormatter.format(totalSize)}) FROM DISK? THIS CANNOT BE UNDONE.",
                    fontFamily = font,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPurgeAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = SwipeDeleteRed),
                    shape = dialogShape
                ) {
                    Text("PERMANENTLY PURGE", fontFamily = font, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPurgeDialog() }) {
                    Text("CANCEL", fontFamily = font, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = dialogShape
        )
    }
}
