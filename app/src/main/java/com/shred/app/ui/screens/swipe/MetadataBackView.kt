package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.MediaFile
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MetadataBackView(
    file: MediaFile,
    onFlipBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(colors.cardCornerRadius)
    val innerShape = if (colors.cardCornerRadius > 0.dp) RoundedCornerShape(10.dp) else RoundedCornerShape(0.dp)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val dateStr = dateFormat.format(Date(if (file.dateTaken > 0) file.dateTaken else file.dateModified))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cardBackground, cardShape)
            .padding(18.dp)
    ) {
        // Fixed Top Header Bar - Clean typography & tap to flip back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, innerShape)
                .border(1.dp, colors.borderSubtle, innerShape)
                .clickable { onFlipBack() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILE METADATA",
                fontFamily = font,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
                color = colors.accent
            )

            Text(
                text = "FLIP BACK ↺",
                fontFamily = font,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Independently Scrollable Metadata Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetadataItem("FILE NAME", file.fileName, font, colors.surface, colors.borderSubtle, innerShape)
            MetadataItem("FILE SIZE", "${FileSizeFormatter.format(file.sizeBytes)} (${file.sizeBytes} bytes)", font, colors.surface, colors.borderSubtle, innerShape)
            MetadataItem("STORAGE PATH", file.path, font, colors.surface, colors.borderSubtle, innerShape)
            MetadataItem("DATE MODIFIED", dateStr, font, colors.surface, colors.borderSubtle, innerShape)
            MetadataItem("DIMENSIONS", if (file.width > 0 && file.height > 0) "${file.width} × ${file.height}" else "N/A", font, colors.surface, colors.borderSubtle, innerShape)
            if (file.durationMs > 0) {
                MetadataItem("DURATION", FileSizeFormatter.formatDuration(file.durationMs), font, colors.surface, colors.borderSubtle, innerShape)
            }
            MetadataItem("FORMAT TYPE", file.extension.uppercase(), font, colors.surface, colors.borderSubtle, innerShape)

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MetadataItem(
    label: String,
    value: String,
    font: FontFamily,
    surfaceColor: Color,
    borderColor: Color,
    shape: RoundedCornerShape
) {
    val colors = LocalShredColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontFamily = font,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontFamily = font,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = colors.textPrimary,
            softWrap = true
        )
    }
}
