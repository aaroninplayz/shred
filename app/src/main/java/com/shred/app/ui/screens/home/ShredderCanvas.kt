package com.shred.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.ui.theme.LocalShredColors

@Composable
fun ShredderMachine(
    modifier: Modifier = Modifier,
    isHovered: Boolean = false,
    onSettingsClick: () -> Unit
) {
    val colors = LocalShredColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "shredder")
    val bladeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isHovered) 200 else 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bladeAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Feed Slot Label & Settings Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FEED SLOT /// DROP TO CLEAN",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = colors.textSecondary
            )

            Box(
                modifier = Modifier
                    .border(2.dp, colors.border, RoundedCornerShape(0.dp))
                    .clickable { onSettingsClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "[SETTINGS]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = colors.accent
                )
            }
        }

        // Shredder Machine Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .border(3.dp, if (isHovered) colors.accent else colors.border)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw background dark metal
                drawRect(
                    color = if (isHovered) colors.cardBackground.copy(alpha = 0.9f) else colors.cardBackground,
                    topLeft = Offset.Zero,
                    size = size
                )

                // Draw Top Intake Funnel / Slot
                val slotHeight = 28f
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(24f, 10f),
                    size = Size(w - 48f, slotHeight)
                )
                drawRect(
                    color = colors.border,
                    topLeft = Offset(24f, 10f),
                    size = Size(w - 48f, slotHeight),
                    style = Stroke(width = 2f)
                )

                // Draw Mechanical Cutting Blades
                val toothWidth = 20f
                val toothHeight = 16f
                val teethStartX = 28f
                val teethEndX = w - 28f
                val teethY = 50f

                val bladePath = Path()
                var currentX = teethStartX - bladeOffset
                while (currentX < teethEndX + toothWidth * 2) {
                    if (currentX >= teethStartX - toothWidth) {
                        bladePath.moveTo(currentX, teethY)
                        bladePath.lineTo(currentX + toothWidth / 2, teethY + toothHeight)
                        bladePath.lineTo(currentX + toothWidth, teethY)
                    }
                    currentX += toothWidth
                }

                drawPath(
                    path = bladePath,
                    color = colors.border,
                    style = Stroke(width = 3f)
                )

                // Secondary lower blades
                val lowerBladePath = Path()
                var lowerX = teethStartX + bladeOffset - toothWidth
                val lowerY = 80f
                while (lowerX < teethEndX + toothWidth * 2) {
                    if (lowerX >= teethStartX - toothWidth) {
                        lowerBladePath.moveTo(lowerX, lowerY + toothHeight)
                        lowerBladePath.lineTo(lowerX + toothWidth / 2, lowerY)
                        lowerBladePath.lineTo(lowerX + toothWidth, lowerY + toothHeight)
                    }
                    lowerX += toothWidth
                }

                drawPath(
                    path = lowerBladePath,
                    color = colors.borderSubtle,
                    style = Stroke(width = 2.5f)
                )

                // Industrial hazard stripes at bottom of shredder
                val stripeWidth = 14f
                var stripeX = 0f
                while (stripeX < w) {
                    drawRect(
                        color = if ((stripeX / stripeWidth).toInt() % 2 == 0) colors.border else Color.Transparent,
                        topLeft = Offset(stripeX, h - 12f),
                        size = Size(stripeWidth, 12f)
                    )
                    stripeX += stripeWidth
                }
            }

            // Central Machine Stencil Text
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SHRED // INDUSTRIAL-X",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = colors.textPrimary
                )
            }
        }
    }
}
