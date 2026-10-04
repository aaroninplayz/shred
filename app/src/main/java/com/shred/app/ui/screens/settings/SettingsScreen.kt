package com.shred.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.*
import com.shred.app.ui.theme.LocalShredColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val colors = LocalShredColors.current

    var newPhraseText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    val isGlass = colors.isGlass
    val headerShape = if (isGlass) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = if (isGlass) Color.Transparent else colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (isGlass) Color.Transparent else colors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isGlass) {
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .shadow(8.dp, headerShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                                .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), headerShape)
                        } else if (colors.borderWidth > 0.dp) {
                            Modifier.border(2.dp, colors.borderSubtle)
                        } else Modifier
                    )
                    .background(colors.surface, headerShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    text = if (isGlass) "SYSTEM CONFIG" else "SYSTEM CONFIG ///",
                    fontFamily = font,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )
            }

            // Scrollable Settings List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Section 1: Appearance & Themes
                SettingsSectionHeader("01. APPEARANCE & THEME")

                SettingThemeSelector(
                    current = settings.themeMode,
                    onSelected = { viewModel.updateSettings(settings.copy(themeMode = it)) }
                )

                SettingColorModeSelector(
                    current = settings.colorMode,
                    onSelected = { viewModel.updateSettings(settings.copy(colorMode = it)) }
                )

                // Section 2: Sorting & Queues
                SettingsSectionHeader("02. QUEUE & SORTING")

                SettingSortOrderSelector(
                    current = settings.sortOrder,
                    onSelected = { viewModel.updateSettings(settings.copy(sortOrder = it)) }
                )

                // Section 2.1: Scan Filters & Exclusions
                SettingsSectionHeader("02B. SCANNING & FILTERS")

                SettingScanModeSelector(
                    current = settings.scanMode,
                    onSelected = { viewModel.updateSettings(settings.copy(scanMode = it)) }
                )

                SettingScanScopeSelector(
                    current = settings.scanScope,
                    onSelected = { viewModel.updateSettings(settings.copy(scanScope = it)) }
                )

                SettingSlider(
                    label = "MINIMUM FILE SIZE",
                    valueText = "${settings.minFileSizeBytes / (1024 * 1024)} MB",
                    value = (settings.minFileSizeBytes / (1024 * 1024)).toFloat(),
                    valueRange = 0f..200f,
                    onValueChange = { viewModel.updateSettings(settings.copy(minFileSizeBytes = (it.toLong() * 1024 * 1024))) }
                )

                Text(
                    text = "EXCLUDED FOLDERS FROM SCAN:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                val excludedFolders = viewModel.getExcludedFoldersList()
                excludedFolders.forEachIndexed { index, folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSubtle)
                            .background(colors.surface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = folder,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeExcludedFolder(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete exclusion",
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                var newFolderText by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newFolderText,
                        onValueChange = { newFolderText = it },
                        placeholder = { Text("FOLDER PATH...", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Box(
                        modifier = Modifier
                            .border(2.dp, colors.accent)
                            .clickable {
                                viewModel.addExcludedFolder(newFolderText)
                                newFolderText = ""
                            }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "+ ADD",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = colors.accent
                        )
                    }
                }

                // Section 3: Trash & Auto-Purge
                SettingsSectionHeader("03. TRASH RETENTION")

                val retentionDays = settings.trashRetentionHours / 24
                val retentionText = if (settings.trashRetentionHours < 24) {
                    "${settings.trashRetentionHours} HOURS"
                } else if (settings.trashRetentionHours % 24 == 0) {
                    "${settings.trashRetentionHours} HOURS ($retentionDays DAYS)"
                } else {
                    "${settings.trashRetentionHours} HOURS"
                }

                SettingSlider(
                    label = "TRASH RETENTION",
                    valueText = retentionText,
                    value = settings.trashRetentionHours.toFloat(),
                    valueRange = 1f..1440f,
                    onValueChange = { viewModel.updateSettings(settings.copy(trashRetentionHours = it.toInt())) }
                )

                // Section 4: Auto-Reset
                SettingsSectionHeader("04. KEPT FILES AUTO-RESET")

                SettingToggle(
                    label = "AUTO-RESET KEPT FILES",
                    checked = settings.autoResetEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(autoResetEnabled = it)) }
                )

                if (settings.autoResetEnabled) {
                    SettingSlider(
                        label = "AUTO-RESET INTERVAL",
                        valueText = "${settings.autoResetDays} DAYS",
                        value = settings.autoResetDays.toFloat(),
                        valueRange = 7f..30f,
                        onValueChange = { viewModel.updateSettings(settings.copy(autoResetDays = it.toInt())) }
                    )
                }

                // Section 5: Haptics & Sound
                SettingsSectionHeader("05. SENSORY FEEDBACK")

                SettingToggle(
                    label = "HAPTIC VIBRATIONS",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(hapticsEnabled = it)) }
                )

                if (settings.hapticsEnabled) {
                    SettingSlider(
                        label = "HAPTIC INTENSITY",
                        valueText = "${(settings.hapticIntensity * 100).toInt()}%",
                        value = settings.hapticIntensity,
                        valueRange = 0.1f..1.0f,
                        onValueChange = { viewModel.updateSettings(settings.copy(hapticIntensity = it)) }
                    )
                }

                SettingToggle(
                    label = "SOUND EFFECTS",
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(soundEnabled = it)) }
                )

                if (settings.soundEnabled) {
                    SettingSlider(
                        label = "SOUND VOLUME",
                        valueText = "${(settings.soundVolume * 100).toInt()}%",
                        value = settings.soundVolume,
                        valueRange = 0.1f..1.0f,
                        onValueChange = { viewModel.updateSettings(settings.copy(soundVolume = it)) }
                    )
                }

                // Section 06: Session Actions
                SettingsSectionHeader("06. SESSION RESET")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.5.dp, colors.border)
                        .background(colors.surface)
                        .clickable { viewModel.resetKeptSession() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[ RESET SESSION (RE-QUEUE ALL KEPT FILES) ]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    val shape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(9999.dp) else RoundedCornerShape(0.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), shape)
                        .background(colors.cardBackground, shape)
                } else {
                    Modifier.background(colors.invertedFill, shape)
                }
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            fontFamily = font,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = if (isGlass) colors.accent else colors.invertedText
        )
    }
}

@Composable
private fun SettingSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (isGlass) 16.dp else colors.cardCornerRadius)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier.shadow(colors.shadowElevation, cardShape)
                } else {
                    Modifier.border(1.5.dp, colors.borderSubtle, cardShape)
                }
            )
            .background(colors.surface, cardShape)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
            Text(valueText, fontFamily = font, fontWeight = FontWeight.Black, fontSize = 11.sp, color = colors.accent)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = colors.accent,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.borderSubtle
            )
        )
    }
}

@Composable
private fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (isGlass) 16.dp else colors.cardCornerRadius)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier.shadow(colors.shadowElevation, cardShape)
                } else {
                    Modifier.border(1.5.dp, colors.borderSubtle, cardShape)
                }
            )
            .background(colors.surface, cardShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accent,
                checkedTrackColor = colors.cardBackground
            )
        )
    }
}

@Composable
private fun SettingThemeSelector(current: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (isGlass) 16.dp else colors.cardCornerRadius)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier.shadow(colors.shadowElevation, cardShape)
                } else {
                    Modifier.border(colors.borderWidth, colors.borderSubtle, shape = cardShape)
                }
            )
            .background(colors.surface, shape = cardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("THEME SYSTEM", fontFamily = font, fontWeight = FontWeight.Black, fontSize = 11.sp, color = colors.accent)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeMode.values().forEach { mode ->
                val isSelected = current == mode
                val label = when (mode) {
                    ThemeMode.BRUTALISM -> "BRUTAL"
                    ThemeMode.MINIMALISM -> "MINIMAL"
                    ThemeMode.GLASSMORPHISM -> "GLASS"
                }
                val btnShape = RoundedCornerShape(if (mode == ThemeMode.GLASSMORPHISM) 12.dp else if (mode == ThemeMode.MINIMALISM) 8.dp else 0.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            shape = btnShape
                            clip = true
                        }
                        .then(
                            if (isSelected && isGlass) {
                                Modifier.border(1.5.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), btnShape)
                            } else {
                                Modifier.border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) colors.accent else colors.borderSubtle,
                                    shape = btnShape
                                )
                            }
                        )
                        .background(
                            if (isSelected) colors.cardBackground else colors.surface,
                            shape = btnShape
                        )
                        .clickable { onSelected(mode) }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = font,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = if (isSelected) colors.accent else colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSortOrderSelector(current: SortOrder, onSelected: (SortOrder) -> Unit) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (isGlass) 16.dp else colors.cardCornerRadius)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier.shadow(colors.shadowElevation, cardShape)
                } else {
                    Modifier.border(1.5.dp, colors.borderSubtle, cardShape)
                }
            )
            .background(colors.surface, cardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("SORT ORDER", fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        SortOrder.values().forEach { order ->
            val isSelected = current == order
            val itemShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle, itemShape)
                    .background(if (isSelected) colors.cardBackground else colors.surface, itemShape)
                    .clickable { onSelected(order) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.name.replace("_", " "), fontFamily = font, fontSize = 11.sp, color = if (isSelected) colors.accent else colors.textPrimary)
                if (isSelected) Text("●", fontFamily = font, fontSize = 11.sp, color = colors.accent)
            }
        }
    }
}

@Composable
private fun SettingColorModeSelector(current: ColorMode, onSelected: (ColorMode) -> Unit) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (isGlass) 16.dp else colors.cardCornerRadius)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier.shadow(colors.shadowElevation, cardShape)
                } else {
                    Modifier.border(1.5.dp, colors.borderSubtle, cardShape)
                }
            )
            .background(colors.surface, cardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("COLOR MODE", fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ColorMode.values().forEach { mode ->
                val isSelected = current == mode
                val btnShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle, btnShape)
                        .background(if (isSelected) colors.cardBackground else colors.surface, btnShape)
                        .clickable { onSelected(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name,
                        fontFamily = font,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSelected) colors.accent else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingScanModeSelector(current: ScanMode, onSelected: (ScanMode) -> Unit) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (isGlass) 16.dp else colors.cardCornerRadius)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier.shadow(colors.shadowElevation, cardShape)
                } else {
                    Modifier.border(1.5.dp, colors.borderSubtle, cardShape)
                }
            )
            .background(colors.surface, cardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("SCAN MODE", fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ScanMode.values().forEach { mode ->
                val isSelected = current == mode
                val btnShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle, btnShape)
                        .background(if (isSelected) colors.cardBackground else colors.surface, btnShape)
                        .clickable { onSelected(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name.replace("_", "\n"),
                        fontFamily = font,
                        fontSize = 9.sp,
                        color = if (isSelected) colors.accent else colors.textSecondary,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingScanScopeSelector(current: ScanScope, onSelected: (ScanScope) -> Unit) {
    val colors = LocalShredColors.current
    val isGlass = colors.isGlass
    val cardShape = RoundedCornerShape(if (isGlass) 16.dp else colors.cardCornerRadius)
    val font = if (isGlass || colors.cardCornerRadius > 0.dp) FontFamily.Default else FontFamily.Monospace
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGlass) {
                    Modifier
                        .shadow(6.dp, cardShape, ambientColor = androidx.compose.ui.graphics.Color(0x3300F2FE), spotColor = androidx.compose.ui.graphics.Color(0x22FF007F))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)), cardShape)
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier.shadow(colors.shadowElevation, cardShape)
                } else {
                    Modifier.border(1.5.dp, colors.borderSubtle, cardShape)
                }
            )
            .background(colors.surface, cardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SCAN SCOPE", fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
            Text(
                text = if (current == ScanScope.MEDIA_FOLDERS) "FAST // 1-2 SEC" else "DEEP CRAWL",
                fontFamily = font,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = colors.accent
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ScanScope.values().forEach { scope ->
                val isSelected = current == scope
                val btnShape = if (isGlass || colors.cardCornerRadius > 0.dp) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                val label = when (scope) {
                    ScanScope.MEDIA_FOLDERS -> "MEDIA FOLDERS\n(FAST)"
                    ScanScope.DEEP_STORAGE -> "DEEP STORAGE\n(FULL ROOT)"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle, btnShape)
                        .background(if (isSelected) colors.cardBackground else colors.surface, btnShape)
                        .clickable { onSelected(scope) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = font,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSelected) colors.accent else colors.textSecondary,
                        lineHeight = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

