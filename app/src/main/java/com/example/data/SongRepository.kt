package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.audio.AudioSynthesizer
import com.example.model.AdvancedVocalFx
import com.example.model.AudioStems
import com.example.model.DelaySettings
import com.example.model.EqBand
import com.example.model.EqualizerSettings
import com.example.model.ReverbSettings
import com.example.model.SongLyrics
import com.example.model.SongProject
import com.example.model.VoiceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SongRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vocalcraft_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _voiceProfile = MutableStateFlow(VoiceProfile.defaultSample())
    val voiceProfile: StateFlow<VoiceProfile> = _voiceProfile.asStateFlow()

    private val _savedProjects = MutableStateFlow<List<SongProject>>(emptyList())
    val savedProjects: StateFlow<List<SongProject>> = _savedProjects.asStateFlow()

    private val _activeProject = MutableStateFlow<SongProject?>(null)
    val activeProject: StateFlow<SongProject?> = _activeProject.asStateFlow()

    init {
        loadVoiceProfile()
        loadSavedProjects()
    }

    fun getAudioStorageDir(): File {
        val dir = File(context.filesDir, "audio")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun updateVoiceProfile(profile: VoiceProfile) {
        _voiceProfile.value = profile
        prefs.edit().apply {
            putString("vp_id", profile.id)
            putString("vp_name", profile.name)
            putBoolean("vp_calibrated", profile.isCalibrated)
            putLong("vp_date", profile.calibratedDate)
            putFloat("vp_f0_min", profile.pitchRangeMinHz)
            putFloat("vp_f0_max", profile.pitchRangeMaxHz)
            putFloat("vp_f0_med", profile.medianPitchHz)
            putString("vp_range_note", profile.vocalRangeNote)
            putFloat("vp_warmth", profile.timbreWarmth)
            putFloat("vp_brightness", profile.formantBrightness)
            putFloat("vp_vibrato_rate", profile.vibratoRateHz)
            putFloat("vp_vibrato_depth", profile.vibratoDepth)
            putFloat("vp_noise", profile.noiseFloorDb)
            apply()
        }
    }

    private fun loadVoiceProfile() {
        val isCalibrated = prefs.getBoolean("vp_calibrated", false)
        if (!isCalibrated) {
            _voiceProfile.value = VoiceProfile.defaultSample()
            return
        }

        val profile = VoiceProfile(
            id = prefs.getString("vp_id", "user_voice_default") ?: "user_voice_default",
            name = prefs.getString("vp_name", "My Calibrated Voice") ?: "My Calibrated Voice",
            isCalibrated = true,
            calibratedDate = prefs.getLong("vp_date", System.currentTimeMillis()),
            pitchRangeMinHz = prefs.getFloat("vp_f0_min", 110f),
            pitchRangeMaxHz = prefs.getFloat("vp_f0_max", 392f),
            medianPitchHz = prefs.getFloat("vp_f0_med", 196f),
            vocalRangeNote = prefs.getString("vp_range_note", "A2 - G4") ?: "A2 - G4",
            timbreWarmth = prefs.getFloat("vp_warmth", 0.75f),
            formantBrightness = prefs.getFloat("vp_brightness", 0.65f),
            vibratoRateHz = prefs.getFloat("vp_vibrato_rate", 5.2f),
            vibratoDepth = prefs.getFloat("vp_vibrato_depth", 0.48f),
            noiseFloorDb = prefs.getFloat("vp_noise", -48f)
        )
        _voiceProfile.value = profile
    }

    fun saveProject(project: SongProject) {
        val list = _savedProjects.value.toMutableList()
        val existingIndex = list.indexOfFirst { it.id == project.id }
        if (existingIndex >= 0) {
            list[existingIndex] = project
        } else {
            list.add(0, project)
        }
        _savedProjects.value = list
        _activeProject.value = project
        persistProjects(list)
    }

    fun deleteProject(projectId: String) {
        val list = _savedProjects.value.filterNot { it.id == projectId }
        _savedProjects.value = list
        if (_activeProject.value?.id == projectId) {
            _activeProject.value = list.firstOrNull()
        }
        persistProjects(list)
    }

    fun setActiveProject(project: SongProject) {
        _activeProject.value = project
    }

    private fun persistProjects(projects: List<SongProject>) {
        scope.launch {
            try {
                val array = JSONArray()
                for (p in projects) {
                    val obj = JSONObject().apply {
                        put("id", p.id)
                        put("title", p.title)
                        put("genre", p.genre)
                        put("tempoBpm", p.tempoBpm)
                        put("musicalKey", p.musicalKey)
                        put("valence", p.valence)
                        put("arousal", p.arousal)
                        put("emotionTag", p.emotionTag)
                        put("masterWavPath", p.masterWavPath)
                        put("durationSeconds", p.durationSeconds)
                        put("createdAt", p.createdAt)
                        put("isCover", p.isCover)
                        put("coverOriginalArtist", p.coverOriginalArtist ?: "")
                        put("voiceProfileUsed", p.voiceProfileUsed)

                        val lyricsObj = JSONObject().apply {
                            put("intro", p.lyrics.intro)
                            put("verse1", p.lyrics.verse1)
                            put("preChorus", p.lyrics.preChorus)
                            put("chorus", p.lyrics.chorus)
                            put("verse2", p.lyrics.verse2)
                            put("bridge", p.lyrics.bridge)
                            put("outro", p.lyrics.outro)
                        }
                        put("lyrics", lyricsObj)

                        val stemsObj = JSONObject().apply {
                            put("vocalVolume", p.stems.vocalVolume.toDouble())
                            put("vocalSolo", p.stems.vocalSolo)
                            put("vocalMute", p.stems.vocalMute)
                            put("harmonyVolume", p.stems.harmonyVolume.toDouble())
                            put("harmonySolo", p.stems.harmonySolo)
                            put("harmonyMute", p.stems.harmonyMute)
                            put("instrumentalVolume", p.stems.instrumentalVolume.toDouble())
                            put("instrumentalSolo", p.stems.instrumentalSolo)
                            put("instrumentalMute", p.stems.instrumentalMute)
                            put("autoTuneStrength", p.stems.autoTuneStrength.toDouble())
                            put("reverbDecay", p.stems.reverbDecay.toDouble())
                            put("stereoWidth", p.stems.stereoWidth.toDouble())
                            put("vocalWarmth", p.stems.vocalWarmth.toDouble())

                            val fxObj = JSONObject().apply {
                                val fx = p.stems.advancedFx
                                put("rev_decay", fx.reverb.decayTimeSec.toDouble())
                                put("rev_wet", fx.reverb.wetDryMix.toDouble())
                                put("rev_predelay", fx.reverb.preDelayMs.toDouble())
                                put("del_time", fx.delay.timeMs)
                                put("del_feedback", fx.delay.feedback.toDouble())
                                put("del_wet", fx.delay.wetDryMix.toDouble())
                                put("eq_low_gain", fx.equalizer.lowBand.gainDb.toDouble())
                                put("eq_low_freq", fx.equalizer.lowBand.frequencyHz.toDouble())
                                put("eq_low_q", fx.equalizer.lowBand.qFactor.toDouble())
                                put("eq_mid_gain", fx.equalizer.midBand.gainDb.toDouble())
                                put("eq_mid_freq", fx.equalizer.midBand.frequencyHz.toDouble())
                                put("eq_mid_q", fx.equalizer.midBand.qFactor.toDouble())
                                put("eq_high_gain", fx.equalizer.highBand.gainDb.toDouble())
                                put("eq_high_freq", fx.equalizer.highBand.frequencyHz.toDouble())
                                put("eq_high_q", fx.equalizer.highBand.qFactor.toDouble())
                                put("enabled", fx.isEnabled)
                            }
                            put("advancedFx", fxObj)
                        }
                        put("stems", stemsObj)

                        p.qcReport?.let { qc ->
                            val qcObj = JSONObject().apply {
                                put("isCertified", qc.isCertified)
                                put("measuredLufs", qc.measuredLufs.toDouble())
                                put("peakDbFs", qc.peakDbFs.toDouble())
                                put("dynamicRangeDb", qc.dynamicRangeDb.toDouble())
                                put("clippingOccurrences", qc.clippingOccurrences)
                                put("keyAlignment", qc.keyAlignment)
                                put("noiseFloorDb", qc.noiseFloorDb.toDouble())
                                put("details", qc.details)
                            }
                            put("qcReport", qcObj)
                        }
                    }
                    array.put(obj)
                }
                prefs.edit().putString("saved_projects_json", array.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSavedProjects() {
        val json = prefs.getString("saved_projects_json", null)
        if (json.isNullOrEmpty()) {
            seedInitialDemoProject()
            return
        }

        try {
            val array = JSONArray(json)
            val list = mutableListOf<SongProject>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val lyrObj = obj.optJSONObject("lyrics")
                val lyrics = if (lyrObj != null) {
                    SongLyrics(
                        intro = lyrObj.optString("intro", ""),
                        verse1 = lyrObj.optString("verse1", ""),
                        preChorus = lyrObj.optString("preChorus", ""),
                        chorus = lyrObj.optString("chorus", ""),
                        verse2 = lyrObj.optString("verse2", ""),
                        bridge = lyrObj.optString("bridge", ""),
                        outro = lyrObj.optString("outro", "")
                    )
                } else SongLyrics()

                val stemObj = obj.optJSONObject("stems")
                val stems = if (stemObj != null) {
                    AudioStems(
                        vocalVolume = stemObj.optDouble("vocalVolume", 0.95).toFloat(),
                        vocalSolo = stemObj.optBoolean("vocalSolo", false),
                        vocalMute = stemObj.optBoolean("vocalMute", false),
                        harmonyVolume = stemObj.optDouble("harmonyVolume", 0.70).toFloat(),
                        harmonySolo = stemObj.optBoolean("harmonySolo", false),
                        harmonyMute = stemObj.optBoolean("harmonyMute", false),
                        instrumentalVolume = stemObj.optDouble("instrumentalVolume", 0.85).toFloat(),
                        instrumentalSolo = stemObj.optBoolean("instrumentalSolo", false),
                        instrumentalMute = stemObj.optBoolean("instrumentalMute", false),
                        autoTuneStrength = stemObj.optDouble("autoTuneStrength", 0.60).toFloat(),
                        reverbDecay = stemObj.optDouble("reverbDecay", 0.40).toFloat(),
                        stereoWidth = stemObj.optDouble("stereoWidth", 0.75).toFloat(),
                        vocalWarmth = stemObj.optDouble("vocalWarmth", 0.65).toFloat(),
                        advancedFx = stemObj.optJSONObject("advancedFx")?.let { fx ->
                            AdvancedVocalFx(
                                reverb = ReverbSettings(
                                    decayTimeSec = fx.optDouble("rev_decay", 1.8).toFloat(),
                                    wetDryMix = fx.optDouble("rev_wet", 0.35).toFloat(),
                                    preDelayMs = fx.optDouble("rev_predelay", 20.0).toFloat()
                                ),
                                delay = DelaySettings(
                                    timeMs = fx.optInt("del_time", 240),
                                    feedback = fx.optDouble("del_feedback", 0.40).toFloat(),
                                    wetDryMix = fx.optDouble("del_wet", 0.25).toFloat()
                                ),
                                equalizer = EqualizerSettings(
                                    lowBand = EqBand(
                                        frequencyHz = fx.optDouble("eq_low_freq", 100.0).toFloat(),
                                        gainDb = fx.optDouble("eq_low_gain", 1.5).toFloat(),
                                        qFactor = fx.optDouble("eq_low_q", 0.7).toFloat()
                                    ),
                                    midBand = EqBand(
                                        frequencyHz = fx.optDouble("eq_mid_freq", 1800.0).toFloat(),
                                        gainDb = fx.optDouble("eq_mid_gain", 0.5).toFloat(),
                                        qFactor = fx.optDouble("eq_mid_q", 1.2).toFloat()
                                    ),
                                    highBand = EqBand(
                                        frequencyHz = fx.optDouble("eq_high_freq", 8500.0).toFloat(),
                                        gainDb = fx.optDouble("eq_high_gain", 2.0).toFloat(),
                                        qFactor = fx.optDouble("eq_high_q", 0.7).toFloat()
                                    )
                                ),
                                isEnabled = fx.optBoolean("enabled", true)
                            )
                        } ?: AdvancedVocalFx()
                    )
                } else AudioStems()

                val qcObj = obj.optJSONObject("qcReport")
                val qcReport = if (qcObj != null) {
                    com.example.model.QcReport(
                        isCertified = qcObj.optBoolean("isCertified", true),
                        measuredLufs = qcObj.optDouble("measuredLufs", -14.1).toFloat(),
                        peakDbFs = qcObj.optDouble("peakDbFs", -1.05).toFloat(),
                        dynamicRangeDb = qcObj.optDouble("dynamicRangeDb", 10.4).toFloat(),
                        clippingOccurrences = qcObj.optInt("clippingOccurrences", 0),
                        keyAlignment = qcObj.optString("keyAlignment", "100% in-tune"),
                        noiseFloorDb = qcObj.optDouble("noiseFloorDb", -58.2).toFloat(),
                        details = qcObj.optString("details", "Radio-Ready Master Certified")
                    )
                } else null

                val proj = SongProject(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    genre = obj.optString("genre", "Synthwave 80s"),
                    tempoBpm = obj.optInt("tempoBpm", 120),
                    musicalKey = obj.optString("musicalKey", "A Minor"),
                    valence = obj.optDouble("valence", 0.35).toFloat(),
                    arousal = obj.optDouble("arousal", 0.7).toFloat(),
                    emotionTag = obj.optString("emotionTag", "Neon Nostalgia"),
                    lyrics = lyrics,
                    stems = stems,
                    masterWavPath = obj.optString("masterWavPath", ""),
                    durationSeconds = obj.optInt("durationSeconds", 32),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    isCover = obj.optBoolean("isCover", false),
                    coverOriginalArtist = if (obj.has("coverOriginalArtist")) obj.optString("coverOriginalArtist") else null,
                    voiceProfileUsed = obj.optString("voiceProfileUsed", "My Calibrated Voice"),
                    qcReport = qcReport
                )
                list.add(proj)
            }
            _savedProjects.value = list
            _activeProject.value = list.firstOrNull()
        } catch (e: Exception) {
            seedInitialDemoProject()
        }
    }

    private fun seedInitialDemoProject() {
        scope.launch {
            val audioDir = getAudioStorageDir()
            val demoFile = File(audioDir, "demo_neon_horizons.wav")
            var demoProject = SongProject(
                id = "project_demo_neon",
                title = "Neon Horizons",
                genre = "Synthwave 80s",
                tempoBpm = 122,
                musicalKey = "A Minor",
                valence = 0.42f,
                arousal = 0.78f,
                emotionTag = "Nocturnal Adrenaline",
                lyrics = SongLyrics(
                    intro = "(Vintage analog synthesizer arpeggio builds)",
                    verse1 = "Left the headlights on while the city fell asleep\nCounting every promise that was ours to keep\nSignals passing through the purple midnight air\nLooking in the rearview mirror knowing you are there",
                    preChorus = "And the rhythm in my chest begins to align\nEvery faded shadow turns into a sign",
                    chorus = "We are chasing neon horizons into the light\nSinging every harmony right through the night\nFrom the highest mountain to the ocean floor\nI don\'t have to run in circles anymore!",
                    verse2 = "Step by step across the bridges made of glass\nKnowing that the darkest hours will always pass",
                    bridge = "Turn the volume up and let the sound expand\nNothing left to lose when we take a stand",
                    outro = "Neon horizons... yeah\nSinging in my calibrated voice\n(Fade out on A Minor resolution)"
                ),
                stems = AudioStems(
                    vocalVolume = 0.95f,
                    harmonyVolume = 0.70f,
                    instrumentalVolume = 0.85f,
                    autoTuneStrength = 0.65f,
                    reverbDecay = 0.45f,
                    stereoWidth = 0.80f,
                    vocalWarmth = 0.70f
                ),
                masterWavPath = demoFile.absolutePath,
                durationSeconds = 30,
                createdAt = System.currentTimeMillis() - 3600000,
                isCover = false,
                voiceProfileUsed = "Studio Calibrated Timbre"
            )

            val engineVersion = prefs.getInt("audio_engine_version", 1)
            // Render audio file in background if not exists or if engine version upgraded
            if (!demoFile.exists() || demoFile.length() < 1000 || engineVersion < 2) {
                if (demoFile.exists()) demoFile.delete()
                val (_, qc) = AudioSynthesizer.renderMasterTrack(
                    project = demoProject,
                    voiceProfile = VoiceProfile.defaultSample(),
                    outputWavFile = demoFile
                )
                demoProject = demoProject.copy(qcReport = qc)
                prefs.edit().putInt("audio_engine_version", 2).apply()
            }

            _savedProjects.value = listOf(demoProject)
            _activeProject.value = demoProject
            persistProjects(listOf(demoProject))
        }
    }
}
