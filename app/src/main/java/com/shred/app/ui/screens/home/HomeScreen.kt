package com.shred.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSwipe: (MediaType) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalShredColors.current
    var isShredderTargeted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Brutalist Header & Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, colors.borderSubtle)
                .background(colors.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S H R E D",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 4.sp,
                    color = colors.textPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .border(1.5.dp, colors.border)
                            .clickable { onNavigateToFavorites() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "★ FAV (${state.favoritesCount})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.accent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .border(1.5.dp, colors.border)
                            .clickable { onNavigateToSettings() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SYS ⚙",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Storage Purged Ticker & Background Scanning Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRASH STAGED: ${FileSizeFormatter.format(state.totalDeletedSize)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary
                )

                if (state.isScanning) {
                    Text(
                        text = "⟳ SCANNING (${state.scannedCount})...",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.accent
                    )
                }
            }
        }

        // Main Playground + Shredder Machine Container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            // Shredder machine height ~ 180dp at bottom
            val shredderHeightPx = with(density) { 180.dp.toPx() }
            val shredderThresholdY = (heightPx - shredderHeightPx).coerceAtLeast(0f)

            // Bottom Shredder Machine
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                ShredderMachine(
                    modifier = Modifier.padding(bottom = 12.dp),
                    isHovered = isShredderTargeted,
                    onSettingsClick = onNavigateToSettings
                )
            }

            // Multi-Block Physics Playground with Pairwise Collision Resolution & Accelerometer Motion Sensor
            FloatingCategoryPlayground(
                photoCount = state.photoCount,
                photoSizeFormatted = FileSizeFormatter.format(state.photoSize),
                photoSizeBytes = state.photoSize,
                videoCount = state.videoCount,
                videoSizeFormatted = FileSizeFormatter.format(state.videoSize),
                videoSizeBytes = state.videoSize,
                audioCount = state.audioCount,
                audioSizeFormatted = FileSizeFormatter.format(state.audioSize),
                audioSizeBytes = state.audioSize,
                containerWidth = widthPx,
                containerHeight = heightPx,
                shredderThresholdY = shredderThresholdY,
                onHoverShredderStateChanged = { isTargeted ->
                    isShredderTargeted = isTargeted
                },
                onThrownIntoShredder = { mediaType ->
                    onNavigateToSwipe(mediaType)
                }
            )
        }
    }
}
