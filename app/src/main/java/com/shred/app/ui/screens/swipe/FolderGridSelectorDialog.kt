package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.shred.app.data.model.FolderGroup
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter

@Composable
fun FolderGridSelectorDialog(
    folderGroups: List<FolderGroup>,
    onSelectFolder: (FolderGroup) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalShredColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .border(3.dp, colors.border)
                    .background(colors.cardBackground)
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "[ 📂 SELECT FOLDER TO START ]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${folderGroups.size} DIRECTORIES DISCOVERED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .border(2.dp, colors.border)
                            .background(colors.surface)
                            .clickable { onDismiss() }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Folder Grid
                if (folderGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO MULTI-FILE FOLDERS DISCOVERED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(folderGroups, key = { it.folderPath }) { group ->
                            FolderGridItemCard(
                                group = group,
                                onClick = { onSelectFolder(group) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderGridItemCard(
    group: FolderGroup,
    onClick: () -> Unit
) {
    val colors = LocalShredColors.current
    val samples = remember(group.folderPath) { group.files.take(4) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(2.dp, colors.border)
            .background(colors.surface)
            .clickable { onClick() }
    ) {
        // Background 2x2 Collage Preview
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (samples.isNotEmpty()) GridThumbnail(samples[0].path, samples[0].mediaType)
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (samples.size > 1) GridThumbnail(samples[1].path, samples[1].mediaType)
                }
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (samples.size > 2) GridThumbnail(samples[2].path, samples[2].mediaType)
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (samples.size > 3) GridThumbnail(samples[3].path, samples[3].mediaType)
                }
            }
        }

        // Dark Gradient Overlay for Readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
        )

        // Text & Details Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = group.folderName,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
            }

            Column {
                Text(
                    text = group.folderPath,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, colors.accent)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${group.fileCount} FILES • ${FileSizeFormatter.format(group.totalSizeBytes)}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = colors.accent
                    )
                }
            }
        }
    }
}

@Composable
private fun GridThumbnail(filePath: String, mediaType: MediaType) {
    val context = LocalContext.current
    val colors = LocalShredColors.current

    if (mediaType == MediaType.AUDIO) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.cardBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎵", fontSize = 16.sp)
        }
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(filePath)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            loading = {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
            },
            error = {
                Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
            }
        )
    }
}
