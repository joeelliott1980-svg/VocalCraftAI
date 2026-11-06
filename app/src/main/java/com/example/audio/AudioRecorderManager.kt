package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.model.VoiceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

class AudioRecorderManager(private val context: Context) {

    data class RecordingState(
        val isRecording: Boolean = false,
        val elapsedSeconds: Int = 0,
        val currentDb: Float = -70f,
        val isClipping: Boolean = false,
        val noiseFloorDb: Float = -55f,
        val currentPitchHz: Float? = null,
        val waveformHistory: List<Float> = emptyList()
    )

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val collectedPitches = mutableListOf<Float>()
    private var noiseFloorSamples = mutableListOf<Float>()
    private var totalRmsSum = 0.0
    private var totalRmsCount = 0

    @SuppressLint("MissingPermission")
    fun startCalibrationRecording(onComplete: (VoiceProfile) -> Unit, onError: (String) -> Unit) {
        if (_recordingState.value.isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            onError("AudioRecord initialization failed: Unsupported hardware buffer")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                max(minBufferSize, 4096 * 2)
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError("Microphone is unavailable or busy.")
                return
            }

            audioRecord?.startRecording()
            collectedPitches.clear()
            noiseFloorSamples.clear()
            totalRmsSum = 0.0
            totalRmsCount = 0

            _recordingState.value = RecordingState(isRecording = true)

            recordingJob = scope.launch {
                val buffer = ShortArray(2048)
                var elapsedMillis = 0L
                val startTime = System.currentTimeMillis()
                val waveList = ArrayDeque<Float>(40)

                while (isActive && _recordingState.value.isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0) {
                        val analysis = VoiceprintAnalyzer.analyzeFrame(buffer, read, sampleRate)
                        if (analysis.pitchHz != null) {
                            collectedPitches.add(analysis.pitchHz)
                        }

                        // Track noise floor when quieter
                        if (analysis.rmsDb < -40f) {
                            noiseFloorSamples.add(analysis.rmsDb)
                        }

                        totalRmsSum += analysis.rmsDb
                        totalRmsCount++

                        // Normalize visual amplitude 0f..1f
                        val normAmp = ((analysis.rmsDb + 60f) / 60f).coerceIn(0.05f, 1f)
                        if (waveList.size >= 40) waveList.removeFirst()
                        waveList.addLast(normAmp)

                        elapsedMillis = System.currentTimeMillis() - startTime
                        val seconds = (elapsedMillis / 1000).toInt()

                        _recordingState.value = _recordingState.value.copy(
                            elapsedSeconds = seconds,
                            currentDb = analysis.rmsDb,
                            isClipping = analysis.isClipping,
                            noiseFloorDb = if (noiseFloorSamples.isNotEmpty()) noiseFloorSamples.takeLast(10).average().toFloat() else -50f,
                            currentPitchHz = analysis.pitchHz,
                            waveformHistory = waveList.toList()
                        )

                        // If 30 seconds reached, complete automatically
                        if (seconds >= 30) {
                            stopAndExtractProfile(onComplete)
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            onError("Recording error: ${e.localizedMessage}")
        }
    }

    fun stopCalibrationRecording(onComplete: (VoiceProfile) -> Unit) {
        stopAndExtractProfile(onComplete)
    }

    private fun stopAndExtractProfile(onComplete: (VoiceProfile) -> Unit) {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (ignored: Exception) {}
        audioRecord = null
        recordingJob?.cancel()
        recordingJob = null

        val avgNoise = if (noiseFloorSamples.isNotEmpty()) noiseFloorSamples.average().toFloat() else -52f
        val avgRms = if (totalRmsCount > 0) (totalRmsSum / totalRmsCount).toFloat() else -28f

        val profile = VoiceprintAnalyzer.buildProfileFromRecordings(
            collectedPitches = collectedPitches,
            averageNoiseFloor = avgNoise,
            averageRms = avgRms
        )

        _recordingState.value = RecordingState(isRecording = false, elapsedSeconds = 30)
        onComplete(profile)
    }

    fun cancelRecording() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (ignored: Exception) {}
        audioRecord = null
        recordingJob?.cancel()
        recordingJob = null
        _recordingState.value = RecordingState(isRecording = false)
    }
}
