package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VividViolet

@Composable
fun VocalCraftTopBar(
    modifier: Modifier = Modifier,
    voiceProfileName: String,
    isVoiceCalibrated: Boolean,
    onVoiceBadgeClick: () -> Unit,
    onAboutClick: () -> Unit,
    onIconStudioClick: () -> Unit,
    onShareClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("vocalcraft_top_bar"),
        color = StudioSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Mark & Brand Title with 'Powered by Elliott Apps' sub-badge
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAboutClick() }
                    .testTag("top_bar_brand_container"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stylized App Mark
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CyanAccent, ElectricIndigo, VividViolet)
                            )
                        )
                        .padding(1.5.dp)
                        .testTag("app_mark"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(7.dp))
                            .background(StudioBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "VocalCraft Mark",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(9.dp))

                Column {
                    Text(
                        text = "VocalCraft AI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.3.sp
                    )

                    // 'Powered by Elliott Apps' sub-badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("powered_by_sub_badge")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.5.dp)
                                .clip(CircleShape)
                                .background(CyanAccent)
                        )
                        Spacer(modifier = Modifier.width(4.5.dp))
                        Text(
                            text = "Powered by Elliott Apps",
                            fontSize = 9.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }

            // Voice Status Badge
            VoiceStatusBadge(
                isCalibrated = isVoiceCalibrated,
                voiceName = voiceProfileName.take(14),
                onClick = onVoiceBadgeClick
            )

            // About & Credits Action (Elliott Media Group & Elliott Apps)
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onAboutClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("about_credits_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About Elliott Apps & Studio Credits",
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Play Store Icon Studio Action
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(
                onClick = onIconStudioClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("icon_studio_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Play Store Icon Studio",
                    tint = VioletAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Optional Share Action
            if (onShareClick != null) {
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("top_bar_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Active Song",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
