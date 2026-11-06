package com.example.data

import com.example.model.AudioStems
import com.example.model.SongLyrics
import com.example.model.SongProject

object PresetSongs {

    const val CALIBRATION_SCRIPT_TITLE = "Standard Acoustic & Phonetic Script"
    
    val CALIBRATION_SCRIPT_TEXT = """
        When the sunlight strikes raindrops in the air, they act as a prism and form a rainbow. 
        The rainbow is a division of white light into many beautiful colors: red, orange, yellow, green, blue, and violet. 
        Throughout the centuries, poets and singers have searched for the hidden frequency of the human voice. 
        Now I speak with clarity, resonance, and warmth, letting the tone rise and fall with natural expression. 
        From quiet whispers to soaring melodies, every harmonic captures the unique timbre of my vocal print.
    """.trimIndent()

    val CALIBRATION_PHONEME_CHECKPOINTS = listOf(
        "Vowel Formants (A, E, I, O, U)",
        "Sibilant Frequencies (S, Z, SH)",
        "Plosive Transients (P, B, T, D)",
        "Resonance & Vocal Warmth",
        "Pitch Inflection & Dynamic Range"
    )

    data class CoverTrackPreset(
        val id: String,
        val title: String,
        val originalArtist: String,
        val genre: String,
        val bpm: Int,
        val key: String,
        val duration: String,
        val description: String,
        val lyrics: SongLyrics
    )

    val COVER_PRESETS = listOf(
        CoverTrackPreset(
            id = "cover_midnight_blvd",
            title = "Midnight Boulevard",
            originalArtist = "The Neon Syndicate (Royalty-Free)",
            genre = "Synthwave 80s",
            bpm = 120,
            key = "A Minor",
            duration = "0:32",
            description = "Pulsing retro synths, 808 snare, driving bassline with soaring vocal hooks.",
            lyrics = SongLyrics(
                intro = "(Analog synth arpeggio fades in)",
                verse1 = "Cruising through the city under purple neon lights\nLost in reflections of a thousand sleepless nights\nElectric wires humming in the cool October breeze\nEvery red light waiting as we chase the memories",
                preChorus = "Feel the engine rumble as the skyline starts to glow\nNowhere left to hide and nowhere left to go",
                chorus = "We are running down the midnight boulevard tonight\nShadows dancing in the strobe of headlights\nSing it louder till the whole world turns to chrome\nTake me all the way home",
                verse2 = "Glass and chrome towers rising up into the stars\nCounting down the miles between where we are",
                bridge = "And if the headlights fade before the dawn\nThe song remains when everything is gone",
                outro = "Midnight boulevard... oh yeah\nFading into the neon dawn."
            )
        ),
        CoverTrackPreset(
            id = "cover_golden_pines",
            title = "Golden Sunlit Pines",
            originalArtist = "Cedar & Pine (Royalty-Free)",
            genre = "Indie Acoustic Folk",
            bpm = 84,
            key = "D Major",
            duration = "0:30",
            description = "Warm acoustic fingerpicking, ambient cello warmth, and intimate vocal delivery.",
            lyrics = SongLyrics(
                intro = "(Acoustic guitar fingerpicking softly)",
                verse1 = "Pack your weathered suitcase leave your heavy boots behind\nFollow where the river and the mountain ridge align\nPine needles crunched beneath the afternoon sun\nKnowing that our journey has only just begun",
                preChorus = "Listen to the cedar trees sighing in the wind\nThis is where the quiet healing can begin",
                chorus = "In the golden sunlit pines we found our peace\nWhere the wild river runs and the worries cease\nHold my hand across the bridge made of stone\nWe are never walking alone",
                verse2 = "Cabin smoke rises in the quiet autumn chill\nTime stands completely still upon the hill",
                bridge = "When the winter comes to cover up the ground\nOur sweet melody will still be found",
                outro = "Never walking alone...\nSoft acoustic hum."
            )
        ),
        CoverTrackPreset(
            id = "cover_velvet_rain",
            title = "Velvet Rain",
            originalArtist = "Moonlight Collective (Royalty-Free)",
            genre = "Lo-Fi R&B",
            bpm = 76,
            key = "F Minor",
            duration = "0:30",
            description = "Warm Rhodes piano chords, vinyl crackle, lush backing vocal layers, and deep 808.",
            lyrics = SongLyrics(
                intro = "(Vinyl static, smooth Rhodes chord)",
                verse1 = "Window pane blurred by the raindrops falling down\nCoffee getting cold as the quiet wraps the town\nTurn the record player to the song you used to love\nWatching heavy clouds drift up above",
                preChorus = "I can still remember what you used to say\nBefore the distance washed the words away",
                chorus = "Velvet rain falling softly on the street\nCatching every rhythm of my heavy heartbeat\nWrap me in the echoes of your sweet embrace\nIn this velvet rainy place",
                verse2 = "Scent of lavender and old paper books\nLost inside the comfort of your tender looks",
                bridge = "Wash the tears away until the storm is clear\nI still feel you right here",
                outro = "Velvet rain... mm yeah\nVinyl fades to silence."
            )
        ),
        CoverTrackPreset(
            id = "cover_starlight_horizon",
            title = "Starlight Horizon",
            originalArtist = "Aura Soundworks (Royalty-Free)",
            genre = "Pop Anthem",
            bpm = 128,
            key = "G Major",
            duration = "0:32",
            description = "Euphoric festival drops, grand piano intro, punchy four-on-the-floor beat, and soaring chorus.",
            lyrics = SongLyrics(
                intro = "(Grand piano chords building)",
                verse1 = "Standing on the rooftop looking out across the sea\nWondering who we are supposed to be\nCity lights are blinking like a constellation sign\nWhispering that this is our design",
                preChorus = "One, two, three, let the heartbeat rise\nSee the whole horizon open in your eyes",
                chorus = "We are reaching for the starlight horizon\nBurning through the dark like a sun rising\nNo more holding back, no more looking down\nWe wear the starlight crown!",
                verse2 = "Gravity is fading as the rhythm takes control\nFire in the spirit and music in the soul",
                bridge = "Higher than we ever thought we'd go\nLet the whole world glow!",
                outro = "Starlight horizon!\nEchoes fade out."
            )
        )
    )
}
