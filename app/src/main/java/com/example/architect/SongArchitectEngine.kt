package com.example.architect

import com.example.model.AudioStems
import com.example.model.SongLyrics
import com.example.model.SongProject
import java.util.UUID

object SongArchitectEngine {

    data class QuestionnaireState(
        val storyAnswer: String = "Driving through the rain past midnight, searching for clarity",
        val atmosphereAnswer: String = "Neon reflections on wet pavement under purple skies",
        val selectedGenre: String = "Synthwave 80s",
        val selectedVocalStyle: String = "Melodic Auto-Tuned",
        val customKeywords: String = "neon, highway, pulse, freedom, memories"
    )

    data class ArchitectResult(
        val title: String,
        val genre: String,
        val bpm: Int,
        val musicalKey: String,
        val valence: Float,
        val arousal: Float,
        val emotionTag: String,
        val lyrics: SongLyrics
    )

    val GENRE_OPTIONS = listOf(
        "Synthwave 80s",
        "Indie Folk",
        "Pop Anthem",
        "Lo-Fi R&B",
        "Alt-Rock",
        "Cinematic Ballad"
    )

    val VOCAL_STYLE_OPTIONS = listOf(
        "Warm & Acoustic",
        "Melodic Auto-Tuned",
        "Intimate & Breathy",
        "Soaring & Anthemic"
    )

    val STORY_PROMPT_PRESETS = listOf(
        "Driving through the rain past midnight, searching for clarity",
        "A summer afternoon on the coast with someone I won't forget",
        "Overcoming the storm and finally finding peace within myself",
        "Standing at the edge of the city watching the lights wake up"
    )

    val ATMOSPHERE_PRESETS = listOf(
        "Neon reflections on wet pavement under purple skies",
        "Warm golden hour sunlight through pine branches",
        "Quiet rainfall against a bedroom window with vinyl playing",
        "Electric stadium lasers and pulse-pounding bass"
    )

    fun composeSong(state: QuestionnaireState): ArchitectResult {
        val combinedText = "${state.storyAnswer} ${state.atmosphereAnswer} ${state.customKeywords}".lowercase()

        // 1. Calculate Valence (-1.0 to 1.0) & Arousal (0.0 to 1.0)
        var valence = 0.5f
        var arousal = 0.5f

        if (combinedText.contains("rain") || combinedText.contains("lost") || combinedText.contains("sad") || combinedText.contains("lonely") || combinedText.contains("storm")) {
            valence -= 0.35f
        }
        if (combinedText.contains("peace") || combinedText.contains("warm") || combinedText.contains("sun") || combinedText.contains("summer") || combinedText.contains("clarity")) {
            valence += 0.40f
        }
        if (combinedText.contains("electric") || combinedText.contains("laser") || combinedText.contains("pulse") || combinedText.contains("run") || combinedText.contains("stadium") || combinedText.contains("fly")) {
            arousal += 0.35f
        }
        if (combinedText.contains("quiet") || combinedText.contains("soft") || combinedText.contains("whisper") || combinedText.contains("vinyl") || combinedText.contains("pine")) {
            arousal -= 0.30f
        }

        valence = valence.coerceIn(-1.0f, 1.0f)
        arousal = arousal.coerceIn(0.1f, 1.0f)

        // 2. Derive Musical Key & Tempo BPM based on Genre & Valence
        val (bpm, key) = when (state.selectedGenre) {
            "Synthwave 80s" -> 122 to (if (valence >= 0) "D Major" else "A Minor")
            "Indie Folk" -> 86 to (if (valence >= 0) "D Major" else "B Minor")
            "Pop Anthem" -> 128 to (if (valence >= 0) "G Major" else "F# Minor")
            "Lo-Fi R&B" -> 76 to (if (valence >= 0) "Eb Major" else "F Minor")
            "Alt-Rock" -> 134 to (if (valence >= 0) "E Major" else "E Minor")
            "Cinematic Ballad" -> 70 to (if (valence >= 0) "C Major" else "A Minor")
            else -> 96 to "D Major"
        }

        val emotionTag = when {
            valence > 0.4f && arousal > 0.6f -> "Euphoric & Triumphant"
            valence > 0.2f && arousal <= 0.6f -> "Serene & Heartfelt"
            valence <= 0.2f && arousal > 0.6f -> "Defiant & Restless"
            else -> "Melancholic & Reflective"
        }

        val title = generateTitle(state.selectedGenre, state.storyAnswer, state.atmosphereAnswer)
        val lyrics = generateStructuredLyrics(state, title, key)

        return ArchitectResult(
            title = title,
            genre = state.selectedGenre,
            bpm = bpm,
            musicalKey = key,
            valence = valence,
            arousal = arousal,
            emotionTag = emotionTag,
            lyrics = lyrics
        )
    }

    private fun generateTitle(genre: String, story: String, atmosphere: String): String {
        val sLower = story.lowercase()
        return when {
            sLower.contains("drive") || sLower.contains("midnight") -> "Midnight Trajectory"
            sLower.contains("summer") || sLower.contains("coast") -> "Golden Shoreline"
            sLower.contains("storm") || sLower.contains("peace") -> "After the Tempest"
            sLower.contains("city") || sLower.contains("lights") -> "Signals in the Skyline"
            genre == "Synthwave 80s" -> "Neon Horizons"
            genre == "Indie Folk" -> "Wildflower Ridge"
            genre == "Lo-Fi R&B" -> "Coffee on the Window"
            else -> "Echoes of Tomorrow"
        }
    }

    private fun generateStructuredLyrics(state: QuestionnaireState, title: String, key: String): SongLyrics {
        val storyFirst = state.storyAnswer.take(45)
        val atmoFirst = state.atmosphereAnswer.take(45)

        return SongLyrics(
            intro = "($atmoFirst - subtle key chords fade in)",
            verse1 = "Left the headlights on while the city fell asleep\nCounting all the quiet promises we couldn\'t keep\n$storyFirst\nListening to echoes in the gentle night wind",
            preChorus = "And the rhythm in my chest begins to align\nEvery faded shadow turns into a sign",
            chorus = "We are chasing $title into the light\nSinging every harmony right through the night\nFrom the highest mountain to the ocean floor\nI don\'t have to run in circles anymore!",
            verse2 = "Step by step across the bridges made of glass\nKnowing that the darkest hours will always pass\n$atmoFirst\nWe found a melody that never fades away",
            bridge = "Turn the volume up and let the sound expand\nNothing left to lose when we take a stand",
            outro = "$title... yeah\nSinging in my own voice to the end\n(Fade out on $key resolution)"
        )
    }
}
