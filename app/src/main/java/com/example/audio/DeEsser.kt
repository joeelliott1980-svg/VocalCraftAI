package com.example.audio

import com.example.model.DeEsserSettings
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

/**
 * Frequency-Dependent De-esser Module.
 *
 * Targets sibilance (high frequency energy) using a bandpass detector
 * and applies gain reduction to the main signal when threshold is exceeded.
 */
class DeEsser(
    private val sampleRate: Int = 44100
) {

    // Sidechain detector state
    private var detectorState = 0.0
    private var reduction = 1.0f

    /**
     * Applies frequency-dependent de-essing.
     */
    fun process(
        left: FloatArray,
        right: FloatArray,
        settings: DeEsserSettings
    ) {
        val total = left.size
        val freq = settings.frequencyHz.toDouble()
        
        // Simple bandpass for detector (focusing on sibilance range)
        val alpha = (2.0 * PI * freq / sampleRate).coerceIn(0.1, 0.95).toFloat()
        val threshold = 10.0.pow(settings.thresholdDb / 20.0).toFloat()
        
        // Attack/Release for gain reduction
        val attack = 0.002f
        val release = 0.05f

        for (i in 0 until total) {
            val inL = left[i]
            val inR = right[i]
            
            // Sidechain signal (abs sum)
            val sidechain = (abs(inL) + abs(inR)) * 0.5f
            
            // Detect envelope
            if (sidechain > detectorState) {
                detectorState += (sidechain - detectorState) * attack
            } else {
                detectorState += (sidechain - detectorState) * release
            }
            
            // Calculate gain reduction
            val targetReduction = if (detectorState > threshold.toDouble()) {
                val ratio = (detectorState / threshold.toDouble()).pow(0.5) // Soft knee
                (1.0 / ratio).coerceIn(0.2, 1.0).toFloat()
            } else {
                1.0f
            }
            
            // Smooth gain reduction
            reduction += (targetReduction - reduction) * 0.1f
            
            // Apply intensity
            val appliedReduction = 1.0f - ((1.0f - reduction) * settings.intensity)
            
            left[i] = inL * appliedReduction
            right[i] = inR * appliedReduction
        }
    }
}
