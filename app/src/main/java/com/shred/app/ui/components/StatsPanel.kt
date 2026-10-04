package com.shred.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.ui.theme.LocalShredAnimation
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeDeleteRed
import com.shred.app.ui.theme.SwipeFavoriteGold
import com.shred.app.ui.theme.SwipeKeepGreen
import com.shred.app.util.FileSizeFormatter

@Composable
fun StatsPanel(
    isVisible: Boolean,
    currentPosition: Int,
    totalCount: Int,
    keptCount: Int,
    deletedCount: Int,
    favoritedCount: Int,
    freedBytes: Long,
    currentFolderPath: String? = null,
    trashRetentionHours: Int = 2,
    onDismiss: () -> Unit,
    onOpenFolderSelector: () -> Unit,
    onNavigateToKept: (() -> Unit)? = null,
    onNavigateToFavorites: (() -> Unit)? = null,
    onNavigateToTrash: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val animSpec = LocalShredAnimation.current

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(animSpec.transitionDurationMs)
        ) + fadeIn(tween(animSpec.crossfadeDurationMs)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(animSpec.transitionDurationMs)
        ) + fadeOut(tween(animSpec.crossfadeDurationMs))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onDismiss() }
        ) {
            val isGlass = colors.isGlass
            val panelShape = RoundedCornerShape(
                bottomStart = if (isGlass) 24.dp else if (colors.cardCornerRadius > 0.dp) colors.cardCornerRadius else 0.dp,
                bottomEnd = if (isGlass) 24.dp else if (colors.cardCornerRadius > 0.dp) colors.cardCornerRadius else 0.dp
            )
            val innerShape = if (colors.cardCornerRadius > 0.dp) RoundedCornerShape(10.dp) else RoundedCornerShape(0.dp)
            val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {} // Prevent click-through
                    .then(
                        if (isGlass) {
                            Modifier
                                .shadow(16.dp, panelShape, ambientColor = Color(0x3300F2FE), spotColor = Color(0x22FF007F))
                                .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), panelShape)
                        } else if (colors.shadowElevation > 0.dp) {
                            Modifier
                                .shadow(colors.shadowElevation, panelShape)
                                .border(1.dp, colors.borderSubtle, panelShape)
                        } else {
                            Modifier.border(
                                width = colors.borderWidth,
                                color = colors.border,
                                shape = panelShape
                            )
                        }
                    )
                    .background(colors.surface, shape = panelShape)
                    .statusBarsPadding()
                    .padding(20.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -15f) {
                                onDismiss()
                            }
                        }
                    }
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SESSION STATS",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        color = colors.accent
                    )

                    val closeShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)
                    Box(
                        modifier = Modifier
                            .background(colors.cardBackground, closeShape)
                            .border(1.dp, colors.borderSubtle, closeShape)
                            .clickable { onDismiss() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "CLOSE ✕",
                            fontFamily = font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.cardBackground, innerShape)
                        .border(1.dp, colors.borderSubtle, innerShape)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REVIEWED",
                        fontFamily = font,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$currentPosition / $totalCount FILES",
                        fontFamily = font,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Interactive Stats Grid: Kept / Trashed / Favorited
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Kept
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.cardBackground, innerShape)
                            .border(1.dp, colors.borderSubtle, innerShape)
                            .clickable {
                                onDismiss()
                                onNavigateToKept?.invoke()
                            }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("KEPT ↗", fontFamily = font, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$keptCount",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = SwipeKeepGreen
                        )
                    }

                    // Staged (Tappable to view staging vault)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.cardBackground, innerShape)
                            .border(1.dp, colors.borderSubtle, innerShape)
                            .clickable {
                                onDismiss()
                                onNavigateToTrash?.invoke()
                            }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("STAGED ↗", fontFamily = font, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$deletedCount",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = SwipeDeleteRed
                        )
                    }

                    // Favorited (Tappable to open Favorites Vault)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.cardBackground, innerShape)
                            .border(1.dp, colors.borderSubtle, innerShape)
                            .clickable {
                                onDismiss()
                                onNavigateToFavorites?.invoke()
                            }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("FAVORITES ↗", fontFamily = font, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$favoritedCount",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = SwipeFavoriteGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Freed Storage Banner & Safe Staging Retention Notice
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.cardBackground, innerShape)
                        .border(1.dp, colors.borderSubtle, innerShape)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STAGED STORAGE",
                            fontFamily = font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = FileSizeFormatter.format(freedBytes),
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = colors.accent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Files are staged safely and auto-purged in ${trashRetentionHours}h",
                        fontFamily = font,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Folder Context & Switch Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.cardBackground, innerShape)
                        .border(1.dp, colors.borderSubtle, innerShape)
                        .clickable {
                            onDismiss()
                            onOpenFolderSelector()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT FOLDER",
                            fontFamily = font,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentFolderPath?.substringAfterLast('/')?.ifEmpty { "ALL STORAGE" } ?: "ALL STORAGE",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(colors.accent.copy(alpha = 0.15f), innerShape)
                            .border(1.dp, colors.accent, innerShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SWITCH",
                            fontFamily = font,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = colors.accent
                        )
                    }
                }

                // Drag indicator line at bottom
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(3.dp)
                        .background(colors.borderSubtle, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
