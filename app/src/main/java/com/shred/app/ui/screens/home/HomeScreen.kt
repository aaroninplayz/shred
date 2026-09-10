package com.shred.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            // Storage Purged Ticker
            Text(
                text = "TRASH STAGED: ${FileSizeFormatter.format(state.totalDeletedSize)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = colors.textSecondary
            )
        }

        // Floating Physics Playground (Top 2/3 of viewport)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            // Floating Photo Block
            FloatingCategoryBlock(
                mediaType = MediaType.PHOTO,
                count = state.photoCount,
                sizeFormatted = FileSizeFormatter.format(state.photoSize),
                initialOffsetX = widthPx * 0.10f,
                initialOffsetY = heightPx * 0.08f,
                shredderThresholdY = heightPx,
                onThrownIntoShredder = { onNavigateToSwipe(MediaType.PHOTO) }
            )

            // Floating Video Block
            FloatingCategoryBlock(
                mediaType = MediaType.VIDEO,
                count = state.videoCount,
                sizeFormatted = FileSizeFormatter.format(state.videoSize),
                initialOffsetX = widthPx * 0.40f,
                initialOffsetY = heightPx * 0.35f,
                shredderThresholdY = heightPx,
                onThrownIntoShredder = { onNavigateToSwipe(MediaType.VIDEO) }
            )

            // Floating Audio Block
            FloatingCategoryBlock(
                mediaType = MediaType.AUDIO,
                count = state.audioCount,
                sizeFormatted = FileSizeFormatter.format(state.audioSize),
                initialOffsetX = widthPx * 0.15f,
                initialOffsetY = heightPx * 0.60f,
                shredderThresholdY = heightPx,
                onThrownIntoShredder = { onNavigateToSwipe(MediaType.AUDIO) }
            )
        }

        // Bottom 1/3: Shredder Machine
        ShredderMachine(
            modifier = Modifier.padding(bottom = 12.dp),
            onSettingsClick = onNavigateToSettings
        )
    }
}
