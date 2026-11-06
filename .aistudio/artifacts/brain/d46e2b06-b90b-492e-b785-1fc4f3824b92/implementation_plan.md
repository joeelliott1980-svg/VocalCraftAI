# VocalCraft AI: Studio-Quality Multitrack Audio & Vocal Production Pipeline

Overhaul the audio synthesis, vocal alignment, and mastering engine from basic procedural playback into a commercially competitive, radio-ready multitrack workstation. The upgraded pipeline introduces structural dynamic arrangement, authentic voice naturalization with breathing intake and stereo vocal doubles, an engineer-grade mixing chain with ducking side-chain compression, a multi-band tape-mastering limiter, and an automated Quality Control (QC) validator.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The clarifying questions were skipped, so the recommended studio-grade defaults have been adopted:
> - **Musical Arrangement**: Versatile multitrack arrangement supporting Pop, R&B, Synthwave, Acoustic Folk, and Alt-Rock with humanized swing micro-timing, structural dynamic transitions, and kick-to-sub-bass side-chaining.
> - **Vocal Naturalizer**: Organic tuning with 25 ms retune speed, human pitch glide (portamento), expressive vibrato swells, breath cadence pauses, and automated stereo vocal doubling with chorus harmonies.
> - **Mastering Target**: Broadcast-standard -14 LUFS integrated loudness with a -1.0 dBFS true-peak brickwall ceiling, multi-band glue compression, and analog tape saturation.

---

## 1. Overview & Core Concept

### What It Delivers
A complete overhaul of the generation, vocal synthesis, and mastering stages in **VocalCraft AI**:
1. **Dynamic Multitrack Arrangement Engine**: Replaces static loops with structured song transitions (intro, verse, pre-chorus energy build with 16th-note high-hats/strings, explosive chorus drop, stripped-down bridge, and outro fade). Implements humanized micro-timing swing (52–58% shuffle) and sub-bass kick ducking.
2. **Realistic Voice Naturalization & Alignment (SVC Layer)**: Introduces F0 pitch contour smoothing with authentic human portamento glides, expressive vibrato swells on sustained notes, breath intake audio spaces, and automated multi-voice vocal tracking (centered lead + ±25% panned detuned doubles + 3rd/5th chorus harmonies).
3. **Pro-Audio Mix & Mastering Chain**: An automated studio insert chain featuring optical vocal leveling, dynamic de-essing (clearance between 4 kHz–7 kHz), vocal presence air boost, vocal-to-instrumental side-chain ducking (gently dipping 1 kHz–3 kHz on backing instruments when the voice sings), multi-band glue compression, and soft-clip tape saturation targeting -14 LUFS.
4. **Automated Calibration & Musicality QC**: An automated post-render validator verifying scale/key alignment, harmonic dissonance metrics, and clipping prevention with automatic fallback re-tuning.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Creation Flow (`Studio`)**:
   - User completes conversational prompts (story, atmosphere, genre, vocal style).
   - The multi-stage generator displays real-time QC telemetry:
     `Arranging Stems -> Naturalizing Vocal Timbre & Doubles -> Side-Chaining Mix -> Multiband Mastering (-14 LUFS) -> QC Validation`.
2. **Interactive Multitrack Console (`Mixer`)**:
   - Expanded channel strips: **Lead Vocals (Center)**, **Stereo Vocal Doubles (L/R)**, **Vocal Harmonies**, and **Instrumental Accompaniment**.
   - Solo, Mute, and Pan sliders with live visual peak/RMS level meters.
3. **Master Chain & QC Dashboard**:
   - Visual Mastering Monitor: Displays real-time integrated LUFS (-14 target), true-peak margin (-1.0 dBFS), dynamic range (DR), and stereo correlation meter.
   - QC status badge: `Studio Master Certified: 0 Clip Warnings • Key Mapped: D Major • Spectral Balance: Optimum`.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Kotlin On-Device Multitrack DSP vs External Cloud APIs**
  - *Chosen Approach*: Implement a custom high-performance 44.1 kHz 16-bit DSP pipeline directly in Kotlin and Android audio framework.
  - *Why*: Adheres strictly to the user constraint: *"don't enlist things that will cost me money"*. Delivers zero-latency, private, offline-capable playback with zero subscription or cloud inference fees.
  - *Alternatives Considered*: Cloud-based Suno or ElevenLabs APIs were rejected due to ongoing usage costs and API key requirements.

- **Decision 2: Side-Chained Multitrack Summing with Dynamic Mid-Ducking**
  - *Chosen Approach*: When combining vocals with instrumental stems, dynamically duck the instrumental 1.5 kHz–3.5 kHz presence band by 1.8 dB whenever vocal amplitude exceeds -28 dBFS.
  - *Why*: Eliminates spectral congestion and prevents the human voice from being drowned out by synth pads and guitars, achieving commercial clarity.

- **Decision 3: Triple-Layer Vocal Arranger (Lead + Doubles + Harmonies)**
  - *Chosen Approach*: Synthesize three separate vocal layers from the user's voiceprint: a focused center lead vocal, dual stereo-widened side doubles with micro-detuning (±6 cents) and subtle 12 ms time offset, and harmonic intervals (3rd/5th) triggered during the chorus.
  - *Why*: Creates the modern "wide pop vocal" wall of sound heard on contemporary radio records without requiring extra recordings from the user.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       VOCALCRAFT PRO-AUDIO PIPELINE                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  [1. ARRANGEMENT ENGINE]                                                    │
│  ┌───────────────────────┐   ┌──────────────────────┐   ┌─────────────────┐ │
│  │ Humanized Micro-Swing │──►│ Harmonic Progression │──►│ Dynamic Sections│ │
│  │ (Drums / 808 / Bass)  │   │ (Key / Scale Mapped) │   │ (Intro->Chorus) │ │
│  └───────────────────────┘   └──────────────────────┘   └─────────────────┘ │
│                                                                             │
│  [2. VOCAL NATURALIZATION & ALIGNMENT]                                      │
│  ┌───────────────────────┐   ┌──────────────────────┐   ┌─────────────────┐ │
│  │ Naturalizer & Vibrato │──►│ Portamento & Breaths │──►│ Stereo Doubles  │ │
│  │ (25ms Retune Speed)   │   │ (Cadence Gaps)       │   │ & 3rd/5th Choir │ │
│  └───────────────────────┘   └──────────────────────┘   └─────────────────┘ │
│                                                                             │
│  [3. PRO-AUDIO MIXING CHAIN]                                                │
│  ┌───────────────────────┐   ┌──────────────────────┐   ┌─────────────────┐ │
│  │ 3-Band Parametric EQ  │──►│ Optical Compressor & │──►│ Tempo Delay &   │ │
│  │ (Presence Air Boost)  │   │ Side-Chain Ducking   │   │ High-Pass Reverb│ │
│  └───────────────────────┘   └──────────────────────┘   └─────────────────┘ │
│                                                                             │
│  [4. MULTIBAND MASTERING & QC]                                              │
│  ┌───────────────────────┐   ┌──────────────────────┐   ┌─────────────────┐ │
│  │ Multiband Compression │──►│ Analog Tape Warmer   │──►│ Brickwall Peak  │ │
│  │ & Stereo Imager       │   │ (Harmonic Saturation)│   │ Limiter (-14LUFS│ │
│  └───────────────────────┘   └──────────────────────┘   └─────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Key Modules to Create / Upgrade:
1. `com.example.audio.ProAudioEngine`:
   - High-fidelity multitrack synthesizer implementing humanized swing micro-timing (54% shuffle), kick-sidechained sub-bass envelope, structural arrangement transitions, and breath intake generation.
2. `com.example.audio.VocalNaturalizer`:
   - Computes continuous F0 pitch trajectory mapped to musical scale, applies portamento pitch glides (35 ms ramp), vibrato onset curves (delayed 180 ms into sustained notes), and renders stereo vocal doubles (left -15%, right +15%, 8 ms delay).
3. `com.example.audio.MasteringChain`:
   - 3-band multiband compressor, stereo widening matrix (keeping sub-bass below 120 Hz mono-locked), analog tape saturation curve, and true-peak brickwall limiter normalized to -14 LUFS.
4. `com.example.audio.AudioQcValidator`:
   - Analyzes rendered stems for true-peak clipping, spectral balance, and harmonic pitch accuracy, returning a certification report with automatic remedial gain scaling.
5. `com.example.ui.components.MasteringConsole`:
   - Live mastering telemetry HUD displaying LUFS meter, dynamic range score, sidechain ducking activity, and vocal doubling controls.

---

## 5. Verification & Testing Plan

1. **Compilation & Build**: Verify clean compilation via `compile_applet`.
2. **Audio File Integrity**: Verify generated WAV masters have valid 44.1 kHz 16-bit headers, 0 digital clipping occurrences, and true peak < -1.0 dBFS.
3. **Unit Tests (`gradle :app:testDebugUnitTest`)**:
   - Verify vocal naturalizer F0 pitch curves snap to valid key intervals.
   - Verify kick-sidechain attenuation reduces sub-bass by 3 dB during kick transients.
   - Verify multi-voice doubling renders separate left/right audio channels.
   - Verify QC validator detects and corrects artificial clipping.
4. **Interactive Playback**: Validate in Streaming Emulator that playback scrub, vocal double toggles, and mastering telemetry render smoothly.
