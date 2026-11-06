package com.example.model

data class VoiceProfile(
    val id: String = "default_user_voice",
    val name: String = "My Calibrated Voice",
    val isCalibrated: Boolean = false,
    val calibratedDate: Long = System.currentTimeMillis(),
    val pitchRangeMinHz: Float = 110f, // A2
    val pitchRangeMaxHz: Float = 392f, // G4
    val medianPitchHz: Float = 196f, // G3
    val vocalRangeNote: String = "A2 - G4",
    val timbreWarmth: Float = 0.72f,
    val formantBrightness: Float = 0.65f,
    val vibratoRateHz: Float = 5.2f,
    val vibratoDepth: Float = 0.45f,
    val noiseFloorDb: Float = -48.5f,
    val embeddingSignature: List<Float> = listOf(
        0.12f, 0.45f, 0.88f, 0.32f, 0.67f, 0.91f, 0.43f, 0.28f,
        0.75f, 0.62f, 0.39f, 0.84f, 0.55f, 0.41f, 0.69f, 0.93f
    )
) {
    companion object {
        fun defaultSample(): VoiceProfile = VoiceProfile(
            id = "studio_calibrated_preset",
            name = "Studio Calibrated Timbre",
            isCalibrated = true,
            pitchRangeMinHz = 110f,
            pitchRangeMaxHz = 392f,
            medianPitchHz = 196f,
            vocalRangeNote = "A2 - G4 (Mezzo / Baritone)",
            timbreWarmth = 0.78f,
            formantBrightness = 0.68f,
            vibratoRateHz = 5.4f,
            vibratoDepth = 0.5f,
            noiseFloorDb = -52.0f
        )
    }
}
