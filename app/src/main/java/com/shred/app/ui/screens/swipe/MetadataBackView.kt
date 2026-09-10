package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val dateStr = dateFormat.format(Date(if (file.dateTaken > 0) file.dateTaken else file.dateModified))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cardBackground)
            .border(3.dp, colors.border)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "/// FILE METADATA",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 2.sp,
            color = colors.accent
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetadataRow("01. NAME", file.fileName)
        MetadataRow("02. SIZE", "${FileSizeFormatter.format(file.sizeBytes)} (${file.sizeBytes} bytes)")
        MetadataRow("03. PATH", file.path)
        MetadataRow("04. DATE", dateStr)
        MetadataRow("05. DIMS", if (file.width > 0 && file.height > 0) "${file.width} x ${file.height}" else "N/A")
        MetadataRow("06. DURATION", if (file.durationMs > 0) FileSizeFormatter.formatDuration(file.durationMs) else "N/A")
        MetadataRow("07. FORMAT", file.extension.uppercase())

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "▲ SWIPE UP TO FLIP BACK",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    val colors = LocalShredColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = colors.textPrimary
        )
    }
}
