package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.FolderGroup
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter

@Composable
fun FolderHeaderCard(
    folderGroup: FolderGroup,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cardBackground)
            .border(3.dp, colors.border)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "FOLDER DETECTED ///",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = folderGroup.folderName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = colors.textPrimary,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = folderGroup.folderPath,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = colors.textSecondary,
                maxLines = 3
            )
        }

        // Center Stats Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, colors.borderSubtle)
                .background(colors.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${folderGroup.fileCount} FILES",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TOTAL: ${FileSizeFormatter.format(folderGroup.totalSizeBytes)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
        }

        // Expand Button & Swipe Guidance
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, colors.accent)
                    .background(colors.surface)
                    .clickable { onExpandClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ TAP TO EXPAND INDIVIDUAL FILES ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = colors.accent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "OR SWIPE: LEFT=DELETE ALL • RIGHT=KEEP ALL",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = colors.textSecondary
            )
        }
    }
}
