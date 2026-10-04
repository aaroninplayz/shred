package com.shred.app.ui.screens.kept

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.shred.app.ui.components.VaultMediaCard
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeKeepGreen
import java.io.File

@Composable
fun KeptScreen(
    viewModel: KeptViewModel,
    onNavigateBack: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val isFolderFilterActive by viewModel.isFolderFilterActive.collectAsState()
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
                        text = "KEPT VAULT",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = SwipeKeepGreen
                    )
                    Text(
                        text = "${items.size} FILES",
                        fontFamily = font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (viewModel.folderPath != null) {
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
                        .clickable { viewModel.toggleFolderFilter() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isFolderFilterActive) "FOLDER ONLY ⇋" else "ALL KEPT ⇋",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = if (isGlass) colors.accent else colors.invertedText
                    )
                }
            }
        }

        // Swiping Area (Card Deck)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
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
                            text = if (isGlass) "NO KEPT FILES" else "NO KEPT FILES ///",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 2.sp,
                            color = SwipeKeepGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "FILES YOU SWIPE RIGHT TO KEEP ARE STORED HERE. SWIPE UP ON ANY CARD TO UN-KEEP AND RETURN IT TO THE REVIEW QUEUE.",
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
                                onActionUp = { viewModel.unkeep(file) }
                            )
                        } else {
                            BackgroundVaultCardThumbnail(file = file, context = context)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BackgroundVaultCardThumbnail(
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
