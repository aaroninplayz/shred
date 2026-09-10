package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.ProgressBarStyle
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    onNavigateBack: () -> Unit
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
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, colors.borderSubtle)
                .background(colors.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Text(
                    text = "[ ${viewModel.mediaType.name} ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
            }

            // Undo Button
            if (state.canUndo) {
                Box(
                    modifier = Modifier
                        .border(2.dp, colors.accent)
                        .background(colors.cardBackground)
                        .clickable { viewModel.performUndo() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "UNDO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = colors.accent
                        )
                    }
                }
            }
        }

        // Configurable Progress Bar Section
        ProgressBarSection(
            style = state.settings.progressBarStyle,
            current = state.currentPosition,
            total = state.totalCount,
            freedBytes = state.deletedSizeInSession,
            keptCount = state.keptCount,
            deletedCount = state.deletedCount
        )

        // Center Swiping Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.items.isEmpty()) {
                // Empty / Cleaned State
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(3.dp, colors.border)
                        .background(colors.cardBackground)
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ALL CLEAN ///",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO MORE UNREVIEWED ${viewModel.mediaType.name} FILES IN QUEUE.",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .border(2.dp, colors.border)
                            .clickable { onNavigateBack() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "[ RETURN TO SHREDDER ]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            } else {
                when (val topItem = state.items.first()) {
                    is SwipeItem.Group -> {
                        FolderHeaderCard(
                            folderGroup = topItem.group,
                            onExpandClick = { viewModel.expandFolderGroup(topItem.group) },
                            modifier = Modifier.fillMaxSize(0.92f)
                        )
                    }
                    is SwipeItem.SingleFile -> {
                        // Extract all upcoming single files for the stack view
                        val upcomingFiles = state.items.mapNotNull {
                            (it as? SwipeItem.SingleFile)?.file
                        }
                        CardStack(
                            files = upcomingFiles,
                            onSwiped = { action, file -> viewModel.handleSwipeAction(action, file) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressBarSection(
    style: ProgressBarStyle,
    current: Int,
    total: Int,
    freedBytes: Long,
    keptCount: Int,
    deletedCount: Int
) {
    val colors = LocalShredColors.current

    if (style == ProgressBarStyle.HIDDEN) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        when (style) {
            ProgressBarStyle.MINIMAL_COUNTER -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PROGRESS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$current / $total",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = colors.accent
                    )
                }
            }
            ProgressBarStyle.STORAGE_FREED -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FREED IN SESSION",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = FileSizeFormatter.format(freedBytes),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = colors.accent
                    )
                }
            }
            ProgressBarStyle.RATIO_BAR -> {
                val totalActions = (keptCount + deletedCount).coerceAtLeast(1)
                val keptRatio = keptCount.toFloat() / totalActions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .border(1.dp, colors.borderSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(keptRatio.coerceAtLeast(0.01f))
                            .background(Color(0xFF00E676))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((1f - keptRatio).coerceAtLeast(0.01f))
                            .background(Color(0xFFFF2A2A))
                    )
                }
            }
            ProgressBarStyle.NUMBERS_ONLY -> {
                Text(
                    text = "REMAINING: ${(total - current).coerceAtLeast(0)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.textPrimary
                )
            }
            ProgressBarStyle.HIDDEN -> {}
        }
    }
}
