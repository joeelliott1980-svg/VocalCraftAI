package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSynthesizer
import com.example.data.PresetSongs
import com.example.data.SongRepository
import com.example.model.AudioStems
import com.example.model.SongProject
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun CoverStudioScreen(
    repository: SongRepository,
    onCoverGenerated: (SongProject) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activeProfile by repository.voiceProfile.collectAsState()

    var selectedCover by remember { mutableStateOf(PresetSongs.COVER_PRESETS[0]) }
    var customAudioUri by remember { mutableStateOf<Uri?>(null) }
    var customSongTitle by remember { mutableStateOf<String?>(null) }

    var pitchShiftSemitones by remember { mutableIntStateOf(0) }
    var timbreTransferStrength by remember { mutableFloatStateOf(0.85f) }
    var autoTuneSnap by remember { mutableFloatStateOf(0.70f) }

    var isConverting by remember { mutableStateOf(false) }
    var conversionProgress by remember { mutableFloatStateOf(0f) }
    var conversionStage by remember { mutableStateOf("Isolating vocal stems...") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customAudioUri = uri
            customSongTitle = "Imported Audio (${uri.lastPathSegment ?: "Track"})"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = VioletAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cover Engine",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Transform reference songs into covers sung in your calibrated voice with on-device stem isolation and timbre transfer.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Voice Badge Indicator
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = StudioSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CyanAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Target Vocal Timbre:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeProfile.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = activeProfile.vocalRangeNote,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanAccent
                    )
                }
            }
        }

        // Track Selection Section
        item {
            Text(
                text = "1. Select Royalty-Free Track or Upload",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(PresetSongs.COVER_PRESETS) { track ->
            val isSelected = selectedCover.id == track.id && customAudioUri == null
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedCover = track
                        customAudioUri = null
                    }
                    .testTag("cover_item_${track.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) StudioSurfaceVariant else StudioSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) VioletAccent else StudioCardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) VioletAccent.copy(alpha = 0.25f) else StudioSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = if (isSelected) VioletAccent else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${track.genre} • ${track.bpm} BPM • ${track.key}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = track.description,
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 14.sp
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VioletAccent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("SELECTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Upload Custom Audio File Button
        item {
            OutlinedButton(
                onClick = { filePickerLauncher.launch("audio/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_audio_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = customSongTitle ?: "Or Import Custom Reference Audio (WAV/MP3)",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Stem Separation Pipeline Visualizer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. Hybrid Transformer Stem Isolation",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Lead Vocal" to MagentaVocal,
                            "Harmony" to VioletAccent,
                            "Instruments" to CyanAccent,
                            "Drums/Bass" to EmeraldMaster
                        ).forEach { (stem, color) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioSurfaceVariant)
                                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stem,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Isolated",
                                    fontSize = 9.sp,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }

        // Conversion Settings Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3. Vocal Timbre Transfer Controls",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timbre Transfer Strength
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Timbre Transfer Depth", fontSize = 12.sp, color = TextSecondary)
                        Text("${(timbreTransferStrength * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                    Slider(
                        value = timbreTransferStrength,
                        onValueChange = { timbreTransferStrength = it },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                        modifier = Modifier.testTag("timbre_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pitch Shift
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pitch Transpose", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = if (pitchShiftSemitones >= 0) "+$pitchShiftSemitones Semitones" else "$pitchShiftSemitones Semitones",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VioletAccent
                        )
                    }
                    Slider(
                        value = pitchShiftSemitones.toFloat(),
                        onValueChange = { pitchShiftSemitones = it.toInt() },
                        valueRange = -6f..6f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = VioletAccent, activeTrackColor = VioletAccent),
                        modifier = Modifier.testTag("pitch_shift_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto-Tune Strength
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-Tune Pitch Snap", fontSize = 12.sp, color = TextSecondary)
                        Text("${(autoTuneSnap * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MagentaVocal)
                    }
                    Slider(
                        value = autoTuneSnap,
                        onValueChange = { autoTuneSnap = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = MagentaVocal, activeTrackColor = MagentaVocal),
                        modifier = Modifier.testTag("autotune_slider")
                    )
                }
            }
        }

        // Render Action Button
        item {
            Button(
                onClick = {
                    isConverting = true
                    conversionProgress = 0.1f
                    conversionStage = "Demucs: Isolating vocal and accompaniment stems..."

                    coroutineScope.launch {
                        val outputDir = repository.getAudioStorageDir()
                        val coverSongId = "cover_${UUID.randomUUID().toString().take(8)}"
                        val audioFile = File(outputDir, "$coverSongId.wav")

                        val title = customSongTitle?.let { "Cover - $it" } ?: "${selectedCover.title} (Cover)"
                        var coverProject = SongProject(
                            id = coverSongId,
                            title = title,
                            genre = selectedCover.genre,
                            tempoBpm = selectedCover.bpm,
                            musicalKey = selectedCover.key,
                            valence = 0.5f,
                            arousal = 0.6f,
                            emotionTag = "Studio Cover",
                            lyrics = selectedCover.lyrics,
                            stems = AudioStems(
                                vocalVolume = 0.95f,
                                harmonyVolume = 0.70f,
                                instrumentalVolume = 0.85f,
                                autoTuneStrength = autoTuneSnap,
                                reverbDecay = 0.40f,
                                stereoWidth = 0.80f,
                                vocalWarmth = activeProfile.timbreWarmth
                            ),
                            masterWavPath = audioFile.absolutePath,
                            durationSeconds = 30,
                            createdAt = System.currentTimeMillis(),
                            isCover = true,
                            coverOriginalArtist = selectedCover.originalArtist,
                            voiceProfileUsed = activeProfile.name
                        )

                        withContext(Dispatchers.IO) {
                            val (_, qc) = AudioSynthesizer.renderMasterTrack(
                                project = coverProject,
                                voiceProfile = activeProfile,
                                outputWavFile = audioFile
                            ) { progress, stage ->
                                conversionProgress = progress
                                conversionStage = stage
                            }
                            coverProject = coverProject.copy(qcReport = qc)
                        }

                        repository.saveProject(coverProject)
                        isConverting = false
                        onCoverGenerated(coverProject)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("render_cover_button"),
                colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                shape = RoundedCornerShape(28.dp),
                enabled = !isConverting
            ) {
                if (isConverting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Converting Stem Timbre...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Render Cover in My Voice", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // Conversion Progress Dialog
    if (isConverting) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = StudioSurfaceVariant,
            title = {
                Text("Vocal Conversion Pipeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = conversionStage,
                        fontSize = 13.sp,
                        color = VioletAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LinearProgressIndicator(
                        progress = { conversionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = VioletAccent,
                        trackColor = StudioSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "100% on-device zero-cost singing voice conversion...",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {}
        )
    }
}
