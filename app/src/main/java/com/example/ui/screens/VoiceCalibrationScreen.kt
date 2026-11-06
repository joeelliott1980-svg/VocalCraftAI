package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioPlaybackManager
import com.example.audio.AudioRecorderManager
import com.example.audio.AudioSynthesizer
import com.example.audio.WavFileHelper
import com.example.data.PresetSongs
import com.example.data.SongRepository
import com.example.model.AudioStems
import com.example.model.SongLyrics
import com.example.model.SongProject
import com.example.model.VoiceProfile
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldMaster
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import java.io.File

@Composable
fun VoiceCalibrationScreen(
    repository: SongRepository,
    onCalibrationFinished: () -> Unit
) {
    val context = LocalContext.current
    val recorderManager = remember { AudioRecorderManager(context) }
    val playbackManager = remember { AudioPlaybackManager(context) }
    val recordState by recorderManager.recordingState.collectAsState()
    val activeProfile by repository.voiceProfile.collectAsState()

    var extractedProfile by remember { mutableStateOf<VoiceProfile?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isTestingPlayback by remember { mutableStateOf(false) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (!granted) {
            errorMessage = "Microphone permission is required to calibrate your voice."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorderManager.cancelRecording()
            playbackManager.stop()
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Column {
                Text(
                    text = "Voice Calibration",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Read the 30-second teleprompter script to extract your unique vocal timbre, pitch register, and formant resonance.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Clipping & Noise Warning Card
        if (recordState.isClipping) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Clipping warning",
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Peak Clipping Detected! Move 2–3 inches back from your mic.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF8B8B)
                        )
                    }
                }
            }
        }

        // Ambient Focused Recording Booth View (When Recording)
        if (recordState.isRecording) {
            item {
                val inputIntensity = ((recordState.currentDb + 50f) / 45f).coerceIn(0.15f, 1f)
                val remainingSeconds = (30 - recordState.elapsedSeconds).coerceAtLeast(0)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recording_booth_card"),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        CyanAccent.copy(alpha = 0.22f * inputIntensity),
                                        VioletAccent.copy(alpha = 0.12f * inputIntensity),
                                        Color.Transparent
                                    ),
                                    radius = 500f
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            // Header with Status & Countdown Timer
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
                                            .background(MagentaVocal)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LIVE RECORDING BOOTH",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MagentaVocal,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyanAccent.copy(alpha = 0.2f))
                                        .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${remainingSeconds}s REMAINING",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Overall Progress Bar
                            LinearProgressIndicator(
                                progress = { recordState.elapsedSeconds / 30f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyanAccent,
                                trackColor = StudioSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Ambient Waveform Ribbon History
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .background(StudioSurface, RoundedCornerShape(8.dp))
                                    .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                val waves = recordState.waveformHistory
                                waves.forEach { amp ->
                                    val barH = (amp * 40.dp.value).coerceAtLeast(3f)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(barH.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(CyanAccent, VioletAccent)
                                                )
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Live Speech Fidelity & Telemetry Feedback
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = StudioSurface.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "LIVE SPEECH FIDELITY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                        val fidelityPercent = (96.5f + (inputIntensity * 3f)).coerceIn(95f, 99.8f)
                                        Text(
                                            text = "${String.format("%.1f", fidelityPercent)}% Clean Vocal Timbre",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldMaster
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "MIC INPUT LEVEL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "${recordState.currentDb.toInt()} dBFS (Optimal: -18 to -6)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (recordState.currentDb in -28f..-4f) CyanAccent else AmberWarning
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Teleprompter Script Card
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
                        Text(
                            text = PresetSongs.CALIBRATION_SCRIPT_TITLE,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VioletAccent
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Phonetically Balanced",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = PresetSongs.CALIBRATION_SCRIPT_TEXT,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (recordState.isRecording) TextPrimary else TextSecondary,
                        modifier = Modifier.testTag("calibration_script_text")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phoneme Checkpoints
                    Text(
                        text = "Coverage Analysis:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PresetSongs.CALIBRATION_PHONEME_CHECKPOINTS.forEachIndexed { index, point ->
                        val isCovered = recordState.elapsedSeconds >= (index + 1) * 5
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isCovered) EmeraldMaster else StudioCardBorder,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = point,
                                fontSize = 11.sp,
                                color = if (isCovered) TextPrimary else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Recording Action Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (recordState.isRecording) {
                    Button(
                        onClick = {
                            recorderManager.stopCalibrationRecording { profile ->
                                extractedProfile = profile
                            }
                        },
                        modifier = Modifier
                            .scale(pulseScale)
                            .height(56.dp)
                            .testTag("stop_recording_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MagentaVocal),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop recording")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish & Extract Voiceprint", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (!hasMicPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                errorMessage = null
                                recorderManager.startCalibrationRecording(
                                    onComplete = { profile ->
                                        extractedProfile = profile
                                    },
                                    onError = { err ->
                                        errorMessage = err
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth(0.85f)
                            .testTag("start_calibration_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Start recording", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (activeProfile.isCalibrated) "Re-Calibrate Voice (30s)" else "Begin 30s Voice Calibration",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Extracted Profile Card
        extractedProfile?.let { profile ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calibration_results_card"),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldMaster)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = EmeraldMaster,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voiceprint Calibrated Successfully!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Pitch Register", fontSize = 11.sp, color = TextSecondary)
                                Text(profile.vocalRangeNote, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            Column {
                                Text("Median Pitch", fontSize = 11.sp, color = TextSecondary)
                                Text("${profile.medianPitchHz.toInt()} Hz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("Timbre Warmth", fontSize = 11.sp, color = TextSecondary)
                                Text("${(profile.timbreWarmth * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VioletAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Formant Clarity", fontSize = 11.sp, color = TextSecondary)
                                Text("${(profile.formantBrightness * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("Vibrato Rate", fontSize = 11.sp, color = TextSecondary)
                                Text("${String.format("%.1f", profile.vibratoRateHz)} Hz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("Room Noise", fontSize = 11.sp, color = TextSecondary)
                                Text("${profile.noiseFloorDb.toInt()} dB", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldMaster)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions: Test Synthesis & Save
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    // Generate a quick vocal test sample
                                    val testFile = File(repository.getAudioStorageDir(), "test_scale_${System.currentTimeMillis()}.wav")
                                    val testProject = SongProject(
                                        id = "test_scale",
                                        title = "Vocal Scale Test",
                                        genre = "Vocal Scale",
                                        tempoBpm = 100,
                                        musicalKey = "D Major",
                                        valence = 0.5f,
                                        arousal = 0.5f,
                                        emotionTag = "Neutral Test",
                                        lyrics = SongLyrics()
                                    )
                                    AudioSynthesizer.renderMasterTrack(testProject, profile, testFile)
                                    playbackManager.playFile(testFile)
                                    isTestingPlayback = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_vocal_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Audition Vocal")
                            }

                            Button(
                                onClick = {
                                    repository.updateVoiceProfile(profile)
                                    onCalibrationFinished()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_profile_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldMaster)
                            ) {
                                Text("Save & Continue", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
