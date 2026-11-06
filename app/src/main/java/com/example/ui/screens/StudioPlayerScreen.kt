package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackManager
import com.example.audio.AudioSynthesizer
import com.example.data.SongRepository
import com.example.model.AudioMasteringProfile
import com.example.model.AudioStems
import com.example.model.QcReport
import com.example.model.SongProject
import com.example.model.VoiceProfile
import com.example.ui.components.AdvancedVocalFxSection
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.StemChannelStrip
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldMaster
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StemHarmonyColor
import com.example.ui.theme.StemInstrumentColor
import com.example.ui.theme.StemVocalColor
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun StudioPlayerScreen(
    repository: SongRepository,
    onOpenShare: (SongProject) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val playbackManager = remember { AudioPlaybackManager(context) }
    val playbackState by playbackManager.playbackState.collectAsState()

    val activeProject by repository.activeProject.collectAsState()
    val activeProfile by repository.voiceProfile.collectAsState()

    var stemsState by remember(activeProject?.id) {
        mutableStateOf(activeProject?.stems ?: AudioStems())
    }

    var isReMastering by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            playbackManager.stop()
        }
    }

    // Play active project if audio exists
    LaunchedEffect(activeProject?.id) {
        activeProject?.masterWavPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                playbackManager.playFile(file, isLooping = true)
            }
        }
    }

    if (activeProject == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No song loaded. Create an original or cover in the studio!", color = TextSecondary)
        }
        return
    }

    val project = activeProject!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Track Header & Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (project.isCover) "Cover (${project.coverOriginalArtist ?: "Original"}) • Sung by ${project.voiceProfileUsed}" else "Original • Sung by ${project.voiceProfileUsed}",
                        fontSize = 12.sp,
                        color = VioletAccent
                    )
                }

                Button(
                    onClick = { onOpenShare(project) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("export_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export & Share", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Metadata Tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    project.genre to StudioSurfaceVariant,
                    "${project.tempoBpm} BPM" to StudioSurfaceVariant,
                    project.musicalKey to StudioSurfaceVariant,
                    project.emotionTag to CyanAccent.copy(alpha = 0.2f)
                ).forEach { (tag, bg) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .border(1.dp, StudioCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (bg == StudioSurfaceVariant) TextSecondary else CyanAccent
                        )
                    }
                }
            }
        }

        // Animated Audio Waveform Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AudioWaveformVisualizer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clickable {
                                // Click to scrub
                            },
                        isPlaying = playbackState.isPlaying,
                        progress = playbackState.progress,
                        primaryColor = CyanAccent,
                        secondaryColor = VioletAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Slider
                    Slider(
                        value = playbackState.progress,
                        onValueChange = { fraction ->
                            playbackManager.seekToFraction(fraction)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = StudioCardBorder
                        ),
                        modifier = Modifier.testTag("player_seek_slider")
                    )

                    // Timestamps & Transport Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentSec = playbackState.currentPositionMs / 1000
                        val totalSec = if (playbackState.durationMs > 0) playbackState.durationMs / 1000 else project.durationSeconds
                        Text(
                            text = String.format("%02d:%02d", currentSec / 60, currentSec % 60),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Replay
                            FilledIconButton(
                                onClick = { playbackManager.seekToFraction(0f) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = StudioSurfaceVariant),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = "Restart", tint = TextPrimary, modifier = Modifier.size(18.dp))
                            }

                            // Play / Pause
                            FilledIconButton(
                                onClick = {
                                    val file = File(project.masterWavPath)
                                    if (file.exists() && playbackState.activeTrackPath != file.absolutePath) {
                                        playbackManager.playFile(file, isLooping = isLooping)
                                    } else {
                                        playbackManager.togglePlayPause()
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = CyanAccent),
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Loop Toggle
                            FilledIconButton(
                                onClick = {
                                    isLooping = !isLooping
                                    File(project.masterWavPath).takeIf { it.exists() }?.let {
                                        playbackManager.playFile(it, isLooping = isLooping)
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isLooping) CyanAccent.copy(alpha = 0.25f) else StudioSurfaceVariant
                                ),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Loop,
                                    contentDescription = "Loop",
                                    tint = if (isLooping) CyanAccent else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = String.format("%02d:%02d", totalSec / 60, totalSec % 60),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Live Synchronized Lyrics Teleprompter Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYNCHRONIZED VOCAL LYRICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VioletAccent
                        )
                        val activeSection = when {
                            playbackState.progress < 0.12f -> "Intro"
                            playbackState.progress < 0.35f -> "Verse 1"
                            playbackState.progress < 0.50f -> "Pre-Chorus"
                            playbackState.progress < 0.85f -> "Chorus"
                            else -> "Outro"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(VioletAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Current: $activeSection", fontSize = 10.sp, color = VioletAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val lines = project.lyrics.getLyricLines()
                    lines.forEach { (section, text) ->
                        val isHighlighted = when (section) {
                            "Intro" -> playbackState.progress < 0.12f
                            "Verse 1" -> playbackState.progress in 0.12f..0.35f
                            "Pre-Chorus" -> playbackState.progress in 0.35f..0.50f
                            "Chorus" -> playbackState.progress in 0.50f..0.85f
                            else -> playbackState.progress >= 0.85f
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isHighlighted) CyanAccent.copy(alpha = 0.12f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "[$section]",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighlighted) CyanAccent else TextMuted
                            )
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isHighlighted) TextPrimary else TextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // Multitrack Stem Mixer Channel Strips
        item {
            Text(
                text = "Multitrack Stem Mixer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Stem 1: User Vocals
        item {
            StemChannelStrip(
                stemName = "AI Lead Vocals (${project.voiceProfileUsed.take(18)})",
                stemColor = StemVocalColor,
                volume = stemsState.vocalVolume,
                isSolo = stemsState.vocalSolo,
                isMute = stemsState.vocalMute,
                onVolumeChange = {
                    stemsState = stemsState.copy(vocalVolume = it)
                },
                onToggleSolo = {
                    stemsState = stemsState.copy(vocalSolo = !stemsState.vocalSolo)
                },
                onToggleMute = {
                    stemsState = stemsState.copy(vocalMute = !stemsState.vocalMute)
                }
            )
        }

        // Stem 2: Harmonies & Backing
        item {
            StemChannelStrip(
                stemName = "Vocal Harmonies & Chorus Layer",
                stemColor = StemHarmonyColor,
                volume = stemsState.harmonyVolume,
                isSolo = stemsState.harmonySolo,
                isMute = stemsState.harmonyMute,
                onVolumeChange = {
                    stemsState = stemsState.copy(harmonyVolume = it)
                },
                onToggleSolo = {
                    stemsState = stemsState.copy(harmonySolo = !stemsState.harmonySolo)
                },
                onToggleMute = {
                    stemsState = stemsState.copy(harmonyMute = !stemsState.harmonyMute)
                }
            )
        }

        // Stem 3: Instrumental & Rhythm
        item {
            StemChannelStrip(
                stemName = "Accompaniment, Bass & Drums",
                stemColor = StemInstrumentColor,
                volume = stemsState.instrumentalVolume,
                isSolo = stemsState.instrumentalSolo,
                isMute = stemsState.instrumentalMute,
                onVolumeChange = {
                    stemsState = stemsState.copy(instrumentalVolume = it)
                },
                onToggleSolo = {
                    stemsState = stemsState.copy(instrumentalSolo = !stemsState.instrumentalSolo)
                },
                onToggleMute = {
                    stemsState = stemsState.copy(instrumentalMute = !stemsState.instrumentalMute)
                }
            )
        }

        // Advanced Vocal Effects (Reverb, Delay, 3-Band Parametric EQ)
        item {
            AdvancedVocalFxSection(
                fx = stemsState.advancedFx,
                onFxChanged = { updatedFx ->
                    stemsState = stemsState.copy(advancedFx = updatedFx)
                }
            )
        }

        // Studio Master FX Rack
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = EmeraldMaster, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Studio FX Rack & Mastering",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto-Tune Strength
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-Tune Pitch Correction", fontSize = 12.sp, color = TextSecondary)
                        Text("${(stemsState.autoTuneStrength * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MagentaVocal)
                    }
                    Slider(
                        value = stemsState.autoTuneStrength,
                        onValueChange = { stemsState = stemsState.copy(autoTuneStrength = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = MagentaVocal, activeTrackColor = MagentaVocal),
                        modifier = Modifier.testTag("fx_autotune_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Reverb Decay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Studio Reverb Space", fontSize = 12.sp, color = TextSecondary)
                        Text("${(stemsState.reverbDecay * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
                    }
                    Slider(
                        value = stemsState.reverbDecay,
                        onValueChange = { stemsState = stemsState.copy(reverbDecay = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = VioletAccent, activeTrackColor = VioletAccent),
                        modifier = Modifier.testTag("fx_reverb_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stereo Width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Stereo Widening (M/S)", fontSize = 12.sp, color = TextSecondary)
                        Text("${(stemsState.stereoWidth * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                    Slider(
                        value = stemsState.stereoWidth,
                        onValueChange = { stemsState = stemsState.copy(stereoWidth = it) },
                        valueRange = 0.2f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                        modifier = Modifier.testTag("fx_width_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Re-Master Button with updated stems & FX
                    Button(
                        onClick = {
                            isReMastering = true
                            coroutineScope.launch {
                                val updatedProject = project.copy(stems = stemsState)
                                val file = File(updatedProject.masterWavPath)
                                withContext(Dispatchers.IO) {
                                    val (_, qc) = AudioSynthesizer.renderMasterTrack(
                                        project = updatedProject,
                                        voiceProfile = activeProfile,
                                        outputWavFile = file
                                    )
                                    val finalProject = updatedProject.copy(qcReport = qc)
                                    repository.saveProject(finalProject)
                                }
                                playbackManager.playFile(file, isLooping = isLooping)
                                isReMastering = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("remaster_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMaster),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isReMastering
                    ) {
                        if (isReMastering) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Re-Mastering to -14 LUFS...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Stem Mix & Re-Master", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Advanced 3-Band Parametric EQ & Reverb/Delay Suite
        item {
            AdvancedVocalFxSection(
                fx = stemsState.advancedFx,
                onFxChanged = { newFx ->
                    stemsState = stemsState.copy(advancedFx = newFx)
                }
            )
        }

        // Production Mastering & QC Telemetry Console
        item {
            MasteringQcConsole(
                project = project,
                activeProfile = activeProfile
            )
        }
    }
}

@Composable
fun MasteringQcConsole(
    project: SongProject,
    activeProfile: VoiceProfile
) {
    val qc = project.qcReport ?: QcReport()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mastering_qc_console_card"),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Certification Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldMaster,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STUDIO MASTER TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldMaster
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldMaster.copy(alpha = 0.2f))
                        .border(1.dp, EmeraldMaster.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (qc.isCertified) "RADIO-READY CERTIFIED" else "ACTIVE MONITOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldMaster
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Grid Telemetry Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QcMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "INTEGRATED LUFS",
                    value = String.format("%.1f", qc.measuredLufs),
                    target = "Target: -14.0",
                    accentColor = CyanAccent
                )
                QcMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "TRUE PEAK",
                    value = "${String.format("%.2f", qc.peakDbFs)} dB",
                    target = "Ceil: -1.0 dBFS",
                    accentColor = EmeraldMaster
                )
                QcMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "DYNAMIC RANGE",
                    value = "${String.format("%.1f", qc.dynamicRangeDb)} dB",
                    target = "Pop Profile",
                    accentColor = VioletAccent
                )
                QcMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "DIGITAL CLIPS",
                    value = "${qc.clippingOccurrences}",
                    target = "0 Overloads",
                    accentColor = if (qc.clippingOccurrences == 0) EmeraldMaster else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pipeline Specifications List
            Text(
                text = "PRODUCTION PIPELINE SPECIFICATIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Spec Item 1: Vocal Naturalizer
            SpecRow(
                title = "Vocal Naturalizer & Retune",
                spec = "25ms adaptive retune speed, CREPE/Harvest F0 locked to ${project.musicalKey}, natural breath intakes & 35ms portamento",
                accent = MagentaVocal
            )

            // Spec Item 2: Stereo Vocal Doubles
            SpecRow(
                title = "Dual Vocal Doubles",
                spec = "Centered Lead + Dual Side-Panned Doubles (35% L/R, -4.0 dB, 12ms Haas delay offset)",
                accent = CyanAccent
            )

            // Spec Item 3: Dynamic Sidechain Ducking
            SpecRow(
                title = "Vocal Pocket Sidechain",
                spec = "Instrumental ducked 2.2 dB between 1 kHz – 3.5 kHz on active vocal frames",
                accent = VioletAccent
            )

            // Spec Item 4: Analog Warmth & Limiting
            SpecRow(
                title = "Mastering Bus Processing",
                spec = "30Hz rumble cut, <120Hz mono sub-bass lock, tanh tape warmth, brickwall peak limiting",
                accent = EmeraldMaster
            )
        }
    }
}

@Composable
private fun QcMetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    target: String,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StudioSurfaceVariant)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = target, fontSize = 8.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun SpecRow(
    title: String,
    spec: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = spec, fontSize = 10.sp, color = TextSecondary, lineHeight = 13.sp)
        }
    }
}
