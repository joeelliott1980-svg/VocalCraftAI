package com.example.model

data class AudioStems(
    val vocalVolume: Float = 0.95f,
    val vocalSolo: Boolean = false,
    val vocalMute: Boolean = false,
    val harmonyVolume: Float = 0.70f,
    val harmonySolo: Boolean = false,
    val harmonyMute: Boolean = false,
    val instrumentalVolume: Float = 0.85f,
    val instrumentalSolo: Boolean = false,
    val instrumentalMute: Boolean = false,
    val autoTuneStrength: Float = 0.60f,
    val reverbDecay: Float = 0.40f,
    val stereoWidth: Float = 0.75f,
    val vocalWarmth: Float = 0.65f,
    val lufsNormalized: Boolean = true,
    val advancedFx: AdvancedVocalFx = AdvancedVocalFx()
) {
    fun getEffectiveVocalGain(): Float {
        if (vocalMute) return 0f
        if (harmonySolo || instrumentalSolo) {
            return if (vocalSolo) vocalVolume else 0f
        }
        return vocalVolume
    }

    fun getEffectiveHarmonyGain(): Float {
        if (harmonyMute) return 0f
        if (vocalSolo || instrumentalSolo) {
            return if (harmonySolo) harmonyVolume else 0f
        }
        return harmonyVolume
    }

    fun getEffectiveInstrumentalGain(): Float {
        if (instrumentalMute) return 0f
        if (vocalSolo || harmonySolo) {
            return if (instrumentalSolo) instrumentalVolume else 0f
        }
        return instrumentalVolume
    }
}
