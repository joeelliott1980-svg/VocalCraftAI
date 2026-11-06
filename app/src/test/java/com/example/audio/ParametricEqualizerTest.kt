package com.example.audio

import com.example.model.EqBand
import com.example.model.EqualizerSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class ParametricEqualizerTest {

    @Test
    fun testBiquadFilterProcessingMaintainsNumericalStability() {
        val sampleRate = 44100
        val numSamples = 4410 // 100ms
        val left = FloatArray(numSamples)
        val right = FloatArray(numSamples)

        // Generate 1 kHz sine test signal
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * 1000.0 * t).toFloat() * 0.5f
            left[i] = sample
            right[i] = sample
        }

        val eqSettings = EqualizerSettings(
            lowBand = EqBand(frequencyHz = 100f, gainDb = 3.0f, qFactor = 0.7f),
            midBand = EqBand(frequencyHz = 1000f, gainDb = 6.0f, qFactor = 1.4f),
            highBand = EqBand(frequencyHz = 10000f, gainDb = -3.0f, qFactor = 0.8f)
        )

        ParametricEqualizer.processStereo(left, right, eqSettings, sampleRate)

        // Verify output is finite, has no NaNs, and was boosted around 1kHz
        var maxPeak = 0f
        for (i in 1000 until numSamples) {
            assertFalse("Sample should not be NaN", left[i].isNaN())
            assertFalse("Sample should not be Infinite", left[i].isInfinite())
            val absVal = kotlin.math.abs(left[i])
            if (absVal > maxPeak) maxPeak = absVal
        }

        // With +6dB gain at 1kHz, maxPeak should exceed input 0.5f
        assertTrue("1kHz peak should be boosted by mid band gain, measured $maxPeak", maxPeak > 0.6f)
    }

    @Test
    fun testFlatEqPreservesSignalGain() {
        val sampleRate = 44100
        val numSamples = 2205 // 50ms
        val left = FloatArray(numSamples) { 0.4f }
        val right = FloatArray(numSamples) { 0.4f }

        val flatSettings = EqualizerSettings(
            lowBand = EqBand(frequencyHz = 100f, gainDb = 0f, qFactor = 1.0f),
            midBand = EqBand(frequencyHz = 1000f, gainDb = 0f, qFactor = 1.0f),
            highBand = EqBand(frequencyHz = 8000f, gainDb = 0f, qFactor = 1.0f)
        )

        ParametricEqualizer.processStereo(left, right, flatSettings, sampleRate)

        // At 0dB across all bands, output settles back to unity gain
        assertEquals(0.4f, left.last(), 0.05f)
        assertEquals(0.4f, right.last(), 0.05f)
    }
}
