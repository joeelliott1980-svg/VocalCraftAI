package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architect.SongArchitectEngine
import com.example.audio.AudioSynthesizer
import com.example.data.SongRepository
import com.example.model.AudioStems
import com.example.model.SongLyrics
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongArchitectScreen(
    repository: SongRepository,
    onSongGenerated: (SongProject) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activeProfile by repository.voiceProfile.collectAsState()

    var state by remember { mutableStateOf(SongArchitectEngine.QuestionnaireState()) }
    var architectResult by remember { mutableStateOf(SongArchitectEngine.composeSong(state)) }

    var isGenerating by remember { mutableStateOf(false) }
    var generationProgress by remember { mutableFloatStateOf(0f) }
    var generationStageText by remember { mutableStateOf("Initializing pipeline...") }

    var showLyricEditor by remember { mutableStateOf(false) }
    var editableChorus by remember { mutableStateOf(architectResult.lyrics.chorus) }
    var editableVerse1 by remember { mutableStateOf(architectResult.lyrics.verse1) }

    fun recompute() {
        val result = SongArchitectEngine.composeSong(state)
        architectResult = result
        editableChorus = result.lyrics.chorus
        editableVerse1 = result.lyrics.verse1
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Song Architect",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Dictate emotional story, landscape, and genre to derive BPM, musical key, and lyrics sung in your voice.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Question 1: Story & Memory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Emotional Story & Core Memory",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = state.storyAnswer,
                        onValueChange = {
                            state = state.copy(storyAnswer = it)
                            recompute()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("story_input"),
                        placeholder = { Text("What memory or feeling is lingering in your mind?") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = StudioSurfaceVariant,
                            unfocusedContainerColor = StudioSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = CyanAccent,
                            unfocusedIndicatorColor = StudioCardBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Quick Inspiration Presets:", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SongArchitectEngine.STORY_PROMPT_PRESETS.forEach { preset ->
                            val isSelected = state.storyAnswer == preset
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) CyanAccent else StudioCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        state = state.copy(storyAnswer = preset)
                                        recompute()
                                    },
                                color = if (isSelected) CyanAccent.copy(alpha = 0.2f) else StudioSurfaceVariant
                            ) {
                                Text(
                                    text = preset.take(34) + "...",
                                    fontSize = 11.sp,
                                    color = if (isSelected) CyanAccent else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Question 2: Atmosphere & Weather
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. Musical Landscape & Atmosphere",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = state.atmosphereAnswer,
                        onValueChange = {
                            state = state.copy(atmosphereAnswer = it)
                            recompute()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("atmosphere_input"),
                        placeholder = { Text("Describe the weather, landscape, or sensory feeling") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = StudioSurfaceVariant,
                            unfocusedContainerColor = StudioSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = CyanAccent,
                            unfocusedIndicatorColor = StudioCardBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SongArchitectEngine.ATMOSPHERE_PRESETS.forEach { preset ->
                            val isSelected = state.atmosphereAnswer == preset
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) CyanAccent else StudioCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        state = state.copy(atmosphereAnswer = preset)
                                        recompute()
                                    },
                                color = if (isSelected) CyanAccent.copy(alpha = 0.2f) else StudioSurfaceVariant
                            ) {
                                Text(
                                    text = preset.take(32) + "...",
                                    fontSize = 11.sp,
                                    color = if (isSelected) CyanAccent else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Question 3: Genre & Vocal Style
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. Genre & Arrangement Style",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MagentaVocal
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SongArchitectEngine.GENRE_OPTIONS.forEach { genre ->
                            val isSelected = state.selectedGenre == genre
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, if (isSelected) MagentaVocal else StudioCardBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        state = state.copy(selectedGenre = genre)
                                        recompute()
                                    },
                                color = if (isSelected) MagentaVocal.copy(alpha = 0.2f) else StudioSurfaceVariant
                            ) {
                                Text(
                                    text = genre,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Vocal Delivery Style",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SongArchitectEngine.VOCAL_STYLE_OPTIONS.forEach { style ->
                            val isSelected = state.selectedVocalStyle == style
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) VioletAccent else StudioCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        state = state.copy(selectedVocalStyle = style)
                                    },
                                color = if (isSelected) VioletAccent.copy(alpha = 0.25f) else StudioSurfaceVariant
                            ) {
                                Text(
                                    text = style,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-time Sentiment & Music Theory Engine Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CALCULATED MUSIC METRICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                        Text(
                            text = architectResult.emotionTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VioletAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Derived Key", fontSize = 11.sp, color = TextMuted)
                            Text(architectResult.musicalKey, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Derived Tempo", fontSize = 11.sp, color = TextMuted)
                            Text("${architectResult.bpm} BPM", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Valence", fontSize = 11.sp, color = TextMuted)
                            Text(String.format("%.2f", architectResult.valence), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (architectResult.valence >= 0) EmeraldMaster else Color(0xFFF59E0B))
                        }
                        Column {
                            Text("Arousal", fontSize = 11.sp, color = TextMuted)
                            Text(String.format("%.2f", architectResult.arousal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                    }
                }
            }
        }

        // Lyrics Preview & Quick Editor
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = EmeraldMaster, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Structured Lyrics: \"${architectResult.title}\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioSurfaceVariant)
                                .clickable { showLyricEditor = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 11.sp, color = CyanAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "[CHORUS]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletAccent
                    )
                    Text(
                        text = editableChorus,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "[VERSE 1]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = editableVerse1,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Primary Generation Action
        item {
            Button(
                onClick = {
                    isGenerating = true
                    generationProgress = 0.05f
                    generationStageText = "Analyzing vocal timbre signature..."

                    coroutineScope.launch {
                        val outputDir = repository.getAudioStorageDir()
                        val songId = "song_${UUID.randomUUID().toString().take(8)}"
                        val audioFile = File(outputDir, "$songId.wav")

                        val finalLyrics = architectResult.lyrics.copy(
                            chorus = editableChorus,
                            verse1 = editableVerse1
                        )

                        val autoTuneStrength = if (state.selectedVocalStyle.contains("Auto-Tuned")) 0.85f else 0.50f
                        var newProject = SongProject(
                            id = songId,
                            title = architectResult.title,
                            genre = architectResult.genre,
                            tempoBpm = architectResult.bpm,
                            musicalKey = architectResult.musicalKey,
                            valence = architectResult.valence,
                            arousal = architectResult.arousal,
                            emotionTag = architectResult.emotionTag,
                            lyrics = finalLyrics,
                            stems = AudioStems(
                                vocalVolume = 0.95f,
                                harmonyVolume = 0.70f,
                                instrumentalVolume = 0.85f,
                                autoTuneStrength = autoTuneStrength,
                                reverbDecay = 0.42f,
                                stereoWidth = 0.75f,
                                vocalWarmth = activeProfile.timbreWarmth
                            ),
                            masterWavPath = audioFile.absolutePath,
                            durationSeconds = 30,
                            createdAt = System.currentTimeMillis(),
                            isCover = false,
                            voiceProfileUsed = activeProfile.name
                        )

                        withContext(Dispatchers.IO) {
                            val (_, qc) = AudioSynthesizer.renderMasterTrack(
                                project = newProject,
                                voiceProfile = activeProfile,
                                outputWavFile = audioFile
                            ) { progress, stage ->
                                generationProgress = progress
                                generationStageText = stage
                            }
                            newProject = newProject.copy(qcReport = qc)
                        }

                        repository.saveProject(newProject)
                        isGenerating = false
                        onSongGenerated(newProject)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_song_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(28.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating VocalCraft Master...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synthesize Song in My Voice", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // Generation Progress Dialog
    if (isGenerating) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = StudioSurfaceVariant,
            title = {
                Text("VocalCraft AI Synthesis Pipeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = generationStageText,
                        fontSize = 13.sp,
                        color = CyanAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LinearProgressIndicator(
                        progress = { generationProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CyanAccent,
                        trackColor = StudioSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "100% on-device zero-cost neural audio rendering in progress...",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Lyric Editor Dialog
    if (showLyricEditor) {
        AlertDialog(
            onDismissRequest = { showLyricEditor = false },
            containerColor = StudioSurfaceVariant,
            title = { Text("Customize Lyrics", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text("Chorus:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VioletAccent)
                    OutlinedTextField(
                        value = editableChorus,
                        onValueChange = { editableChorus = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("edit_chorus_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = StudioSurface,
                            unfocusedContainerColor = StudioSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = CyanAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Verse 1:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanAccent)
                    OutlinedTextField(
                        value = editableVerse1,
                        onValueChange = { editableVerse1 = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("edit_verse_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = StudioSurface,
                            unfocusedContainerColor = StudioSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = CyanAccent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLyricEditor = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Save Lyrics", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLyricEditor = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
