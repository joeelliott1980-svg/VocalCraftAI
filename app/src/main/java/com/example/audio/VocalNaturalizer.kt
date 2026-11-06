package com.example.audio

import com.example.model.VoiceProfile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tanh

/**
 * High-Fidelity Vocal Synthesis & Naturalizer Engine.
 * 
 * Features:
 * - 3-Formant Vocal Tract Filter (F1 Chest Body, F2 Vowel Articulation, F3 Singer's Formant Resonance)
 * - Liljencrants-Fant style smooth glottal pulse excitation (natural vocal cord behavior)
 * - S-Curve portamento pitch gliding with adaptive retune speed
 * - Delayed expressive vibrato with coupled amplitude tremolo
 * - Aspiration turbulence / breathy vocal air
 * - Realistic acoustic breath intake synthesis
 * - Modern Haas stereo doubling with micro-pitch detuning
 */
object VocalNaturalizer {

    const val SAMPLE_RATE = 44100

    enum class Vowel(
        val f1: Double, val f1Q: Double,
        val f2: Double, val f2Q: Double,
        val f3: Double, val f3Q: Double
    ) {
        AA(750.0, 3.5, 1250.0, 4.0, 2750.0, 5.0),   // "Ah" open, soaring chorus
        OH(500.0, 3.0, 950.0, 3.5, 2600.0, 4.5),    // "Oh" warm, emotional verse
        EE(320.0, 4.0, 2200.0, 4.5, 2900.0, 5.5),   // "Ee" bright, intimate
        OO(360.0, 3.0, 750.0, 3.0, 2400.0, 4.0)     // "Ooh" smooth intro/outro hum
    }

    /**
     * Resonant 2nd-order bandpass filter for vocal tract formants
     */
    class FormantFilter(freq: Double, q: Double, sampleRate: Int = SAMPLE_RATE) {
        private var w0 = 2.0 * PI * freq / sampleRate
        private var alpha = sin(w0) / (2.0 * q)
        private var b0 = alpha
        private var b1 = 0.0
        private var b2 = -alpha
        private var a0 = 1.0 + alpha
        private var a1 = -2.0 * kotlin.math.cos(w0)
        private var a2 = 1.0 - alpha

        private var x1 = 0.0
        private var x2 = 0.0
        private var y1 = 0.0
        private var y2 = 0.0

        fun process(input: Double): Double {
            val y = (b0 / a0) * input + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2
            x2 = x1
            x1 = input
            y2 = y1
            y1 = y
            return y
        }
    }

    /**
     * Renders a natural, emotive vocal note with formant vocal tract modeling.
     */
    fun renderNaturalVocalNote(
        left: FloatArray,
        right: FloatArray,
        startSample: Int,
        numSamples: Int,
        targetPitchHz: Double,
        previousPitchHz: Double,
        warmth: Float,
        brightness: Float,
        vibratoRate: Float,
        vibratoDepth: Float,
        retuneSpeedMs: Float = 25f,
        pan: Float = 0.5f,
        gainScale: Float = 1.0f,
        vowel: Vowel = Vowel.AA
    ) {
        val end = (startSample + numSamples).coerceAtMost(left.size)
        if (startSample >= end) return

        val glideSamples = ((SAMPLE_RATE * (retuneSpeedMs / 1000f))).toInt().coerceIn(150, numSamples / 2)
        val attackSamples = (SAMPLE_RATE * 0.025).toInt() // fast natural vocal attack
        val releaseSamples = (SAMPLE_RATE * 0.045).toInt()
        val vibratoOnsetSamples = (SAMPLE_RATE * 0.140).toInt() // Vibrato blossoms after 140ms

        val f1Filter = FormantFilter(vowel.f1 * (0.9 + warmth * 0.2), vowel.f1Q)
        val f2Filter = FormantFilter(vowel.f2 * (0.9 + brightness * 0.2), vowel.f2Q)
        val f3Filter = FormantFilter(vowel.f3 * (0.95 + brightness * 0.1), vowel.f3Q) // Singer's Formant

        var phase = 0.0
        val effectivePan = pan.coerceIn(0f, 1f)
        val airIntensity = (0.04f + (1.0f - warmth) * 0.03f) * gainScale

        for (i in startSample until end) {
            val localIdx = i - startSample
            val timeSec = localIdx.toDouble() / SAMPLE_RATE

            // 1. Portamento Pitch Glide (S-Curve smoothstep interpolation)
            val currentBaseHz = if (localIdx < glideSamples) {
                val progress = (localIdx.toDouble() / glideSamples)
                val sCurve = progress * progress * (3.0 - 2.0 * progress)
                previousPitchHz + (targetPitchHz - previousPitchHz) * sCurve
            } else {
                targetPitchHz
            }

            // 2. Expressive Delayed Vibrato & Amplitude Tremolo
            val vibratoEnvelope = if (localIdx < vibratoOnsetSamples) {
                0.0
            } else {
                ((localIdx - vibratoOnsetSamples).toDouble() / (SAMPLE_RATE * 0.18)).coerceIn(0.0, 1.0)
            }

            // Coupled human pitch modulation (vibrato) + natural micro-drift
            val humanDrift = sin(2.0 * PI * 0.28 * timeSec) * 0.0025
            val vibratoOsc = sin(2.0 * PI * vibratoRate * timeSec) * (0.015 * vibratoDepth * vibratoEnvelope)
            val modulatedHz = currentBaseHz * (1.0 + vibratoOsc + humanDrift)

            // Amplitude tremolo: vocal tension increases volume slightly (+1.2 dB) at vibrato peaks
            val tremolo = 1.0 + (vibratoOsc * 4.5 * vibratoEnvelope)

            phase += 2.0 * PI * modulatedHz / SAMPLE_RATE
            if (phase > 2.0 * PI) phase -= 2.0 * PI

            // 3. Glottal Pulse Generator (Liljencrants-Fant model approximation)
            // Asymmetric opening and closing creates organic harmonic roll-off
            val normPhase = phase / (2.0 * PI)
            val glottalSource = if (normPhase < 0.65) {
                0.5 * (1.0 - kotlin.math.cos(PI * normPhase / 0.65))
            } else {
                kotlin.math.cos(0.5 * PI * (normPhase - 0.65) / 0.35)
            }

            // 4. Vocal Tract Formant Filtering
            // Pass glottal source through the resonant formants
            val outF1 = f1Filter.process(glottalSource) * (0.85 + warmth * 0.4)
            val outF2 = f2Filter.process(glottalSource) * (0.65 + brightness * 0.3)
            val outF3 = f3Filter.process(glottalSource) * (0.45 + brightness * 0.4) // singer's formant brilliance

            // 5. Breathy Vocal Air Sheen (subtle aspiration noise shaped by formants)
            val aspiration = (Math.random() * 2.0 - 1.0) * airIntensity * (sin(phase).coerceAtLeast(0.0))

            // Composite vocal wave with analog warmth saturation
            val composite = (outF1 + outF2 + outF3 + aspiration) * tremolo * 0.58
            val warmVocal = tanh(composite * (1.0 + warmth * 0.4))

            // 6. Dynamic Note Envelope (Attack, Sustain, Decay)
            val env = when {
                localIdx < attackSamples -> (localIdx.toFloat() / attackSamples)
                localIdx > numSamples - releaseSamples -> ((numSamples - localIdx).toFloat() / releaseSamples)
                else -> 1.0f
            }.coerceIn(0f, 1f)

            val sample = (warmVocal * env * gainScale).toFloat()

            // Stereo placement (Lead centered with natural width)
            left[i] += sample * (1.0f - effectivePan * 0.38f)
            right[i] += sample * (0.62f + effectivePan * 0.38f)
        }
    }

    /**
     * Synthesizes natural human breath intake sounds before lyrical phrases.
     */
    fun insertBreathIntake(
        left: FloatArray,
        right: FloatArray,
        startSample: Int,
        durationSamples: Int = (SAMPLE_RATE * 0.16).toInt(),
        intensity: Float = 0.06f
    ) {
        val actualStart = startSample.coerceAtLeast(0)
        val end = (actualStart + durationSamples).coerceAtMost(left.size)
        var pinkState = 0.0

        for (i in actualStart until end) {
            val localIdx = i - actualStart
            val progress = localIdx.toFloat() / durationSamples

            // Natural inhale envelope: smooth rise, quick intake peak, gentle drop
            val env = (sin(progress * PI) * exp(-progress * 1.5)).toFloat()

            // Filtered pink-noise character (softer and warmer than harsh white noise)
            val white = (Math.random() * 2.0 - 1.0)
            pinkState = 0.92 * pinkState + 0.08 * white
            val breath = (pinkState.toFloat() * intensity * env * 0.85f)

            left[i] += breath * 0.8f
            right[i] += breath * 0.8f
        }
    }

    /**
     * Renders modern commercial stereo vocal doubles (wide wall of sound).
     */
    fun renderStereoVocalDoubles(
        vocalLeft: FloatArray,
        vocalRight: FloatArray,
        doublesLeft: FloatArray,
        doublesRight: FloatArray,
        volume: Float = 0.63f // -4 dB
    ) {
        val total = vocalLeft.size
        // 12ms delay left, 18ms delay right (Haas stereo spread)
        val delaySamplesL = (SAMPLE_RATE * 0.012).toInt()
        val delaySamplesR = (SAMPLE_RATE * 0.018).toInt()

        for (i in 0 until total) {
            // Left Double: panned 35% L
            if (i >= delaySamplesL) {
                val sampleL = vocalLeft[i - delaySamplesL] * volume
                doublesLeft[i] += sampleL * 0.85f
                doublesRight[i] += sampleL * 0.15f
            }

            // Right Double: panned 35% R
            if (i >= delaySamplesR) {
                val sampleR = vocalRight[i - delaySamplesR] * volume
                doublesLeft[i] += sampleR * 0.15f
                doublesRight[i] += sampleR * 0.85f
            }
        }
    }
}
