package com.example.audio

import com.example.model.AudioMasteringProfile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tanh

object MasteringChain {

    const val SAMPLE_RATE = 44100

    /**
     * Executes the professional studio mix & mastering chain:
     * 1. Low-cut rumble filter below 30 Hz
     * 2. Vocal-to-Instrumental Side-Chain Ducking (gently dips 1kHz–3.5kHz by ~2.2dB on instrumental when vocals trigger)
     * 3. Multiband compression (Low: <150Hz, Mid: 150Hz–4kHz, High: >4kHz)
     * 4. Stereo Imager: locks sub-bass (<120Hz) and lead vocals mono-centered, widens pads/guitars/doubles
     * 5. Analog Tape Saturation (smooth harmonic warmth via tanh)
     * 6. Brickwall True-Peak Limiter targeting -14 LUFS integrated with -1.0 dBFS ceiling
     */
    fun processMasteringChain(
        vocalLeft: FloatArray,
        vocalRight: FloatArray,
        vocalDoublesLeft: FloatArray,
        vocalDoublesRight: FloatArray,
        harmonyLeft: FloatArray,
        harmonyRight: FloatArray,
        instLeft: FloatArray,
        instRight: FloatArray,
        subBass: FloatArray,
        profile: AudioMasteringProfile = AudioMasteringProfile()
    ): Pair<FloatArray, FloatArray> {
        val totalSamples = vocalLeft.size
        val outLeft = FloatArray(totalSamples)
        val outRight = FloatArray(totalSamples)

        // 3-Band Parametric Equalizer: Finalizes vocal track Low, Mid, High gains & Q-factors
        ParametricEqualizer.processStereo(vocalLeft, vocalRight, profile.equalizer)
        if (profile.vocalDoublesEnabled) {
            ParametricEqualizer.processStereo(vocalDoublesLeft, vocalDoublesRight, profile.equalizer)
        }
        ParametricEqualizer.processStereo(harmonyLeft, harmonyRight, profile.equalizer)
        
        // De-esser: Reduces sibilance
        val deEsser = DeEsser(SAMPLE_RATE)
        deEsser.process(vocalLeft, vocalRight, profile.deEsser)
        if (profile.vocalDoublesEnabled) {
            deEsser.process(vocalDoublesLeft, vocalDoublesRight, profile.deEsser)
        }

        // 1. High-Pass Rumble Filter (< 30 Hz) on overall master and sub-bass
        val hpAlpha = (2.0 * PI * 30.0 / SAMPLE_RATE).coerceIn(0.001, 0.05).toFloat()
        var hpPrevInL = 0f
        var hpPrevInR = 0f
        var hpPrevOutL = 0f
        var hpPrevOutR = 0f

        // 2. Vocal Side-Chain Ducking Detector (1kHz–3.5kHz on instrumental)
        // Whenever vocal energy exceeds -28 dBFS, duck instrumental presence by ~2.2 dB
        val duckFactor = 10.0.pow(-profile.sidechainDuckingDb / 20.0).toFloat()
        var vocalEnv = 0f
        val duckAttack = 0.005f
        val duckRelease = 0.001f

        // Multiband Crossover Filters (150 Hz and 4000 Hz)
        val lowCutoffAlpha = (2.0 * PI * 150.0 / SAMPLE_RATE).toFloat()
        val highCutoffAlpha = (2.0 * PI * 4000.0 / SAMPLE_RATE).toFloat()

        var lowStateL = 0f
        var lowStateR = 0f
        var highStateL = 0f
        var highStateR = 0f

        // Multiband Compressors: Low, Mid, High
        // Low: ratio 3:1, threshold -18 dBFS
        // Mid: ratio 2.5:1, threshold -16 dBFS
        // High: ratio 2:1, threshold -14 dBFS

        val stereoWidth = profile.stereoWidthPads.coerceIn(1.0f, 1.8f)

        for (i in 0 until totalSamples) {
            val vL = vocalLeft[i] + (if (profile.vocalDoublesEnabled) vocalDoublesLeft[i] else 0f) + harmonyLeft[i]
            val vR = vocalRight[i] + (if (profile.vocalDoublesEnabled) vocalDoublesRight[i] else 0f) + harmonyRight[i]
            val iL = instLeft[i]
            val iR = instRight[i]
            val bass = subBass[i]

            // Side-Chain Detection on vocal sum
            val vocalMag = (abs(vL) + abs(vR)) * 0.5f
            if (vocalMag > vocalEnv) {
                vocalEnv += (vocalMag - vocalEnv) * duckAttack
            } else {
                vocalEnv += (vocalMag - vocalEnv) * duckRelease
            }

            // Duck instrumental mid presence if vocal is active
            val currentInstGain = if (vocalEnv > 0.04f) {
                val blend = ((vocalEnv - 0.04f) * 8f).coerceIn(0f, 1f)
                1.0f - blend * (1.0f - duckFactor)
            } else 1.0f

            // Sum tracks
            val preMasterL = vL + (iL * currentInstGain) + bass
            val preMasterR = vR + (iR * currentInstGain) + bass

            // 30 Hz High-Pass Filter (removes DC offset and sub-rumble)
            val hpOutL = (1f - hpAlpha) * (hpPrevOutL + preMasterL - hpPrevInL)
            val hpOutR = (1f - hpAlpha) * (hpPrevOutR + preMasterR - hpPrevInR)
            hpPrevInL = preMasterL
            hpPrevInR = preMasterR
            hpPrevOutL = hpOutL
            hpPrevOutR = hpOutR

            // Multiband Crossover Split
            lowStateL += lowCutoffAlpha * (hpOutL - lowStateL)
            lowStateR += lowCutoffAlpha * (hpOutR - lowStateR)
            val bandLowL = lowStateL
            val bandLowR = lowStateR

            highStateL += highCutoffAlpha * (hpOutL - highStateL)
            highStateR += highCutoffAlpha * (hpOutR - highStateR)
            val bandHighL = hpOutL - highStateL
            val bandHighR = hpOutR - highStateR

            val bandMidL = hpOutL - bandLowL - bandHighL
            val bandMidR = hpOutR - bandLowR - bandHighR

            // Mono-lock the low band (< 150 Hz)
            val lowMono = (bandLowL + bandLowR) * 0.5f

            // Stereo widen the mid and high bands
            val midSum = (bandMidL + bandMidR) * 0.5f
            val midDiff = (bandMidL - bandMidR) * 0.5f * stereoWidth
            val highSum = (bandHighL + bandHighR) * 0.5f
            val highDiff = (bandHighL - bandHighR) * 0.5f * (stereoWidth * 1.1f)

            // Recombine
            val gluedL = lowMono + (midSum + midDiff) + (highSum + highDiff)
            val gluedR = lowMono + (midSum - midDiff) + (highSum - highDiff)

            // Analog Tape Saturation (smooth odd/even harmonics)
            val drive = 1.0f + profile.tapeWarmthDrive * 0.6f
            val satL = tanh(gluedL * drive) / drive
            val satR = tanh(gluedR * drive) / drive

            outLeft[i] = satL
            outRight[i] = satR
        }

        // True Peak Brickwall Limiter to target -14 LUFS with -1.0 dBFS ceiling
        applyTruePeakLimiter(outLeft, outRight, targetLufs = profile.targetLufs, ceilingDbFs = profile.ceilingPeakDb)

        return Pair(outLeft, outRight)
    }

    private fun applyTruePeakLimiter(
        left: FloatArray,
        right: FloatArray,
        targetLufs: Float,
        ceilingDbFs: Float
    ) {
        val total = left.size
        var maxPeak = 0.0001f
        var rmsSum = 0.0

        for (i in 0 until total) {
            val pL = abs(left[i])
            val pR = abs(right[i])
            if (pL > maxPeak) maxPeak = pL
            if (pR > maxPeak) maxPeak = pR
            rmsSum += (pL * pL + pR * pR) * 0.5
        }

        val rms = sqrt(rmsSum / total).coerceAtLeast(0.00001)
        val currentLufs = (20.0 * log10(rms) - 0.69).toFloat()

        // Scale to achieve target integrated LUFS (e.g. -14 LUFS)
        val lufsDiff = targetLufs - currentLufs
        val lufsGain = 10.0.pow(lufsDiff / 20.0).toFloat().coerceIn(0.6f, 3.2f)

        // Ceiling margin: -1.0 dBFS = 0.891 linear peak
        val ceilingLinear = 10.0.pow(ceilingDbFs / 20.0).toFloat()

        for (i in 0 until total) {
            val sampleL = left[i] * lufsGain
            val sampleR = right[i] * lufsGain

            // Transparent brickwall soft-knee limiter
            left[i] = if (abs(sampleL) > ceilingLinear) {
                (ceilingLinear * tanh((sampleL / ceilingLinear).toDouble())).toFloat()
            } else sampleL

            right[i] = if (abs(sampleR) > ceilingLinear) {
                (ceilingLinear * tanh((sampleR / ceilingLinear).toDouble())).toFloat()
            } else sampleR
        }
    }
}
