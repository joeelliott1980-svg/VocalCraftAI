package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdvancedVocalFx
import com.example.model.DeEsserSettings
import com.example.model.DelaySettings
import com.example.model.EqBand
import com.example.model.EqualizerSettings
import com.example.model.ReverbSettings
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldMaster
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun AdvancedVocalFxSection(
    modifier: Modifier = Modifier,
    fx: AdvancedVocalFx,
    onFxChanged: (AdvancedVocalFx) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: EQ, 1: Reverb, 2: Delay

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("advanced_vocal_fx_card"),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with toggle & expand/collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Advanced Vocal Effects",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "3-Band EQ • Space Reverb • Stereo Delay",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = fx.isEnabled,
                        onCheckedChange = { onFxChanged(fx.copy(isEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyanAccent,
                            uncheckedTrackColor = StudioSurfaceVariant
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("fx_power_switch")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Default Studio" to { AdvancedVocalFx.defaultSettings() },
                            "Radio Pop Air" to { AdvancedVocalFx.radioPopAir() },
                            "Ethereal Dream Space" to { AdvancedVocalFx.etherealDreamSpace() },
                            "Warm Vintage Tube" to { AdvancedVocalFx.warmVintageTube() },
                            "Intimate Dry Lead" to { AdvancedVocalFx.intimateDryLead() }
                        ).forEach { (name, factory) ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onFxChanged(factory()) },
                                color = StudioSurfaceVariant
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub-tabs (3-Band EQ, Reverb, Delay)
                    TabRow(
                        selectedTabIndex = selectedSubTab,
                        containerColor = StudioSurfaceVariant,
                        contentColor = CyanAccent,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                                color = CyanAccent,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = selectedSubTab == 0,
                            onClick = { selectedSubTab = 0 },
                            text = { Text("3-Band EQ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedSubTab == 1,
                            onClick = { selectedSubTab = 1 },
                            text = { Text("Reverb", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedSubTab == 2,
                            onClick = { selectedSubTab = 2 },
                            text = { Text("Delay", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedSubTab == 3,
                            onClick = { selectedSubTab = 3 },
                            text = { Text("De-esser", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (selectedSubTab) {
                        0 -> EqualizerControls(
                            eq = fx.equalizer,
                            onEqChanged = { onFxChanged(fx.copy(equalizer = it)) }
                        )
                        1 -> ReverbControls(
                            reverb = fx.reverb,
                            onReverbChanged = { onFxChanged(fx.copy(reverb = it)) }
                        )
                        2 -> DelayControls(
                            delay = fx.delay,
                            onDelayChanged = { onFxChanged(fx.copy(delay = it)) }
                        )
                        3 -> DeEsserControls(
                            deEsser = fx.deEsser,
                            onDeEsserChanged = { onFxChanged(fx.copy(deEsser = it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeEsserControls(
    deEsser: DeEsserSettings,
    onDeEsserChanged: (DeEsserSettings) -> Unit
) {
    Column(modifier = Modifier.padding(10.dp)) {
        // Threshold
        Text("Threshold: ${deEsser.thresholdDb.toInt()} dB", fontSize = 12.sp, color = TextPrimary)
        Slider(
            value = deEsser.thresholdDb,
            onValueChange = { onDeEsserChanged(deEsser.copy(thresholdDb = it)) },
            valueRange = -40f..0f,
            colors = SliderDefaults.colors(thumbColor = MagentaVocal, activeTrackColor = MagentaVocal)
        )
        // Frequency
        Text("Frequency: ${deEsser.frequencyHz.toInt()} Hz", fontSize = 12.sp, color = TextPrimary)
        Slider(
            value = deEsser.frequencyHz,
            onValueChange = { onDeEsserChanged(deEsser.copy(frequencyHz = it)) },
            valueRange = 4000f..10000f,
            colors = SliderDefaults.colors(thumbColor = MagentaVocal, activeTrackColor = MagentaVocal)
        )
        // Intensity
        Text("Intensity: ${(deEsser.intensity * 100).toInt()}%", fontSize = 12.sp, color = TextPrimary)
        Slider(
            value = deEsser.intensity,
            onValueChange = { onDeEsserChanged(deEsser.copy(intensity = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = MagentaVocal, activeTrackColor = MagentaVocal)
        )
    }
}

@Composable
fun EqualizerControls(
    eq: EqualizerSettings,
    onEqChanged: (EqualizerSettings) -> Unit
) {
    Column {
        // Visual Parametric EQ Curve Graph
        EqCurveVisualizer(
            eq = eq,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StudioSurfaceVariant)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Low Band
        BandStrip(
            bandName = "Low Shelf",
            color = CyanAccent,
            band = eq.lowBand,
            freqRange = 60f..350f,
            freqUnit = "Hz",
            onBandChange = { onEqChanged(eq.copy(lowBand = it)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Mid Band
        BandStrip(
            bandName = "Mid Parametric",
            color = VioletAccent,
            band = eq.midBand,
            freqRange = 400f..4500f,
            freqUnit = "Hz",
            onBandChange = { onEqChanged(eq.copy(midBand = it)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. High Band
        BandStrip(
            bandName = "High Shelf / Air",
            color = MagentaVocal,
            band = eq.highBand,
            freqRange = 4000f..16000f,
            freqUnit = "Hz",
            onBandChange = { onEqChanged(eq.copy(highBand = it)) }
        )
    }
}

@Composable
fun BandStrip(
    bandName: String,
    color: Color,
    band: EqBand,
    freqRange: ClosedFloatingPointRange<Float>,
    freqUnit: String,
    onBandChange: (EqBand) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StudioSurfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(bandName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Text(
                    text = "${if (band.gainDb >= 0) "+" else ""}${String.format("%.1f", band.gainDb)} dB @ ${band.frequencyHz.toInt()} $freqUnit (Q: ${String.format("%.1f", band.qFactor)})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }

            // Gain slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gain", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(32.dp))
                Slider(
                    value = band.gainDb,
                    onValueChange = { onBandChange(band.copy(gainDb = it)) },
                    valueRange = -12f..12f,
                    colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color),
                    modifier = Modifier.weight(1f)
                )
            }

            // Frequency slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Freq", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(32.dp))
                Slider(
                    value = band.frequencyHz,
                    onValueChange = { onBandChange(band.copy(frequencyHz = it)) },
                    valueRange = freqRange,
                    colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color),
                    modifier = Modifier.weight(1f)
                )
            }

            // Q factor slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Q", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.width(32.dp))
                Slider(
                    value = band.qFactor,
                    onValueChange = { onBandChange(band.copy(qFactor = it)) },
                    valueRange = 0.4f..3.0f,
                    colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EqCurveVisualizer(
    eq: EqualizerSettings,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        // Draw 0dB center line
        drawLine(
            color = StudioCardBorder,
            start = Offset(0f, midY),
            end = Offset(w, midY),
            strokeWidth = 1f
        )

        // Frequency grid lines (100Hz, 1kHz, 10kHz)
        val f100X = (log10(100f / 20f) / log10(20000f / 20f)) * w
        val f1kX = (log10(1000f / 20f) / log10(20000f / 20f)) * w
        val f10kX = (log10(10000f / 20f) / log10(20000f / 20f)) * w

        drawLine(color = StudioCardBorder.copy(alpha = 0.5f), start = Offset(f100X, 0f), end = Offset(f100X, h), strokeWidth = 1f)
        drawLine(color = StudioCardBorder.copy(alpha = 0.5f), start = Offset(f1kX, 0f), end = Offset(f1kX, h), strokeWidth = 1f)
        drawLine(color = StudioCardBorder.copy(alpha = 0.5f), start = Offset(f10kX, 0f), end = Offset(f10kX, h), strokeWidth = 1f)

        // Plot composite EQ curve
        val path = Path()
        val numPoints = 64
        for (i in 0..numPoints) {
            val fraction = i.toFloat() / numPoints
            // log scale from 20 Hz to 20,000 Hz
            val freq = 20.0 * (20000.0 / 20.0).pow(fraction.toDouble())

            // Low contribution
            val lowDiff = (freq - eq.lowBand.frequencyHz) / (eq.lowBand.frequencyHz * 0.8)
            val lowResponse = eq.lowBand.gainDb / (1.0 + lowDiff.pow(2.0).coerceAtLeast(0.0))

            // Mid contribution (bell with Q)
            val midDiff = (log10(freq) - log10(eq.midBand.frequencyHz.toDouble())) * eq.midBand.qFactor * 3.0
            val midResponse = eq.midBand.gainDb / (1.0 + midDiff.pow(2.0))

            // High contribution
            val highDiff = (eq.highBand.frequencyHz - freq) / (eq.highBand.frequencyHz * 0.8)
            val highResponse = eq.highBand.gainDb / (1.0 + highDiff.pow(2.0).coerceAtLeast(0.0))

            val totalGainDb = (lowResponse + midResponse + highResponse).coerceIn(-14.0, 14.0)

            // Map dB (-14dB to +14dB) to canvas Y
            val y = midY - (totalGainDb / 14.0 * (h * 0.42)).toFloat()
            val x = fraction * w

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = CyanAccent,
            style = Stroke(width = 3f)
        )
    }
}

@Composable
fun ReverbControls(
    reverb: ReverbSettings,
    onReverbChanged: (ReverbSettings) -> Unit
) {
    Column {
        // Decay Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Decay Time", fontSize = 12.sp, color = TextSecondary)
            Text("${String.format("%.1f", reverb.decayTimeSec)} seconds", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
        }
        Slider(
            value = reverb.decayTimeSec,
            onValueChange = { onReverbChanged(reverb.copy(decayTimeSec = it)) },
            valueRange = 0.2f..6.0f,
            colors = SliderDefaults.colors(thumbColor = VioletAccent, activeTrackColor = VioletAccent),
            modifier = Modifier.testTag("reverb_decay_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Wet / Dry Mix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Wet / Dry Mix", fontSize = 12.sp, color = TextSecondary)
            Text("${(reverb.wetDryMix * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
        }
        Slider(
            value = reverb.wetDryMix,
            onValueChange = { onReverbChanged(reverb.copy(wetDryMix = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = VioletAccent, activeTrackColor = VioletAccent),
            modifier = Modifier.testTag("reverb_wet_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pre-Delay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Pre-Delay Offset", fontSize = 12.sp, color = TextSecondary)
            Text("${reverb.preDelayMs.toInt()} ms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Slider(
            value = reverb.preDelayMs,
            onValueChange = { onReverbChanged(reverb.copy(preDelayMs = it)) },
            valueRange = 0f..80f,
            colors = SliderDefaults.colors(thumbColor = TextPrimary, activeTrackColor = VioletAccent)
        )
    }
}

@Composable
fun DelayControls(
    delay: DelaySettings,
    onDelayChanged: (DelaySettings) -> Unit
) {
    Column {
        // Delay Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Delay Time", fontSize = 12.sp, color = TextSecondary)
            Text("${delay.timeMs} ms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
        }
        Slider(
            value = delay.timeMs.toFloat(),
            onValueChange = { onDelayChanged(delay.copy(timeMs = it.toInt())) },
            valueRange = 20f..1000f,
            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
            modifier = Modifier.testTag("delay_time_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Feedback
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Feedback Repeats", fontSize = 12.sp, color = TextSecondary)
            Text("${(delay.feedback * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
        }
        Slider(
            value = delay.feedback,
            onValueChange = { onDelayChanged(delay.copy(feedback = it)) },
            valueRange = 0f..0.90f,
            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
            modifier = Modifier.testTag("delay_feedback_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Wet / Dry Mix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Wet / Dry Mix", fontSize = 12.sp, color = TextSecondary)
            Text("${(delay.wetDryMix * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
        }
        Slider(
            value = delay.wetDryMix,
            onValueChange = { onDelayChanged(delay.copy(wetDryMix = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
            modifier = Modifier.testTag("delay_wet_slider")
        )
    }
}
