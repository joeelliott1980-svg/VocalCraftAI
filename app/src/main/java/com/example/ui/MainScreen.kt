package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongRepository
import com.example.model.SongProject
import com.example.ui.components.SplashView
import com.example.ui.components.VocalCraftTopBar
import com.example.ui.components.VoiceStatusBadge
import com.example.ui.screens.AboutCreditsSheet
import com.example.ui.screens.CoverStudioScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayStoreIconStudioSheet
import com.example.ui.screens.SocialShareSheet
import com.example.ui.screens.SongArchitectScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudioPlayerScreen
import com.example.ui.screens.VoiceCalibrationScreen
import com.example.ui.screens.VoiceprintProfileScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.launch

enum class StudioTab(val label: String) {
    ARCHITECT("Studio"),
    COVER("Cover Engine"),
    PLAYER("Mixer"),
    CALIBRATE("Voiceprint"),
    LIBRARY("Library")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repository: SongRepository) {
    val coroutineScope = rememberCoroutineScope()
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(StudioTab.ARCHITECT) }
    var shareTargetProject by remember { mutableStateOf<SongProject?>(null) }
    var showIconStudio by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeVoiceProfile by repository.voiceProfile.collectAsState()
    val activeProject by repository.activeProject.collectAsState()

    // Full-bleed modern dark splash view with animated pulsing audio glyph
    if (showSplash) {
        SplashView(
            onSplashFinished = { showSplash = false }
        )
        return
    }

    // Handle back button: if not on Architect (Home), go back to Architect
    BackHandler(enabled = currentTab != StudioTab.ARCHITECT) {
        currentTab = StudioTab.ARCHITECT
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground),
        containerColor = StudioBackground,
        topBar = {
            VocalCraftTopBar(
                voiceProfileName = activeVoiceProfile.name,
                isVoiceCalibrated = activeVoiceProfile.isCalibrated,
                onVoiceBadgeClick = { currentTab = StudioTab.CALIBRATE },
                onAboutClick = { showAboutSheet = true },
                onIconStudioClick = { showIconStudio = true },
                onShareClick = activeProject?.let { proj -> { shareTargetProject = proj } }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = StudioSurface,
                tonalElevation = 8.dp
            ) {
                StudioTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    StudioTab.ARCHITECT -> if (isSelected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
                                    StudioTab.COVER -> if (isSelected) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq
                                    StudioTab.PLAYER -> if (isSelected) Icons.Filled.Equalizer else Icons.Outlined.Equalizer
                                    StudioTab.CALIBRATE -> if (isSelected) Icons.Filled.Mic else Icons.Outlined.Mic
                                    StudioTab.LIBRARY -> if (isSelected) Icons.Filled.FolderOpen else Icons.Outlined.FolderOpen
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    StudioTab.ARCHITECT -> {
                        SongArchitectScreen(
                            repository = repository,
                            onSongGenerated = { generatedProject ->
                                repository.setActiveProject(generatedProject)
                                currentTab = StudioTab.PLAYER
                            }
                        )
                    }
                    StudioTab.COVER -> {
                        CoverStudioScreen(
                            repository = repository,
                            onCoverGenerated = { coverProject ->
                                repository.setActiveProject(coverProject)
                                currentTab = StudioTab.PLAYER
                            }
                        )
                    }
                    StudioTab.PLAYER -> {
                        StudioPlayerScreen(
                            repository = repository,
                            onOpenShare = { proj ->
                                shareTargetProject = proj
                            }
                        )
                    }
                    StudioTab.CALIBRATE -> {
                        VoiceCalibrationScreen(
                            repository = repository,
                            onCalibrationFinished = {
                                currentTab = StudioTab.ARCHITECT
                            }
                        )
                    }
                    StudioTab.LIBRARY -> {
                        LibraryScreen(
                            repository = repository,
                            onSelectProject = { selectedProj ->
                                repository.setActiveProject(selectedProj)
                                currentTab = StudioTab.PLAYER
                            },
                            onOpenShare = { proj ->
                                shareTargetProject = proj
                            }
                        )
                    }
                }
            }
        }
    }

    // Export & Social Share Bottom Sheet
    shareTargetProject?.let { proj ->
        ModalBottomSheet(
            onDismissRequest = { shareTargetProject = null },
            sheetState = sheetState,
            containerColor = StudioBackground
        ) {
            SocialShareSheet(
                project = proj,
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                        shareTargetProject = null
                    }
                }
            )
        }
    }

    // Play Store Icon Studio Bottom Sheet
    if (showIconStudio) {
        ModalBottomSheet(
            onDismissRequest = { showIconStudio = false },
            containerColor = StudioBackground
        ) {
            PlayStoreIconStudioSheet(
                onDismiss = { showIconStudio = false }
            )
        }
    }

    // Official Elliott Media Group & Elliott Apps Credits Bottom Sheet
    if (showAboutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAboutSheet = false },
            containerColor = StudioBackground
        ) {
            AboutCreditsSheet(
                onDismiss = { showAboutSheet = false }
            )
        }
    }
}
