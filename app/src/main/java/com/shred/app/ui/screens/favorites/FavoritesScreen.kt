package com.shred.app.ui.screens.favorites

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.ui.components.CardDeck
import com.shred.app.ui.components.DeckMode
import com.shred.app.ui.components.VaultGridThumbnailItem
import com.shred.app.ui.components.interactiveMultiZoomGesture
import com.shred.app.ui.screens.trash.VaultViewMode
import com.shred.app.ui.theme.LocalShredColors
import java.io.File

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val colors = LocalShredColors.current
    val context = LocalContext.current

    var viewMode by remember { mutableStateOf(VaultViewMode.CARDS) }
    var gridColumns by remember { mutableIntStateOf(2) } // 2, 3, or 4
    var interactiveScale by remember { mutableFloatStateOf(1f) }

    val animatedScale by animateFloatAsState(
        targetValue = interactiveScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
        label = "favoritesScaleAnim"
    )

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
                Text(
                    text = "FAVORITES VAULT",
                    fontFamily = font,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = colors.accent
                )
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
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${favorites.size} STORED",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = if (isGlass) colors.accent else colors.invertedText
                    )
                }
            }
        }

        // Swiping Area (Favorites Cards vs Multi-Column Grid)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (favorites.isEmpty()) {
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
                            text = if (isGlass) "VAULT EMPTY" else "VAULT EMPTY ///",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = 2.sp,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "SWIPE UP ON ANY CARD IN THE REVIEW QUEUE TO STORE IT IN THIS SAFE VAULT.",
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
                                text = if (isGlass) "RETURN TO SHRED" else "[ BACK TO SHREDDER ]",
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
                    label = "favoritesViewModeContent"
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
                                items = favorites,
                                deckMode = DeckMode.FAVORITES,
                                keySelector = { it.id },
                                cardContent = { file, isTopCard ->
                                    if (isTopCard) {
                                        FavoriteMediaCard(
                                            file = file,
                                            isTopCard = true,
                                            onLoop = { viewModel.loopCard(file) },
                                            onUnfavorite = { viewModel.unfavorite(file) }
                                        )
                                    } else {
                                        BackgroundFavoriteCardThumbnail(file = file, context = context)
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
                                items = favorites,
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
}

@Composable
private fun BackgroundFavoriteCardThumbnail(
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
