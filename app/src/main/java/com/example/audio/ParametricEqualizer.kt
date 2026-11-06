package com.example.audio

import com.example.model.EqBand
import com.example.model.EqualizerSettings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * High-Precision 3-Band Parametric Equalizer Module for VocalCraft AI.
 *
 * Implements Robert Bristow-Johnson (RBJ) Audio EQ Cookbook biquad filters:
 * - Low Band: Parametric Peaking / Shelf with adjustable Frequency, Gain (dB), and Q-factor
 * - Mid Band: Parametric Peaking / Bell filter with adjustable Frequency, Gain (dB), and Q-factor
 * - High Band: Parametric Peaking / Shelf with adjustable Frequency, Gain (dB), and Q-factor
 *
 * Allows users to shape vocal timbre, presence, clarity, and air sheen
 * prior to the true-peak brickwall limiter stage.
 */
class ParametricEqualizer(
    private val sampleRate: Int = 44100
) {

    /**
     * Biquad 2nd-order IIR Filter implementation
     */
    class BiquadFilter(
        var freqHz: Float,
        var gainDb: Float,
        var qFactor: Float,
        private val sampleRate: Int
    ) {
        private var b0 = 1.0
        private var b1 = 0.0
        private var b2 = 0.0
        private var a1 = 0.0
        private var a2 = 0.0

        // Filter state memory for stereo channels
        private var x1L = 0.0
        private var x2L = 0.0
        private var y1L = 0.0
        private var y2L = 0.0

        private var x1R = 0.0
        private var x2R = 0.0
        private var y1R = 0.0
        private var y2R = 0.0

        init {
            recalculateCoefficients()
        }

        fun update(newFreqHz: Float, newGainDb: Float, newQFactor: Float) {
            if (freqHz != newFreqHz || gainDb != newGainDb || qFactor != newQFactor) {
                freqHz = newFreqHz
                gainDb = newGainDb
                qFactor = newQFactor
                recalculateCoefficients()
            }
        }

        /**
         * Computes RBJ peaking/bell filter coefficients:
         * A = 10^(gainDb / 40)
         * w0 = 2 * PI * freq / sampleRate
         * alpha = sin(w0) / (2 * Q)
         */
        private fun recalculateCoefficients() {
            val clampedFreq = freqHz.toDouble().coerceIn(20.0, (sampleRate / 2.0) - 100.0)
            val clampedQ = qFactor.toDouble().coerceIn(0.1, 10.0)
            val a = 10.0.pow(gainDb.toDouble() / 40.0)

            val w0 = 2.0 * PI * clampedFreq / sampleRate
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val alpha = sinW0 / (2.0 * clampedQ)

            val rawB0 = 1.0 + alpha * a
            val rawB1 = -2.0 * cosW0
            val rawB2 = 1.0 - alpha * a
            val rawA0 = 1.0 + alpha / a
            val rawA1 = -2.0 * cosW0
            val rawA2 = 1.0 - alpha / a

            // Normalize by a0
            b0 = rawB0 / rawA0
            b1 = rawB1 / rawA0
            b2 = rawB2 / rawA0
            a1 = rawA1 / rawA0
            a2 = rawA2 / rawA0
        }

        fun processSampleLeft(x: Double): Double {
            val y = b0 * x + b1 * x1L + b2 * x2L - a1 * y1L - a2 * y2L
            x2L = x1L
            x1L = x
            y2L = y1L
            y1L = y
            return y
        }

        fun processSampleRight(x: Double): Double {
            val y = b0 * x + b1 * x1R + b2 * x2R - a1 * y1R - a2 * y2R
            x2R = x1R
            x1R = x
            y2R = y1R
            y1R = y
            return y
        }

        fun reset() {
            x1L = 0.0; x2L = 0.0; y1L = 0.0; y2L = 0.0
            x1R = 0.0; x2R = 0.0; y1R = 0.0; y2R = 0.0
        }
    }

    private val lowFilter = BiquadFilter(100f, 0f, 0.7f, sampleRate)
    private val midFilter = BiquadFilter(1800f, 0f, 1.2f, sampleRate)
    private val highFilter = BiquadFilter(8500f, 0f, 0.7f, sampleRate)

    /**
     * Applies the 3-band parametric equalizer in-place to stereo audio buffers.
     */
    fun process(
        left: FloatArray,
        right: FloatArray,
        settings: EqualizerSettings
    ) {
        lowFilter.update(settings.lowBand.frequencyHz, settings.lowBand.gainDb, settings.lowBand.qFactor)
        midFilter.update(settings.midBand.frequencyHz, settings.midBand.gainDb, settings.midBand.qFactor)
        highFilter.update(settings.highBand.frequencyHz, settings.highBand.gainDb, settings.highBand.qFactor)

        val total = left.size.coerceAtMost(right.size)
        for (i in 0 until total) {
            var sL = left[i].toDouble()
            var sR = right[i].toDouble()

            // Cascade through Low, Mid, and High parametric biquads
            sL = lowFilter.processSampleLeft(sL)
            sR = lowFilter.processSampleRight(sR)

            sL = midFilter.processSampleLeft(sL)
            sR = midFilter.processSampleRight(sR)

            sL = highFilter.processSampleLeft(sL)
            sR = highFilter.processSampleRight(sR)

            left[i] = sL.toFloat()
            right[i] = sR.toFloat()
        }
    }

    fun reset() {
        lowFilter.reset()
        midFilter.reset()
        highFilter.reset()
    }

    companion object {
        /**
         * Utility function for direct static processing without persisting filter instance
         */
        fun processStereo(
            left: FloatArray,
            right: FloatArray,
            settings: EqualizerSettings,
            sampleRate: Int = 44100
        ) {
            val eq = ParametricEqualizer(sampleRate)
            eq.process(left, right, settings)
        }
    }
}
