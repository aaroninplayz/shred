package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.ui.components.CardDeck
import com.shred.app.ui.components.DeckMode
import com.shred.app.ui.components.SkeletonCardLoading
import com.shred.app.ui.components.StatsPanel
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeDeleteRed
import com.shred.app.util.FileSizeFormatter
import java.io.File

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToKept: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToTrash: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalShredColors.current
    val context = LocalContext.current

    var isZoomActive by remember { mutableStateOf(false) }
    var isStatsPanelVisible by remember { mutableStateOf(false) }

    // Flatten swipe items to get list of files
    val files = remember(state.items) {
        state.items.flatMap { item ->
            when (item) {
                is SwipeItem.SingleFile -> listOf(item.file)
                is SwipeItem.Group -> item.group.files
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen Dark Backdrop when Zoom Mode is Active
        if (isZoomActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
            )
        }

        val isGlass = colors.isGlass
        val headerShape = if (isGlass) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp)
        val pillShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)
        val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isZoomActive || isGlass) Color.Transparent else colors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Top Minimal Header Bar (Swipe down reveals StatsPanel)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isGlass) {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .shadow(8.dp, headerShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                                .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), headerShape)
                        } else if (colors.borderWidth > 0.dp) {
                            Modifier.border(2.dp, colors.borderSubtle)
                        } else Modifier
                    )
                    .background(colors.surface, headerShape)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .then(if (isZoomActive) Modifier.blur(24.dp) else Modifier)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 15f) {
                                isStatsPanelVisible = true
                            }
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }

                    Column {
                        Text(
                            text = viewModel.mediaType.name,
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        val folderLabel = viewModel.folderPath?.substringAfterLast('/')?.ifEmpty { "ROOT" } ?: "ALL SECTORS"
                        Text(
                            text = "FOLDER: $folderLabel",
                            fontFamily = font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colors.textSecondary
                        )
                    }
                }

                // Stats Trigger Button (Inverted Pill)
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
                        .clickable { isStatsPanelVisible = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "📊 STATS",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (isGlass) colors.accent else colors.invertedText
                    )
                }
            }

            // 2. Center Swiping Card Deck Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    SkeletonCardLoading()
                } else if (files.isEmpty()) {
                    // Empty State: All Clean
                    val emptyShape = RoundedCornerShape(colors.cardCornerRadius)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.86f)
                            .aspectRatio(0.72f)
                            .then(
                                if (isGlass) {
                                    Modifier
                                        .shadow(12.dp, emptyShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
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
                                text = if (isGlass) "ALL CLEAN" else "ALL CLEAN ///",
                                fontFamily = font,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                letterSpacing = 2.sp,
                                color = colors.accent
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "EVERY FILE IN THIS SECTOR HAS BEEN REVIEWED.",
                                fontFamily = font,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                            val returnBtnShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .then(
                                        if (isGlass) {
                                            Modifier
                                                .shadow(8.dp, returnBtnShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                                                .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), returnBtnShape)
                                                .background(colors.cardBackground, returnBtnShape)
                                        } else {
                                            Modifier.background(colors.invertedFill, returnBtnShape)
                                        }
                                    )
                                    .clickable { onNavigateBack() }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isGlass) "RETURN" else "[ RETURN ]",
                                    fontFamily = font,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = if (isGlass) colors.accent else colors.invertedText
                                )
                            }
                        }
                    }
                } else {
                    CardDeck(
                        items = files,
                        deckMode = DeckMode.ACTION,
                        keySelector = { it.id },
                        onUndo = { viewModel.performUndo() },
                        cardContent = { file, isTopCard ->
                            if (isTopCard) {
                                MediaCard(
                                    file = file,
                                    isTopCard = true,
                                    onSwiped = { action -> viewModel.handleSwipeAction(action, file) },
                                    onUndo = { viewModel.performUndo() },
                                    onFolderLongPress = { isStatsPanelVisible = true },
                                    onZoomModeChanged = { zoomed -> isZoomActive = zoomed }
                                )
                            } else {
                                // Lightweight Background Thumbnail
                                BackgroundCardThumbnail(file = file, context = context)
                            }
                        }
                    )
                }
            }
        }

        // 3. Hidden Pull-Down Stats Panel
        StatsPanel(
            isVisible = isStatsPanelVisible,
            currentPosition = state.currentPosition,
            totalCount = state.totalCount,
            keptCount = state.keptCount,
            deletedCount = state.deletedCount,
            favoritedCount = state.favoritedCount,
            freedBytes = state.deletedSizeInSession,
            currentFolderPath = viewModel.folderPath,
            trashRetentionHours = state.settings.trashRetentionHours,
            onDismiss = { isStatsPanelVisible = false },
            onOpenFolderSelector = { viewModel.showFolderGrid() },
            onNavigateToKept = {
                isStatsPanelVisible = false
                onNavigateToKept()
            },
            onNavigateToFavorites = {
                isStatsPanelVisible = false
                onNavigateToFavorites()
            },
            onNavigateToTrash = {
                isStatsPanelVisible = false
                onNavigateToTrash()
            }
        )

        // 4. Folder Grid Selector Dialog
        if (state.isFolderGridVisible) {
            FolderGridSelectorDialog(
                folderGroups = state.allFolderGroups,
                onSelectFolder = { group ->
                    viewModel.selectFolderToStart(group)
                },
                onDismiss = { viewModel.hideFolderGrid() }
            )
        }

        // 5. Folder Mass Delete Confirmation Dialog
        if (state.pendingFolderDelete != null) {
            val group = state.pendingFolderDelete!!
            AlertDialog(
                onDismissRequest = { viewModel.cancelFolderDelete() },
                title = {
                    Text(
                        text = "PURGE FOLDER ///",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = SwipeDeleteRed
                    )
                },
                text = {
                    Text(
                        text = "STAGE ALL ${group.fileCount} FILES IN '${group.folderName}' (${FileSizeFormatter.format(group.totalSizeBytes)}) FOR DELETION?",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colors.textPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmFolderDelete() },
                        colors = ButtonDefaults.buttonColors(containerColor = SwipeDeleteRed),
                        shape = RoundedCornerShape(colors.cardCornerRadius)
                    ) {
                        Text("PURGE ENTIRE FOLDER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelFolderDelete() }) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    }
                },
                containerColor = colors.surface,
                shape = RoundedCornerShape(colors.cardCornerRadius)
            )
        }
    }
}

@Composable
private fun BackgroundCardThumbnail(
    file: MediaFile,
    context: android.content.Context
) {
    val colors = LocalShredColors.current
    val cardShape = RoundedCornerShape(colors.cardCornerRadius)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black, cardShape)
            .border(colors.borderWidth, colors.border, cardShape),
        contentAlignment = Alignment.Center
    ) {
        when (file.mediaType) {
            MediaType.PHOTO -> {
                val diskFile = remember(file.path) { File(file.path) }
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (diskFile.exists()) diskFile else file.path)
                        .size(720, 1080)
                        .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                        .crossfade(false)
                        .memoryCacheKey("thumb_${file.path}")
                        .diskCacheKey("thumb_${file.path}")
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            MediaType.VIDEO -> {
                val diskFile = remember(file.path) { File(file.path) }
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (diskFile.exists()) diskFile else file.path)
                            .size(200, 300)
                            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                            .crossfade(false)
                            .memoryCacheKey("thumb_${file.path}")
                            .diskCacheKey("thumb_${file.path}")
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "▶", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            MediaType.AUDIO -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🎧", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = file.fileName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
