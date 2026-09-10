package com.shred.app.ui.screens.loading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.LoadingAnimation
import com.shred.app.data.model.TransitionStyle
import com.shred.app.ui.theme.LocalShredColors
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    viewModel: LoadingViewModel,
    onNavigateToHome: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val colors = LocalShredColors.current

    LaunchedEffect(step) {
        if (step is LoadingStep.ReadyToTransition) {
            when (settings.transitionStyle) {
                TransitionStyle.FADE_TO_BLACK -> {
                    delay(200)
                    onNavigateToHome()
                }
                TransitionStyle.DIRECT,
                TransitionStyle.CROSS_DISSOLVE -> onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        when (val current = step) {
            is LoadingStep.Scanning -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Stark single brutalist line bar with NO numbers
                    val animatedProgress by animateFloatAsState(
                        targetValue = current.progress,
                        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
                        label = "loadingProgress"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .border(1.dp, colors.borderSubtle)
                            .background(colors.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(colors.accent)
                        )
                    }
                }
            }

            is LoadingStep.Blank -> {
                // Blank screen
            }

            is LoadingStep.Phrase -> {
                TypewriterOrAnimatedText(
                    text = current.text,
                    animation = current.animation,
                    color = colors.textPrimary
                )
            }

            is LoadingStep.ReadyToTransition -> {
                // Fading out into black
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }
        }
    }
}

@Composable
fun TypewriterOrAnimatedText(
    text: String,
    animation: LoadingAnimation,
    color: Color
) {
    var displayedChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(text, animation) {
        if (animation == LoadingAnimation.TYPEWRITER) {
            displayedChars = 0
            for (i in 1..text.length) {
                displayedChars = i
                delay(35)
            }
        } else {
            displayedChars = text.length
        }
    }

    when (animation) {
        LoadingAnimation.TYPEWRITER -> {
            Text(
                text = text.take(displayedChars) + if (displayedChars < text.length) "█" else "",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                color = color
            )
        }
        LoadingAnimation.FADE_IN -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Text(
                    text = text,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                    color = color
                )
            }
        }
        LoadingAnimation.SLIDE_IN -> {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
            ) {
                Text(
                    text = text,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                    color = color
                )
            }
        }
    }
}
