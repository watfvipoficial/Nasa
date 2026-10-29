package com.example.ui.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.Vector2D
import com.example.game.GameManager
import com.example.game.GameUIState
import com.example.haptics.HapticPattern
import com.example.model.PowerSystem
import kotlin.math.roundToInt

@Composable
fun TacticalHudOverlay(
    gameManager: GameManager,
    uiState: GameUIState,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // TOP TELEMETRY BAR
        TopTelemetryHeader(
            uiState = uiState,
            onPauseClick = onPauseClick,
            onUiTap = { gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // BOSS HEALTH BAR (If active)
        AnimatedVisibility(
            visible = uiState.bossActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        ) {
            BossHealthDisplay(
                bossName = uiState.bossName,
                healthPercent = uiState.bossHealthPercent
            )
        }

        // TARGET LOCK-ON CARD (Top Right under header)
        if (uiState.lockedTargetName != null) {
            TargetLockDisplay(
                targetName = uiState.lockedTargetName,
                distance = uiState.lockedTargetDistance,
                hullPercent = uiState.lockedTargetHullPercent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
            )
        }

        // REAL-TIME COMBAT LOG OVERLAY (Top-Left under header)
        CombatLogOverlay(
            combatLog = uiState.combatLog,
            onClearLog = { gameManager.clearCombatLog() },
            onUiTap = { gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 76.dp, start = 16.dp)
        )

        // BOTTOM CENTER STATUS GAUGES (Hull, Shield, Energy)
        ShipVitalsCard(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        // LEFT VIRTUAL FLIGHT JOYSTICK
        VirtualJoystick(
            onVectorChanged = { vec ->
                gameManager.thrustVector = vec
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 28.dp)
        )

        // RIGHT COMBAT CLUSTER (Weapons, Secondary, Power diverted)
        CombatActionControls(
            gameManager = gameManager,
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
        )
    }
}

@Composable
private fun TopTelemetryHeader(
    uiState: GameUIState,
    onPauseClick: () -> Unit,
    onUiTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xCC0D1424),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sector info & Orbit state
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Cuerpo Celeste",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = uiState.nearestBody.uppercase(),
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (uiState.crashWarning) "⚠️ RIESGO DE COLISIÓN"
                        else if (uiState.isStableOrbit) "🛰️ ÓRBITA ESTABLE"
                        else "🚀 TRAYECTORIA LIBRE",
                        color = if (uiState.crashWarning) Color(0xFFFF1744) else Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "V: ${(uiState.speedKmh / 100f).roundToInt()} km/s  |  Alt: ${uiState.altitudeKm.roundToInt()} km",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
            }

            // Score & Enemies remaining
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ENEMIGOS: ${uiState.enemiesRemaining}",
                        color = if (uiState.enemiesRemaining > 0) Color(0xFFFFB300) else Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "PUNTOS: ${uiState.currentScore}",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        onUiTap()
                        onPauseClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pausar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BossHealthDisplay(
    bossName: String,
    healthPercent: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.widthIn(max = 420.dp),
        color = Color(0xD91A0A10),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF1744))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⚠️ $bossName".uppercase(),
                    color = Color(0xFFFF1744),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${(healthPercent * 100).roundToInt()}%",
                    color = Color(0xFFFF1744),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { healthPercent.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF1744),
                trackColor = Color(0xFF3E1018)
            )
        }
    }
}

@Composable
private fun TargetLockDisplay(
    targetName: String,
    distance: Float,
    hullPercent: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(180.dp),
        color = Color(0xCC0D1424),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFFF1744), CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "BLANCO FIJADO",
                    color = Color(0xFFFF1744),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = targetName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "Distancia: ${distance.roundToInt()} m",
                color = Color(0xFF90A4AE),
                fontSize = 10.sp
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { hullPercent.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFFF1744),
                trackColor = Color(0xFF263238)
            )
        }
    }
}

@Composable
private fun ShipVitalsCard(
    uiState: GameUIState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.widthIn(max = 340.dp),
        color = Color(0xCC090E1A),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Escudos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ESCUDOS", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${uiState.playerShield.roundToInt()}/${uiState.playerMaxShield.roundToInt()}", color = Color(0xFF80F2FF), fontSize = 10.sp)
            }
            LinearProgressIndicator(
                progress = { (uiState.playerShield / uiState.playerMaxShield).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF0D253B)
            )

            Spacer(Modifier.height(6.dp))

            // Casco
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CASCO", color = if (uiState.playerHull < 300f) Color(0xFFFF1744) else Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${uiState.playerHull.roundToInt()}/${uiState.playerMaxHull.roundToInt()}", color = Color.White, fontSize = 10.sp)
            }
            LinearProgressIndicator(
                progress = { (uiState.playerHull / uiState.playerMaxHull).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (uiState.playerHull < 300f) Color(0xFFFF1744) else Color(0xFF00E676),
                trackColor = Color(0xFF1A261E)
            )

            Spacer(Modifier.height(6.dp))

            // Energía Reactor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ENERGÍA", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${uiState.playerEnergy.roundToInt()}/${uiState.playerMaxEnergy.roundToInt()}", color = Color(0xFFFFE082), fontSize = 10.sp)
            }
            LinearProgressIndicator(
                progress = { (uiState.playerEnergy / uiState.playerMaxEnergy).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFFFB300),
                trackColor = Color(0xFF3E2C08)
            )
        }
    }
}

@Composable
private fun VirtualJoystick(
    onVectorChanged: (Vector2D) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 60f

    Box(
        modifier = modifier
            .size(130.dp)
            .background(Color(0x3300E5FF), CircleShape)
            .border(2.dp, Color(0x6600E5FF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        val dist = delta.getDistance()
                        val clamped = if (dist > maxRadius) delta * (maxRadius / dist) else delta
                        knobOffset = clamped
                        onVectorChanged(Vector2D(clamped.x / maxRadius, clamped.y / maxRadius))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clamped = if (dist > maxRadius) newOffset * (maxRadius / dist) else newOffset
                        knobOffset = clamped
                        onVectorChanged(Vector2D(clamped.x / maxRadius, clamped.y / maxRadius))
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onVectorChanged(Vector2D.Zero)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onVectorChanged(Vector2D.Zero)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Center crosshair
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Vector Guía",
            tint = Color(0x3300E5FF),
            modifier = Modifier.size(24.dp)
        )

        // Joystick knob
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(46.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF80F2FF), Color(0xFF00B0FF))
                    ),
                    CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
private fun CombatActionControls(
    gameManager: GameManager,
    uiState: GameUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Power Divert Preset Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            PowerPresetButton(
                label = "ARMAS",
                icon = Icons.Default.Bolt,
                isSelected = uiState.powerSystem == PowerSystem.WEAPONS_FOCUS,
                onClick = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    gameManager.divertPower(PowerSystem.WEAPONS_FOCUS)
                }
            )
            PowerPresetButton(
                label = "ESCUDOS",
                icon = Icons.Default.Shield,
                isSelected = uiState.powerSystem == PowerSystem.SHIELDS_FOCUS,
                onClick = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    gameManager.divertPower(PowerSystem.SHIELDS_FOCUS)
                }
            )
            PowerPresetButton(
                label = "MOTORES",
                icon = Icons.Default.Speed,
                isSelected = uiState.powerSystem == PowerSystem.ENGINES_FOCUS,
                onClick = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    gameManager.divertPower(PowerSystem.ENGINES_FOCUS)
                }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SECONDARY ORDNANCE BUTTON
            val ordnance = gameManager.playerCustomization.secondaryOrdnance
            val isCooldown = uiState.secondaryCooldownPercent > 0.01f

            Button(
                onClick = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    if (!isCooldown) {
                        gameManager.isTriggeringSecondary = true
                    }
                },
                enabled = !isCooldown,
                modifier = Modifier
                    .size(62.dp)
                    .testTag("secondary_weapon_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6D00),
                    disabledContainerColor = Color(0xFF422610)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = ordnance.displayName,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    if (isCooldown) {
                        Text(
                            text = "${(uiState.secondaryCooldownPercent * ordnance.cooldown).roundToInt()}s",
                            color = Color(0xFFFFCC80),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "ORD",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // PRIMARY FIRE BUTTON (TOUCH & HOLD TO FIRE)
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFF1744), Color(0xFFB71C1C))
                        ),
                        CircleShape
                    )
                    .border(3.dp, Color(0xFFFF8A80), CircleShape)
                    .testTag("primary_fire_button")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { gameManager.isFiringPrimary = true },
                            onDrag = { change, _ ->
                                change.consume()
                                gameManager.isFiringPrimary = true
                            },
                            onDragEnd = { gameManager.isFiringPrimary = false },
                            onDragCancel = { gameManager.isFiringPrimary = false }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Disparar Cañón",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "FUEGO",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerPresetButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF00E5FF) else Color(0x990D1424),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color.White else Color(0xFF1E3A5F)
        ),
        modifier = Modifier
            .heightIn(min = 36.dp)
            .testTag("power_preset_${label.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else Color(0xFF90A4AE),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) Color.Black else Color(0xFF90A4AE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
