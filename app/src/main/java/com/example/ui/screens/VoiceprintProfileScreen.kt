package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongRepository
import com.example.model.VoiceProfile
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldMaster
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoiceprintProfileScreen(
    repository: SongRepository,
    onStartCalibration: () -> Unit
) {
    val activeProfile by repository.voiceProfile.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voiceprint Acoustics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Acoustic parameters extracted from your 30-second spoken calibration script.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Active Voice Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (activeProfile.isCalibrated) EmeraldMaster else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeProfile.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (activeProfile.isCalibrated) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldMaster.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("CALIBRATED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldMaster)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid of Metrics
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Fundamental F0 Range", fontSize = 11.sp, color = TextMuted)
                            Text(activeProfile.vocalRangeNote, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Column {
                            Text("Median Pitch", fontSize = 11.sp, color = TextMuted)
                            Text("${activeProfile.medianPitchHz.toInt()} Hz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Timbre Warmth", fontSize = 11.sp, color = TextMuted)
                            Text("${(activeProfile.timbreWarmth * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Formant Brightness", fontSize = 11.sp, color = TextMuted)
                            Text("${(activeProfile.formantBrightness * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MagentaVocal)
                        }
                        Column {
                            Text("Vibrato LFO Rate", fontSize = 11.sp, color = TextMuted)
                            Text("${String.format("%.1f", activeProfile.vibratoRateHz)} Hz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Ambient Noise Floor", fontSize = 11.sp, color = TextMuted)
                            Text("${activeProfile.noiseFloorDb.toInt()} dB", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldMaster)
                        }
                    }
                }
            }
        }

        // Speaker Embedding Radar Fingerprint Visualizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SPEAKER EMBEDDING SIGNATURE VECTOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "16-band neural vocal timbre distribution",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Canvas(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(8.dp)
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = size.width / 2.2f

                        // Draw background concentric guide rings
                        drawCircle(color = StudioCardBorder, radius = maxRadius * 0.33f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
                        drawCircle(color = StudioCardBorder, radius = maxRadius * 0.66f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
                        drawCircle(color = StudioCardBorder, radius = maxRadius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))

                        val signature = activeProfile.embeddingSignature
                        val numPoints = signature.size
                        val points = mutableListOf<Offset>()

                        for (i in 0 until numPoints) {
                            val angle = (i.toFloat() / numPoints) * 2f * Math.PI.toFloat()
                            val value = signature[i].coerceIn(0.1f, 1f)
                            val r = maxRadius * value
                            val x = center.x + r * cos(angle)
                            val y = center.y + r * sin(angle)
                            points.add(Offset(x, y))
                        }

                        // Connect points
                        for (i in 0 until numPoints) {
                            val next = points[(i + 1) % numPoints]
                            drawLine(
                                color = CyanAccent,
                                start = points[i],
                                end = next,
                                strokeWidth = 3f
                            )
                            drawCircle(
                                color = VioletAccent,
                                radius = 4f,
                                center = points[i]
                            )
                        }
                    }
                }
            }
        }

        // Action: Recalibrate Voice
        item {
            Button(
                onClick = onStartCalibration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("recalibrate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch 30s Voice Calibration",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Vocal Presets for instant testing
        item {
            Text(
                text = "Studio Timbre Presets",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Warm Studio Timbre", "A2 - G4 (Mezzo / Baritone)", 0.78f) to 0.68f,
                    Triple("Bright Ethereal Vocal", "C3 - A4 (Soprano / Tenor)", 0.60f) to 0.88f,
                    Triple("Low Husky Resonance", "E2 - C4 (Bass / Low Baritone)", 0.92f) to 0.52f
                ).forEach { (meta, brightness) ->
                    val (title, range, warmth) = meta
                    val isSelected = activeProfile.name == title
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val preset = VoiceProfile(
                                    id = "preset_${title.lowercase().replace(" ", "_")}",
                                    name = title,
                                    isCalibrated = true,
                                    vocalRangeNote = range,
                                    timbreWarmth = warmth,
                                    formantBrightness = brightness,
                                    medianPitchHz = if (warmth > 0.8f) 140f else if (brightness > 0.8f) 260f else 196f
                                )
                                repository.updateVoiceProfile(preset)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StudioSurfaceVariant else StudioSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanAccent else StudioCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(range, fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("Warmth: ${(warmth * 100).toInt()}%", fontSize = 11.sp, color = VioletAccent)
                        }
                    }
                }
            }
        }
    }
}
