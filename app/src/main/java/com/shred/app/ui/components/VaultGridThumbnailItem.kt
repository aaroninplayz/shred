package com.shred.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter
import java.io.File

@Composable
fun VaultGridThumbnailItem(
    file: MediaFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val context = LocalContext.current
    val shimmerBrush = rememberShimmerBrush()

    val cardShape = RoundedCornerShape(if (colors.cardCornerRadius > 0.dp) (colors.cardCornerRadius * 0.75f).coerceAtLeast(6.dp) else 0.dp)
    val font = if (colors.isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (colors.isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = Color(0x3300F2FE), spotColor = Color(0x22FF007F))
                        .border(1.dp, Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else {
                    Modifier.border(1.dp, colors.borderSubtle, cardShape)
                }
            )
            .clip(cardShape)
            .background(colors.cardBackground, cardShape)
            .clickable { onClick() }
    ) {
        // Thumbnail Image with Immediate Skeleton Loading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            when (file.mediaType) {
                MediaType.PHOTO -> {
                    val diskFile = remember(file.path) { File(file.path) }
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (diskFile.exists()) diskFile else file.path)
                            .size(280, 280)
                            .crossfade(120)
                            .memoryCacheKey("vault_grid_${file.path}")
                            .diskCacheKey("vault_grid_${file.path}")
                            .build(),
                        contentDescription = file.fileName,
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(shimmerBrush)
                            )
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("IMG", fontFamily = font, fontSize = 10.sp, color = colors.textSecondary)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaType.VIDEO -> {
                    val diskFile = remember(file.path) { File(file.path) }
                    Box(modifier = Modifier.fillMaxSize()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (diskFile.exists()) diskFile else file.path)
                                .size(280, 280)
                                .crossfade(120)
                                .memoryCacheKey("vault_grid_${file.path}")
                                .diskCacheKey("vault_grid_${file.path}")
                                .build(),
                            contentDescription = file.fileName,
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(shimmerBrush)
                                )
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colors.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("VID", fontFamily = font, fontSize = 10.sp, color = colors.textSecondary)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(26.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(9999.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "▶", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                MediaType.AUDIO -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surface)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🎧", fontSize = 24.sp)
                    }
                }
            }
        }

        // Bottom Compact Meta Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface.copy(alpha = 0.9f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = file.fileName,
                fontFamily = font,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = FileSizeFormatter.format(file.sizeBytes),
                fontFamily = font,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )
        }
    }
}
