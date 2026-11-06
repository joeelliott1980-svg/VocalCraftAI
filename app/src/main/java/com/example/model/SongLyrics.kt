package com.example.model

data class SongLyrics(
    val intro: String = "(Soft atmospheric chords swell)",
    val verse1: String = "Streetlights flicker through the amber rain\nChasing shadows that still whisper your name\nFootsteps echoing on wet concrete\nEvery heartbeat keeps the quiet beat",
    val preChorus: String = "I told myself I\'d let the night rewind\nLeave every silent promise far behind",
    val chorus: String = "Now the neon horizon is pulling me through\nSinging every lost melody back to you\nThrough the static and thunder we learn how to fly\nCaught in the golden spark of the open sky",
    val verse2: String = "Dashboard glowing in a 3 AM dream\nNothing is ever as quiet as it seems\nTurn the frequency dial past the midnight noise\nSearching the dark for an honest voice",
    val bridge: String = "And if the morning washes all of this away\nWe built a universe the world could never break",
    val outro: String = "Yeah the open sky... (fading with vocal ad-libs)\nBack to you...\nSoft acoustic chords resolve."
) {
    fun toFormattedString(): String = buildString {
        appendLine("[INTRO]")
        appendLine(intro)
        appendLine()
        appendLine("[VERSE 1]")
        appendLine(verse1)
        appendLine()
        appendLine("[PRE-CHORUS]")
        appendLine(preChorus)
        appendLine()
        appendLine("[CHORUS]")
        appendLine(chorus)
        appendLine()
        appendLine("[VERSE 2]")
        appendLine(verse2)
        appendLine()
        appendLine("[BRIDGE]")
        appendLine(bridge)
        appendLine()
        appendLine("[OUTRO]")
        appendLine(outro)
    }

    fun getLyricLines(): List<Pair<String, String>> = listOf(
        "Intro" to intro,
        "Verse 1" to verse1,
        "Pre-Chorus" to preChorus,
        "Chorus" to chorus,
        "Verse 2" to verse2,
        "Bridge" to bridge,
        "Outro" to outro
    )
}
