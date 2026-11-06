package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldMaster
import com.example.ui.theme.MagentaVocal
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

data class IconConcept(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val description: String,
    val hexPrimary: String,
    val hexSecondary: String,
    val hexBackground: String,
    val contrastRatio: String,
    val safeZoneCompliance: String,
    val svgCode: String
)

val PRODUCTION_SVG_CODE = """
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <defs>
    <!-- Studio Obsidian Radial Gradient Background -->
    <radialGradient id="bgGlow" cx="50%" cy="45%" r="65%">
      <stop offset="0%" stop-color="#14192E"/>
      <stop offset="55%" stop-color="#0E1220"/>
      <stop offset="100%" stop-color="#070911"/>
    </radialGradient>
    
    <!-- Neon Vibrant Gradients -->
    <linearGradient id="cyanMagenta" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#00F2FE"/>
      <stop offset="50%" stop-color="#8A2BE2"/>
      <stop offset="100%" stop-color="#FF2E93"/>
    </linearGradient>

    <linearGradient id="waveGrad" x1="0%" y1="50%" x2="100%" y2="50%">
      <stop offset="0%" stop-color="#FF2E93" stop-opacity="0.2"/>
      <stop offset="60%" stop-color="#FF2E93" stop-opacity="0.95"/>
      <stop offset="100%" stop-color="#00F2FE"/>
    </linearGradient>

    <linearGradient id="neuralGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#00F2FE"/>
      <stop offset="100%" stop-color="#BD00FF"/>
    </linearGradient>

    <filter id="neonGlow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="8" result="blur" />
      <feMerge>
        <feMergeNode in="blur" />
        <feMergeNode in="SourceGraphic" />
      </feMerge>
    </filter>
  </defs>

  <!-- 512x512 Full Bleed Canvas -->
  <rect width="512" height="512" fill="url(#bgGlow)"/>

  <!-- Safe Zone Visual Guide: 384x384 (64px margin) -->
  <g id="safe-zone-content">
    
    <!-- Left: Acoustic Audio Waveform Frequency Pulses -->
    <g filter="url(#neonGlow)">
      <line x1="88" y1="256" x2="88" y2="256" stroke="#FF2E93" stroke-width="4" stroke-linecap="round"/>
      <line x1="104" y1="230" x2="104" y2="282" stroke="#FF2E93" stroke-width="4" stroke-linecap="round"/>
      <line x1="120" y1="200" x2="120" y2="312" stroke="#FF2E93" stroke-width="4.5" stroke-linecap="round"/>
      <line x1="136" y1="160" x2="136" y2="352" stroke="#FF2E93" stroke-width="5" stroke-linecap="round"/>
      <line x1="152" y1="210" x2="152" y2="302" stroke="#FF2E93" stroke-width="5" stroke-linecap="round"/>
      <line x1="168" y1="130" x2="168" y2="382" stroke="#FF2E93" stroke-width="5.5" stroke-linecap="round"/>
      <line x1="184" y1="180" x2="184" y2="332" stroke="#FF2E93" stroke-width="5.5" stroke-linecap="round"/>
      <line x1="200" y1="220" x2="200" y2="292" stroke="#FF2E93" stroke-width="5" stroke-linecap="round"/>
    </g>

    <!-- Center: Iconic Studio Condenser Microphone Capsule -->
    <g filter="url(#neonGlow)">
      <!-- Outer Capsule Silhouette -->
      <rect x="216" y="116" width="80" height="150" rx="40" fill="#0C101C" stroke="url(#cyanMagenta)" stroke-width="7"/>
      
      <!-- Microphone Acoustic Mesh Pattern -->
      <path d="M226,140 h60 M226,155 h60 M226,170 h60 M226,185 h60 M226,200 h60 M226,215 h60" stroke="#00F2FE" stroke-width="1.8" stroke-dasharray="3,3" stroke-opacity="0.7"/>

      <!-- Shock Mount Ring -->
      <circle cx="256" cy="275" r="44" fill="none" stroke="url(#cyanMagenta)" stroke-width="6"/>
      <line x1="256" y1="290" x2="256" y2="368" stroke="url(#cyanMagenta)" stroke-width="9" stroke-linecap="round"/>
      <line x1="220" y1="368" x2="292" y2="368" stroke="url(#cyanMagenta)" stroke-width="8" stroke-linecap="round"/>
    </g>

    <!-- Right: Generative AI Neural Constellation & Notes -->
    <g filter="url(#neonGlow)">
      <!-- Connected Neural Lattice Lines -->
      <line x1="296" y1="240" x2="340" y2="195" stroke="#00F2FE" stroke-width="2.2" stroke-opacity="0.8"/>
      <line x1="340" y1="195" x2="395" y2="160" stroke="#8A2BE2" stroke-width="2.2" stroke-opacity="0.8"/>
      <line x1="340" y1="195" x2="365" y2="260" stroke="#00F2FE" stroke-width="2.2" stroke-opacity="0.8"/>
      <line x1="365" y1="260" x2="415" y2="245" stroke="#FF2E93" stroke-width="2.2" stroke-opacity="0.8"/>
      <line x1="365" y1="260" x2="385" y2="330" stroke="#BD00FF" stroke-width="2.2" stroke-opacity="0.8"/>
      <line x1="296" y1="275" x2="365" y2="260" stroke="#00F2FE" stroke-width="2.2" stroke-opacity="0.8"/>
      <line x1="415" y1="245" x2="435" y2="256" stroke="#00F2FE" stroke-width="2.2" stroke-opacity="0.8"/>

      <!-- Neural Synapse Data Nodes -->
      <circle cx="340" cy="195" r="7" fill="#00F2FE"/>
      <circle cx="395" cy="160" r="8" fill="#FF2E93"/>
      <circle cx="365" cy="260" r="7.5" fill="#00F2FE"/>
      <circle cx="415" cy="245" r="6.5" fill="#FF2E93"/>
      <circle cx="385" cy="330" r="7" fill="#BD00FF"/>
      <circle cx="435" cy="256" r="5" fill="#00F2FE"/>

      <!-- Floating Musical Eighth Note Nodes -->
      <path d="M388,145 v-15 h14 v15 m-14,-5 h14" stroke="#00F2FE" stroke-width="2.5" fill="none"/>
      <circle cx="385" cy="148" r="3.5" fill="#00F2FE"/>
      <circle cx="399" cy="143" r="3.5" fill="#00F2FE"/>

      <path d="M375,305 v-14 h12 v14" stroke="#FF2E93" stroke-width="2" fill="none"/>
      <circle cx="372" cy="307" r="3" fill="#FF2E93"/>
      <circle cx="384" cy="303" r="3" fill="#FF2E93"/>
    </g>

  </g>
</svg>
""".trimIndent()

val CONCEPTS = listOf(
    IconConcept(
        id = "concept_c_neural_mic",
        title = "Concept C: Neural Studio Microphone",
        subtitle = "Primary Production Choice (Reference Match)",
        tag = "SELECTED PRODUCTION ASSET",
        description = "A precision studio condenser capsule centered in the safe area. Acoustic vocal frequencies enter from the left in neon magenta, pass through the mic's glowing acoustic mesh, and resolve into a generative neural network constellation of glowing cyan data nodes and musical notes.",
        hexPrimary = "#00F2FE (Cyber Cyan)",
        hexSecondary = "#FF2E93 (Electric Magenta)",
        hexBackground = "#0B0E1A (Midnight Obsidian)",
        contrastRatio = "11.8:1 (AAA High Contrast)",
        safeZoneCompliance = "100% inside 384x384 central safe zone (64px margins)",
        svgCode = PRODUCTION_SVG_CODE
    ),
    IconConcept(
        id = "concept_a_minimalist",
        title = "Concept A: Modern Geometric Monolith",
        subtitle = "Ultra-Scalable Vector Silhouette",
        tag = "HIGH-SCALABILITY CONCEPT",
        description = "Clean geometric interplay of concentric singing frequency rings forming an abstract microphone silhouette. Designed for extreme legibility down to 24dp notification icons with bold, unfilled vector channels and gradient accents.",
        hexPrimary = "#00E5FF (Electric Cyan)",
        hexSecondary = "#8A2BE2 (Neon Violet)",
        hexBackground = "#090C15 (Obsidian Slate)",
        contrastRatio = "13.4:1 (AAA High Contrast)",
        safeZoneCompliance = "100% inside 384x384 central safe zone",
        svgCode = """
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <rect width="512" height="512" fill="#090C15"/>
  <circle cx="256" cy="220" r="72" fill="none" stroke="#00E5FF" stroke-width="14"/>
  <rect x="224" y="148" width="64" height="136" rx="32" fill="#090C15" stroke="#8A2BE2" stroke-width="14"/>
  <line x1="256" y1="292" x2="256" y2="376" stroke="#00E5FF" stroke-width="14" stroke-linecap="round"/>
  <line x1="208" y1="376" x2="304" y2="376" stroke="#00E5FF" stroke-width="14" stroke-linecap="round"/>
</svg>
""".trimIndent()
    ),
    IconConcept(
        id = "concept_b_expressive",
        title = "Concept B: Dynamic Harmonic Fluid Wave",
        subtitle = "Fluid Neon Emotion & Prosody",
        tag = "EXPRESSIVE & EMOTIONAL",
        description = "Intersecting 3D neon ribbons that flow from vocal chords into harmonized musical waveforms. Represents the emotional valence and prosody of the singing voice transformed into studio-quality musical arrangements.",
        hexPrimary = "#A855F7 (Electric Violet)",
        hexSecondary = "#00F2FE (Cyan) & #F43F5E (Magenta)",
        hexBackground = "#0D111E (Studio Deep Navy)",
        contrastRatio = "10.2:1 (AAA High Contrast)",
        safeZoneCompliance = "100% inside 384x384 central safe zone",
        svgCode = """
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <rect width="512" height="512" fill="#0D111E"/>
  <path d="M110,256 C170,140 230,370 290,200 C340,90 410,256 410,256" fill="none" stroke="#00F2FE" stroke-width="18" stroke-linecap="round"/>
  <path d="M102,256 C162,372 222,142 282,312 C332,422 402,256 402,256" fill="none" stroke="#F43F5E" stroke-width="18" stroke-linecap="round"/>
</svg>
""".trimIndent()
    )
)

@Composable
fun PlayStoreIconStudioSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    var previewMask by remember { mutableStateOf("squircle") } // "squircle", "circle", "square"

    val activeConcept = CONCEPTS[selectedIndex]

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground),
        color = StudioBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play Store Icon Studio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "512×512 Google Play asset & Android Adaptive Icon layers",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Concept Selection Tabs
            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = StudioSurfaceVariant,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = CyanAccent,
                        height = 3.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                CONCEPTS.forEachIndexed { idx, concept ->
                    Tab(
                        selected = selectedIndex == idx,
                        onClick = { selectedIndex = idx },
                        text = {
                            Text(
                                text = "Concept ${('A' + idx)}",
                                fontSize = 12.sp,
                                fontWeight = if (selectedIndex == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive Preview Canvas
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeConcept.tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )

                                // Mask selector (Squircle, Circle, Square Full Bleed)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "squircle" to "Play Mask (20%)",
                                        "circle" to "Pixel / Round",
                                        "square" to "Full Bleed 512"
                                    ).forEach { (mask, label) ->
                                        val isChosen = previewMask == mask
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isChosen) CyanAccent.copy(alpha = 0.25f) else StudioSurfaceVariant)
                                                .border(1.dp, if (isChosen) CyanAccent else StudioCardBorder, RoundedCornerShape(6.dp))
                                                .clickable { previewMask = mask }
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (mask == "squircle") "Play Mask" else if (mask == "circle") "Round" else "512 Raw",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isChosen) CyanAccent else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Icon Canvas Preview Box
                            val shapeModifier = when (previewMask) {
                                "squircle" -> Modifier.clip(RoundedCornerShape(44.dp)) // ~20% of 220dp
                                "circle" -> Modifier.clip(CircleShape)
                                else -> Modifier.clip(RoundedCornerShape(0.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .then(shapeModifier)
                                    .border(
                                        width = if (previewMask == "square") 1.dp else 0.dp,
                                        color = StudioCardBorder,
                                        shape = RoundedCornerShape(if (previewMask == "squircle") 44.dp else 0.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.google_play_icon_512),
                                    contentDescription = "Production Play Store Icon",
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Draw Safe Zone overlay if in square raw mode
                                if (previewMask == "square") {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // 384 safe zone is 75% of 512, so (200 * 0.75) = 150dp
                                        val safeSize = size.width * (384f / 512f)
                                        val offset = (size.width - safeSize) / 2f
                                        drawRect(
                                            color = Color(0x6600E5FF),
                                            topLeft = Offset(offset, offset),
                                            size = Size(safeSize, safeSize),
                                            style = Stroke(width = 2f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (previewMask == "square") "Showing 384px Safe Area Guide (cyan boundary)" else "Rendered with Google Play Dynamic Corner Masking",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Specifications & Color Palette Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = activeConcept.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = activeConcept.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Specs Grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Primary Hue", fontSize = 10.sp, color = TextMuted)
                                    Text(activeConcept.hexPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                }
                                Column {
                                    Text("Accent Hue", fontSize = 10.sp, color = TextMuted)
                                    Text(activeConcept.hexSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MagentaVocal)
                                }
                                Column {
                                    Text("Canvas Fill", fontSize = 10.sp, color = TextMuted)
                                    Text(activeConcept.hexBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Contrast Ratio", fontSize = 10.sp, color = TextMuted)
                                    Text(activeConcept.contrastRatio, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldMaster)
                                }
                                Column {
                                    Text("Dimensions", fontSize = 10.sp, color = TextMuted)
                                    Text("512 × 512 px sRGB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column {
                                    Text("Safe Area", fontSize = 10.sp, color = TextMuted)
                                    Text("384 × 384 px (64px margin)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                }
                            }
                        }
                    }
                }

                // Android Adaptive Layers Separation Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Layers, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Android Adaptive Icon Layer Architecture",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• Background Layer (432 × 432 px): Solid Studio Obsidian #0B0E1A (app/src/main/res/drawable/ic_launcher_background.xml)\n" +
                                       "• Foreground Layer (432 × 432 px): Centered 66dp icon safe viewport inside 108dp canvas (app/src/main/res/drawable/ic_launcher_foreground.xml)\n" +
                                       "• Raster Fallbacks: Generated in all 5 density buckets (mdpi:48, hdpi:72, xhdpi:96, xxhdpi:144, xxxhdpi:192).",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Copy Production SVG Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(activeConcept.svgCode))
                        Toast.makeText(context, "Production SVG copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_svg_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Production SVG", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "512x512 PNG ready at drawable/google_play_icon_512.png", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("512 PNG", fontSize = 13.sp)
                }
            }
        }
    }
}
