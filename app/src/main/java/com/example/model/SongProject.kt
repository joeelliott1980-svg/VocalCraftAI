package com.example.model

data class SongProject(
    val id: String,
    val title: String,
    val genre: String,
    val tempoBpm: Int,
    val musicalKey: String,
    val valence: Float,
    val arousal: Float,
    val emotionTag: String,
    val lyrics: SongLyrics,
    val stems: AudioStems = AudioStems(),
    val masterWavPath: String = "",
    val durationSeconds: Int = 32,
    val createdAt: Long = System.currentTimeMillis(),
    val isCover: Boolean = false,
    val coverOriginalArtist: String? = null,
    val voiceProfileUsed: String = "My Calibrated Voice",
    val masteringProfile: AudioMasteringProfile = AudioMasteringProfile(),
    val qcReport: QcReport? = null
)
