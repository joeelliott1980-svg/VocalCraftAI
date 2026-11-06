package com.example.model

data class ReverbSettings(
    val decayTimeSec: Float = 1.8f, // 0.2s to 6.0s
    val wetDryMix: Float = 0.35f,   // 0% to 100%
    val preDelayMs: Float = 20f     // 0ms to 80ms
)

data class DelaySettings(
    val timeMs: Int = 240,       // 20ms to 1000ms
    val feedback: Float = 0.40f, // 0% to 90%
    val wetDryMix: Float = 0.25f // 0% to 100%
)

data class DeEsserSettings(
    val thresholdDb: Float = -20.0f, // -40dB to 0dB
    val frequencyHz: Float = 6000f,  // 4kHz to 10kHz
    val intensity: Float = 0.5f      // 0.0 to 1.0
)

data class EqBand(
    val frequencyHz: Float, // Frequency in Hz
    val gainDb: Float,      // -12dB to +12dB
    val qFactor: Float      // 0.4 to 3.0
)

data class EqualizerSettings(
    val lowBand: EqBand = EqBand(frequencyHz = 100f, gainDb = 1.5f, qFactor = 0.7f),
    val midBand: EqBand = EqBand(frequencyHz = 1800f, gainDb = 0.5f, qFactor = 1.2f),
    val highBand: EqBand = EqBand(frequencyHz = 8500f, gainDb = 2.0f, qFactor = 0.7f)
)

data class AdvancedVocalFx(
    val reverb: ReverbSettings = ReverbSettings(),
    val delay: DelaySettings = DelaySettings(),
    val deEsser: DeEsserSettings = DeEsserSettings(),
    val equalizer: EqualizerSettings = EqualizerSettings(),
    val isEnabled: Boolean = true
) {
    companion object {
        fun defaultSettings(): AdvancedVocalFx = AdvancedVocalFx()

        fun radioPopAir(): AdvancedVocalFx = AdvancedVocalFx(
            reverb = ReverbSettings(decayTimeSec = 1.4f, wetDryMix = 0.28f, preDelayMs = 15f),
            delay = DelaySettings(timeMs = 180, feedback = 0.30f, wetDryMix = 0.20f),
            equalizer = EqualizerSettings(
                lowBand = EqBand(frequencyHz = 90f, gainDb = -1.0f, qFactor = 0.8f),
                midBand = EqBand(frequencyHz = 2400f, gainDb = 2.0f, qFactor = 1.4f),
                highBand = EqBand(frequencyHz = 10500f, gainDb = 4.0f, qFactor = 0.7f)
            )
        )

        fun etherealDreamSpace(): AdvancedVocalFx = AdvancedVocalFx(
            reverb = ReverbSettings(decayTimeSec = 3.8f, wetDryMix = 0.65f, preDelayMs = 45f),
            delay = DelaySettings(timeMs = 420, feedback = 0.55f, wetDryMix = 0.45f),
            equalizer = EqualizerSettings(
                lowBand = EqBand(frequencyHz = 120f, gainDb = 0.0f, qFactor = 0.7f),
                midBand = EqBand(frequencyHz = 1500f, gainDb = -1.5f, qFactor = 1.0f),
                highBand = EqBand(frequencyHz = 9000f, gainDb = 2.5f, qFactor = 0.6f)
            )
        )

        fun warmVintageTube(): AdvancedVocalFx = AdvancedVocalFx(
            reverb = ReverbSettings(decayTimeSec = 1.2f, wetDryMix = 0.22f, preDelayMs = 10f),
            delay = DelaySettings(timeMs = 90, feedback = 0.20f, wetDryMix = 0.15f),
            equalizer = EqualizerSettings(
                lowBand = EqBand(frequencyHz = 150f, gainDb = 3.0f, qFactor = 0.9f),
                midBand = EqBand(frequencyHz = 1200f, gainDb = 1.5f, qFactor = 1.1f),
                highBand = EqBand(frequencyHz = 6500f, gainDb = -1.0f, qFactor = 0.8f)
            )
        )

        fun intimateDryLead(): AdvancedVocalFx = AdvancedVocalFx(
            reverb = ReverbSettings(decayTimeSec = 0.6f, wetDryMix = 0.12f, preDelayMs = 5f),
            delay = DelaySettings(timeMs = 40, feedback = 0.05f, wetDryMix = 0.05f),
            equalizer = EqualizerSettings(
                lowBand = EqBand(frequencyHz = 110f, gainDb = 0.5f, qFactor = 0.7f),
                midBand = EqBand(frequencyHz = 2800f, gainDb = 2.5f, qFactor = 1.5f),
                highBand = EqBand(frequencyHz = 12000f, gainDb = 1.5f, qFactor = 0.7f)
            )
        )
    }
}
