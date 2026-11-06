package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElliottGold
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StudioCardBorderGlow
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioOnyx
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-bleed pro-audio splash view with centered pulsing audio glyph,
 * VocalCraft AI branding, and Elliott Media Group / Elliott Apps production anchor.
 */
@Composable
fun SplashView(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_audio_glyph_pulse")

    // Pulsing Scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Glowing Aura Alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Orbital Frequency Rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_rotation"
    )

    // Auto-transition to main interface after 2.2 seconds
    LaunchedEffect(Unit) {
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF181B2D),
                        StudioObsidian
                    ),
                    radius = 950f
                )
            )
            .clickable { onSplashFinished() }
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_view"),
        contentAlignment = Alignment.Center
    ) {
        // Centered Pulsing Audio Glyph & Main Branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Centered Pulsing Audio Glyph Container
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .testTag("pulsing_audio_glyph"),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Audio Wave Rings Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseR = 54.dp.toPx()

                    // Ring 1: Outer glowing gradient harmonic aura
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(CyanAccent, ElectricIndigo, VividViolet, MagentaVocal, CyanAccent)
                        ),
                        radius = baseR * pulseScale * 1.25f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx()),
                        alpha = glowAlpha * 0.45f
                    )

                    // Ring 2: Mid resonant neon frequency ring
                    drawCircle(
                        color = ElectricIndigo,
                        radius = baseR * pulseScale * 1.10f,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx()),
                        alpha = glowAlpha * 0.75f
                    )

                    // Ring 3: Tight inner energy halo
                    drawCircle(
                        color = CyanAccent,
                        radius = baseR * pulseScale * 0.95f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx()),
                        alpha = glowAlpha * 0.60f
                    )

                    // Orbital satellite audio nodes
                    for (i in 0 until 6) {
                        val theta = Math.toRadians((rotationAngle + i * 60.0)).toFloat()
                        val nodeR = baseR * pulseScale * 1.10f
                        val nx = center.x + nodeR * cos(theta)
                        val ny = center.y + nodeR * sin(theta)
                        drawCircle(
                            color = if (i % 2 == 0) CyanAccent else VividViolet,
                            radius = 3.5.dp.toPx(),
                            center = Offset(nx, ny)
                        )
                    }
                }

                // Center Illuminated Disc
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricIndigo, VividViolet, CyanAccent)
                            )
                        )
                        .padding(2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(StudioObsidian),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "VocalCraft Studio Glyph",
                            tint = CyanAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "VocalCraft AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Feature Subtitle Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElectricIndigo.copy(alpha = 0.20f))
                    .border(1.dp, ElectricIndigo.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "AI VOCAL STUDIO & MULTI-TRACK ENGINE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Studio-Quality Original Music & Covers\nSung in Your Calibrated Voice",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        // Bottom Anchor: An Elliott Media Group Production & Powered by Elliott Apps
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, StudioCardBorderGlow, RoundedCornerShape(12.dp)),
                color = StudioOnyx.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ElliottGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AN ELLIOTT MEDIA GROUP PRODUCTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Powered by Elliott Apps",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanAccent.copy(alpha = 0.85f),
                letterSpacing = 0.8.sp
            )
        }
    }
}
