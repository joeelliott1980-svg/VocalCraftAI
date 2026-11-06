package com.example.model

data class AudioMasteringProfile(
    val targetLufs: Float = -14.0f,         // Streaming standard -14 LUFS
    val ceilingPeakDb: Float = -1.0f,       // True peak ceiling -1.0 dBFS
    val tapeWarmthDrive: Float = 0.45f,     // Analog harmonic saturation (0..1)
    val stereoWidthPads: Float = 1.35f,     // Widening for pads, guitars, doubles (1.0..1.8)
    val monoSubBassFreq: Float = 120.0f,    // Mono cutoff frequency for bass
    val sidechainDuckingDb: Float = 2.2f,   // Instrumental mid-cut when vocals are present
    val vocalDoublesEnabled: Boolean = true,// Modern wide pop stereo doubles
    val humanizedSwing: Float = 0.54f,      // Micro-timing swing (0.50 = straight, 0.54 = groove)
    val equalizer: EqualizerSettings = EqualizerSettings(), // 3-band parametric EQ before limiter stage
    val deEsser: DeEsserSettings = DeEsserSettings()        // Frequency-dependent de-esser
)

data class QcReport(
    val isCertified: Boolean = true,
    val measuredLufs: Float = -14.1f,
    val peakDbFs: Float = -1.05f,
    val dynamicRangeDb: Float = 10.4f,
    val clippingOccurrences: Int = 0,
    val keyAlignment: String = "100% in-tune",
    val noiseFloorDb: Float = -58.2f,
    val details: String = "Radio-Ready Master Certified"
)
