package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.AudioEngine
import com.example.haptics.HapticController
import com.example.haptics.HapticPattern

@Composable
fun MainMenuScreen(
    credits: Int,
    exoticMatter: Int,
    audioEngine: AudioEngine,
    hapticController: HapticController,
    onStartCampaign: () -> Unit,
    onOpenShipyard: () -> Unit,
    onOpenSecondaryMissions: () -> Unit,
    onOpenEndings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAudioDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060913))
    ) {
        // Hero Background Image with Gradient Overlay
        Image(
            painter = painterResource(id = R.drawable.mothership_hero),
            contentDescription = "Batalla Espacial NASA",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark sci-fi gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x9903050A),
                            Color(0xCC060913),
                            Color(0xF2060913),
                            Color(0xFF060913)
                        )
                    )
                )
        )

        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: NASA INSIGNIA & RESOURCE WALLET
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // NASA Tag
                Surface(
                    color = Color(0x990D1424),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00E5FF), CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "NASA DEEP SPACE DEFENSE",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Audio Settings Button
                IconButton(
                    onClick = { showAudioDialog = true },
                    modifier = Modifier
                        .background(Color(0x990D1424), CircleShape)
                        .border(1.dp, Color(0xFF1E3A5F), CircleShape)
                        .testTag("audio_settings_button")
                ) {
                    Icon(
                        imageVector = if (audioEngine.isMusicEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Ajustes de Sonido",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // TITLE AND BRANDING
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0x33FF1744),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x80FF1744)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "ESTADO DE EMERGENCIA SOLAR: NIVEL 5",
                        color = Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "ASTROSTRIKE",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "GUERRA ESPACIAL Y FÍSICA ORBITAL",
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Comanda la flota nodriza de la NASA en tiempo real, domina la atracción gravitacional de los mundos solares y salva a la Tierra de la invasión alienígena.",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(18.dp))

                // Wallet Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ResourceBadge(
                        label = "CRÉDITOS",
                        value = "$credits CR",
                        color = Color(0xFFFFB300),
                        icon = Icons.Default.MonetizationOn
                    )
                    ResourceBadge(
                        label = "MATERIA EXÓTICA",
                        value = "$exoticMatter MT",
                        color = Color(0xFFD500F9),
                        icon = Icons.Default.Science
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // NAVIGATION BUTTONS CLUSTER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Start Button
                Button(
                    onClick = {
                        hapticController.trigger(HapticPattern.UI_TAP)
                        onStartCampaign()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_campaign_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color(0xFF00363F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "INICIAR CAMPAÑA PLANETARIA",
                        color = Color(0xFF00363F),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                // Shipyard Button
                OutlinedButton(
                    onClick = {
                        hapticController.trigger(HapticPattern.UI_TAP)
                        onOpenShipyard()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("shipyard_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x330D1424)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "ASTILLERO & FLOTA NODRIZA NASA",
                        color = Color(0xFFFFB300),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                // Secondary Missions & Endings Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            hapticController.trigger(HapticPattern.UI_TAP)
                            onOpenSecondaryMissions()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("secondary_missions_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x22152238)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = Color(0xFF80D8FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "MISIONES EXTRA",
                            color = Color(0xFF80D8FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            hapticController.trigger(HapticPattern.UI_TAP)
                            onOpenEndings()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("endings_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x22152238)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFD500F9),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "FINALES & HISTORIA",
                            color = Color(0xFFD500F9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // AUDIO & HAPTIC SETTINGS DIALOG
        if (showAudioDialog) {
            AudioSettingsDialog(
                audioEngine = audioEngine,
                hapticController = hapticController,
                onDismiss = { showAudioDialog = false }
            )
        }
    }
}

@Composable
private fun ResourceBadge(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = Color(0xCC0D1424),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, color = Color(0xFF90A4AE), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun AudioSettingsDialog(
    audioEngine: AudioEngine,
    hapticController: HapticController,
    onDismiss: () -> Unit
) {
    var musicEnabled by remember { mutableStateOf(audioEngine.isMusicEnabled) }
    var sfxEnabled by remember { mutableStateOf(audioEngine.isSfxEnabled) }
    var hapticEnabled by remember { mutableStateOf(hapticController.isVibrationEnabled) }
    var volume by remember { mutableFloatStateOf(audioEngine.masterVolume) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1424),
        title = {
            Text(
                text = "AUDIO Y RESPUESTA HÁPTICA TÁCTICA",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Banda sonora adaptativa que modula el ritmo según la intensidad del combate y patrones de vibración háptica para explosiones e impactos.",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Banda Sonora Dinámica", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = musicEnabled,
                        onCheckedChange = {
                            musicEnabled = it
                            audioEngine.isMusicEnabled = it
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Efectos de Batalla (SFX)", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = sfxEnabled,
                        onCheckedChange = {
                            sfxEnabled = it
                            audioEngine.isSfxEnabled = it
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vibración Háptica Táctica", color = Color.White, fontSize = 13.sp)
                        Text("Impactos cinéticos y explosiones", color = Color(0xFF90A4AE), fontSize = 10.sp)
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = {
                            hapticEnabled = it
                            hapticController.isVibrationEnabled = it
                            if (it) hapticController.trigger(HapticPattern.KINETIC_IMPACT)
                        }
                    )
                }

                Column {
                    Text("Volumen Maestro: ${(volume * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            audioEngine.masterVolume = it
                        },
                        valueRange = 0f..1f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("ACEPTAR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
