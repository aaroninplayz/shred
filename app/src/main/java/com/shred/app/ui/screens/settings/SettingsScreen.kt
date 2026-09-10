package com.shred.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, colors.borderSubtle)
                    .background(colors.surface)
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
                    text = "SYSTEM CONFIG ///",
                    fontFamily = FontFamily.Monospace,
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

                SettingProgressBarStyleSelector(
                    current = settings.progressBarStyle,
                    onSelected = { viewModel.updateSettings(settings.copy(progressBarStyle = it)) }
                )

                // Section 2: Sorting & Queues
                SettingsSectionHeader("02. QUEUE & SORTING")

                SettingSortOrderSelector(
                    current = settings.sortOrder,
                    onSelected = { viewModel.updateSettings(settings.copy(sortOrder = it)) }
                )

                // Section 3: Trash & Auto-Purge
                SettingsSectionHeader("03. TRASH RETENTION")

                SettingSlider(
                    label = "TRASH RETENTION",
                    valueText = "${settings.trashRetentionHours} HOURS (${settings.trashRetentionHours / 24} DAYS)",
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

                // Section 6: Loading Screen Customization
                SettingsSectionHeader("06. CINEMATIC LOADING")

                SettingLoadingAnimationSelector(
                    current = settings.loadingAnimation,
                    onSelected = { viewModel.updateSettings(settings.copy(loadingAnimation = it)) }
                )

                SettingTransitionSelector(
                    current = settings.transitionStyle,
                    onSelected = { viewModel.updateSettings(settings.copy(transitionStyle = it)) }
                )

                // Custom phrases pool
                Text(
                    text = "CUSTOM LOADING PHRASES POOL:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                val phrases = viewModel.getLoadingPhrasesList()
                phrases.forEachIndexed { index, phrase ->
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
                            text = phrase,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeLoadingPhrase(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete phrase",
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPhraseText,
                        onValueChange = { newPhraseText = it },
                        placeholder = { Text("NEW PHRASE...", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Box(
                        modifier = Modifier
                            .border(2.dp, colors.accent)
                            .clickable {
                                viewModel.addLoadingPhrase(newPhraseText)
                                newPhraseText = ""
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

                // Section 7: Session Actions
                SettingsSectionHeader("07. SESSION RESET")

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
    Text(
        text = title,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 13.sp,
        letterSpacing = 1.sp,
        color = colors.accent,
        modifier = Modifier.padding(top = 8.dp)
    )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.borderSubtle)
            .background(colors.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
            Text(valueText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp, color = colors.accent)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.borderSubtle)
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textPrimary)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.borderSubtle)
            .background(colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("THEME FLAVOR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ThemeMode.values().forEach { mode ->
                val isSelected = current == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle)
                        .background(if (isSelected) colors.cardBackground else colors.surface)
                        .clickable { onSelected(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name.replace("_", "\n"),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        fontSize = 10.sp,
                        color = if (isSelected) colors.accent else colors.textSecondary,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSortOrderSelector(current: SortOrder, onSelected: (SortOrder) -> Unit) {
    val colors = LocalShredColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.borderSubtle)
            .background(colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("SORT ORDER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        SortOrder.values().forEach { order ->
            val isSelected = current == order
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle)
                    .background(if (isSelected) colors.cardBackground else colors.surface)
                    .clickable { onSelected(order) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.name.replace("_", " "), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (isSelected) colors.accent else colors.textPrimary)
                if (isSelected) Text("●", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = colors.accent)
            }
        }
    }
}

@Composable
private fun SettingProgressBarStyleSelector(current: ProgressBarStyle, onSelected: (ProgressBarStyle) -> Unit) {
    val colors = LocalShredColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.borderSubtle)
            .background(colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("PROGRESS BAR STYLE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        ProgressBarStyle.values().forEach { style ->
            val isSelected = current == style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle)
                    .background(if (isSelected) colors.cardBackground else colors.surface)
                    .clickable { onSelected(style) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(style.name.replace("_", " "), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (isSelected) colors.accent else colors.textPrimary)
                if (isSelected) Text("●", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = colors.accent)
            }
        }
    }
}

@Composable
private fun SettingLoadingAnimationSelector(current: LoadingAnimation, onSelected: (LoadingAnimation) -> Unit) {
    val colors = LocalShredColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.borderSubtle)
            .background(colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("LOADING TEXT ANIMATION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LoadingAnimation.values().forEach { anim ->
                val isSelected = current == anim
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle)
                        .clickable { onSelected(anim) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = anim.name,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isSelected) colors.accent else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingTransitionSelector(current: TransitionStyle, onSelected: (TransitionStyle) -> Unit) {
    val colors = LocalShredColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colors.borderSubtle)
            .background(colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("TRANSITION STYLE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TransitionStyle.values().forEach { trans ->
                val isSelected = current == trans
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.accent else colors.borderSubtle)
                        .clickable { onSelected(trans) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trans.name.replace("_", "\n"),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = if (isSelected) colors.accent else colors.textSecondary,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}
