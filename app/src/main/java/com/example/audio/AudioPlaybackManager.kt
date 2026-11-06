package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlaybackManager(private val context: Context) {

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val currentPositionMs: Int = 0,
        val durationMs: Int = 0,
        val progress: Float = 0f,
        val activeTrackPath: String? = null
    )

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playFile(file: File, isLooping: Boolean = false) {
        if (!file.exists()) return

        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                this.isLooping = isLooping
                prepare()
                start()
                setOnCompletionListener {
                    if (!isLooping) {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            currentPositionMs = 0,
                            progress = 0f
                        )
                    }
                }
            }

            val duration = mediaPlayer?.duration ?: 0
            _playbackState.value = PlaybackState(
                isPlaying = true,
                currentPositionMs = 0,
                durationMs = duration,
                progress = 0f,
                activeTrackPath = file.absolutePath
            )

            startProgressPolling()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
        } else {
            player.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressPolling()
        }
    }

    fun seekToFraction(fraction: Float) {
        val player = mediaPlayer ?: return
        val duration = player.duration
        if (duration > 0) {
            val targetMs = (duration * fraction.coerceIn(0f, 1f)).toInt()
            player.seekTo(targetMs)
            _playbackState.value = _playbackState.value.copy(
                currentPositionMs = targetMs,
                progress = fraction
            )
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition ?: 0
                val duration = mediaPlayer?.duration ?: 1
                val prog = if (duration > 0) (current.toFloat() / duration).coerceIn(0f, 1f) else 0f
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    currentPositionMs = current,
                    durationMs = duration,
                    progress = prog
                )
                delay(60) // Smooth ~16fps visualizer sync
            }
        }
    }
}
