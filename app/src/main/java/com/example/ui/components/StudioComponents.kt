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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldMaster
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCardBorderGlow
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioOnyx
import com.example.ui.theme.StudioSlate
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VividViolet
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    progress: Float,
    primaryColor: Color = CyanAccent,
    secondaryColor: Color = VioletAccent,
    barCount: Int = 36
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. Fluid Ambient Glowing Wave Ribbon in background
        if (isPlaying) {
            val ribbonPath = Path()
            val points = 24
            for (p in 0..points) {
                val px = (p.toFloat() / points) * width
                val waveFactor = sin(phase + p * 0.45f) * 0.5f + cos(phase * 1.5f + p * 0.3f) * 0.5f
                val py = (height / 2f) + waveFactor * (height * 0.28f)
                if (p == 0) ribbonPath.moveTo(px, py) else ribbonPath.lineTo(px, py)
            }
            drawPath(
                path = ribbonPath,
                brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor, MagentaVocal)),
                style = Stroke(width = 3.dp.toPx()),
                alpha = 0.40f
            )
        }

        // 2. Neon Spectrum Frequency Columns
        val barWidth = (width / (barCount * 1.55f)).coerceAtLeast(3f)
        val spacing = barWidth * 0.55f
        val startX = (width - (barCount * (barWidth + spacing))) / 2f

        for (i in 0 until barCount) {
            val barProgress = i.toFloat() / barCount
            val isPlayed = barProgress <= progress

            val wave1 = sin(phase + i * 0.32f)
            val wave2 = sin(phase * 1.8f + i * 0.22f)
            val dynamicAmp = if (isPlaying) {
                ((wave1 * 0.5f + wave2 * 0.5f + 1f) * 0.5f).coerceIn(0.18f, 0.96f)
            } else {
                (sin(i * 0.38f) * 0.22f + 0.42f).coerceIn(0.18f, 0.75f)
            }

            val barHeight = (height * dynamicAmp).coerceAtLeast(6f)
            val x = startX + i * (barWidth + spacing)
            val y = (height - barHeight) / 2f

            val barBrush = if (isPlayed) {
                Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor)
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(StudioCardBorderGlow, StudioSlate)
                )
            }

            // Draw base bar
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Draw glowing peak cap if active
            if (isPlayed && isPlaying) {
                drawCircle(
                    color = primaryColor,
                    radius = barWidth * 0.6f,
                    center = Offset(x + barWidth / 2f, y)
                )
            }
        }

        // 3. Illuminated Playhead Scrub Line
        val scrubX = (startX + (progress * barCount * (barWidth + spacing))).coerceIn(0f, width)
        drawLine(
            color = Color.White,
            start = Offset(scrubX, 0f),
            end = Offset(scrubX, height),
            strokeWidth = 2.5.dp.toPx()
        )
        drawCircle(
            color = primaryColor,
            radius = 5.dp.toPx(),
            center = Offset(scrubX, height / 2f)
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = Offset(scrubX, height / 2f)
        )
    }
}

@Composable
fun StemChannelStrip(
    modifier: Modifier = Modifier,
    stemName: String,
    stemColor: Color,
    volume: Float,
    isSolo: Boolean,
    isMute: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleSolo: () -> Unit,
    onToggleMute: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vu_meter_pulse")
    val vuOsc by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vu"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp)),
        color = StudioSlate
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Stem Name + Solo / Mute Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(stemColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stemName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Glowing Solo Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSolo) stemColor else StudioOnyx)
                            .border(1.dp, if (isSolo) stemColor else StudioCardBorder, RoundedCornerShape(6.dp))
                            .clickable { onToggleSolo() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SOLO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSolo) Color.Black else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Glowing Mute Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMute) Color(0xFFEF4444) else StudioOnyx)
                            .border(1.dp, if (isMute) Color(0xFFEF4444) else StudioCardBorder, RoundedCornerShape(6.dp))
                            .clickable { onToggleMute() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MUTE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMute) Color.White else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mixing Console Fader Row with Reactive Stereo VU Peak Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isMute || volume == 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volume Icon",
                    tint = if (isMute) Color(0xFFEF4444) else stemColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Illuminated Volume Fader
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1.2f,
                    colors = SliderDefaults.colors(
                        thumbColor = stemColor,
                        activeTrackColor = stemColor,
                        inactiveTrackColor = StudioObsidian
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("slider_${stemName.lowercase().replace(" ", "_").take(20)}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Reactive Stereo LED VU Peak Meter Ladder (L & R columns)
                val effectiveLevel = if (isMute) 0f else (volume * vuOsc).coerceIn(0f, 1.2f)
                StereoVuMeter(
                    level = effectiveLevel,
                    modifier = Modifier
                        .width(18.dp)
                        .height(28.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Percentage Readout
                Text(
                    text = "${(volume * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMute) Color(0xFFEF4444) else TextPrimary,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
private fun StereoVuMeter(
    level: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val totalSegments = 8
        val segHeight = (size.height / totalSegments) - 1.5f
        val segWidth = (size.width / 2f) - 2f

        for (ch in 0..1) {
            val chX = ch * (segWidth + 3f)
            val chLevel = if (ch == 0) level else level * 0.94f // slight stereo discrepancy

            for (s in 0 until totalSegments) {
                val segIdx = totalSegments - 1 - s
                val threshold = (s + 1).toFloat() / totalSegments
                val isActive = chLevel >= threshold * 0.85f

                val segColor = when {
                    s >= 6 -> if (isActive) Color(0xFFEF4444) else Color(0x33EF4444) // Peak Red
                    s >= 4 -> if (isActive) Color(0xFFF59E0B) else Color(0x33F59E0B) // Amber
                    else -> if (isActive) EmeraldMaster else Color(0x3310B981)        // Green
                }

                drawRoundRect(
                    color = segColor,
                    topLeft = Offset(chX, segIdx * (segHeight + 1.5f)),
                    size = Size(segWidth, segHeight),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun VoiceStatusBadge(
    modifier: Modifier = Modifier,
    isCalibrated: Boolean,
    voiceName: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, if (isCalibrated) CyanAccent.copy(alpha = 0.6f) else StudioCardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = StudioSlate
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isCalibrated) CyanAccent else Color(0xFFF59E0B))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = if (isCalibrated) CyanAccent else TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isCalibrated) voiceName else "Calibrate Voice",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isCalibrated) TextPrimary else TextSecondary
            )
        }
    }
}
