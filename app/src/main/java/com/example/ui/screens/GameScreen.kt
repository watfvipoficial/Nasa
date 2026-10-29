package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameManager
import com.example.haptics.HapticPattern
import com.example.model.ChoiceOption
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    gameManager: GameManager,
    onMissionCompleted: (credits: Int, exoticMatter: Int, choiceMade: String?) -> Unit,
    onExitMission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPaused by remember { mutableStateOf(false) }
    var pendingChoiceMade by remember { mutableStateOf<ChoiceOption?>(null) }
    var showStoryDilemma by remember { mutableStateOf(false) }

    val uiState by gameManager.uiState.collectAsState()

    BackHandler {
        isPaused = true
    }

    // 60FPS High-Performance Game Loop
    LaunchedEffect(isPaused) {
        if (isPaused) return@LaunchedEffect

        var lastNanos = System.nanoTime()
        while (isActive) {
            withFrameNanos { now ->
                val dt = (now - lastNanos) / 1_000_000_000f
                lastNanos = now
                gameManager.update(dt)
            }
        }
    }

    // Trigger story dilemma once mission is accomplished
    LaunchedEffect(uiState.isMissionAccomplished) {
        if (uiState.isMissionAccomplished && gameManager.currentSector.storyChoice != null && pendingChoiceMade == null) {
            showStoryDilemma = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03050A))
    ) {
        // Space Graphic Engine Canvas
        com.example.ui.game.SpaceCanvasRenderer(
            gameManager = gameManager,
            modifier = Modifier.fillMaxSize()
        )

        // Tactical HUD and Controls Layer
        com.example.ui.hud.TacticalHudOverlay(
            gameManager = gameManager,
            uiState = uiState,
            onPauseClick = { isPaused = true }
        )

        // 1. STORY CHOICE MODAL (If sector contains dilemma)
        if (showStoryDilemma && gameManager.currentSector.storyChoice != null) {
            val choice = gameManager.currentSector.storyChoice!!
            StoryChoiceDialog(
                storyChoice = choice,
                onOptionSelected = { option ->
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    pendingChoiceMade = option
                    showStoryDilemma = false
                }
            )
        }

        // 2. MISSION ACCOMPLISHED MODAL
        if (uiState.isMissionAccomplished && !showStoryDilemma) {
            MissionAccomplishedDialog(
                sectorName = gameManager.currentSector.name,
                score = uiState.currentScore,
                credits = uiState.creditsEarned + (pendingChoiceMade?.creditsReward ?: 0),
                exoticMatter = uiState.exoticMatterEarned,
                choiceOutcome = pendingChoiceMade?.consequence,
                onContinue = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    val summary = pendingChoiceMade?.let { "${gameManager.currentSector.name}: ${it.text}" }
                    onMissionCompleted(
                        uiState.creditsEarned + (pendingChoiceMade?.creditsReward ?: 0),
                        uiState.exoticMatterEarned,
                        summary
                    )
                }
            )
        }

        // 3. GAME OVER MODAL (Ship destroyed)
        if (uiState.isGameOver) {
            GameOverDialog(
                onRetry = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    gameManager.startMission(gameManager.currentSector, gameManager.playerCustomization)
                },
                onExit = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    onExitMission()
                }
            )
        }

        // 4. PAUSE MODAL
        if (isPaused) {
            PauseDialog(
                onResume = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    isPaused = false
                },
                onRestart = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    isPaused = false
                    gameManager.startMission(gameManager.currentSector, gameManager.playerCustomization)
                },
                onExit = {
                    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
                    onExitMission()
                }
            )
        }
    }
}

@Composable
private fun MissionAccomplishedDialog(
    sectorName: String,
    score: Int,
    credits: Int,
    exoticMatter: Int,
    choiceOutcome: String?,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF0F1B2E),
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "¡SECTOR LIBERADO!",
                    color = Color(0xFF00E676),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = sectorName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "La flota enemiga ha sido erradicada del pozo orbital. La NASA ha asegurado las estaciones de comando.",
                    color = Color(0xFFCFD8DC),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                if (choiceOutcome != null) {
                    Surface(
                        color = Color(0x3300E5FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Resultado Táctico: $choiceOutcome",
                            color = Color(0xFF80F2FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF080D17),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PUNTOS", color = Color(0xFF90A4AE), fontSize = 10.sp)
                            Text("$score", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CRÉDITOS", color = Color(0xFFFFB300), fontSize = 10.sp)
                            Text("+$credits CR", color = Color(0xFFFFB300), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MATERIA", color = Color(0xFFD500F9), fontSize = 10.sp)
                            Text("+$exoticMatter MT", color = Color(0xFFD500F9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("victory_continue_button")
            ) {
                Text("CONTINUAR EXPEDICIÓN", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }
    )
}

@Composable
private fun GameOverDialog(
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF1E0A10),
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = Color(0xFFFF1744),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "NAVE DESTRUIDA",
                    color = Color(0xFFFF1744),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Text(
                text = "Tu nave nodriza sucumbió al fuego concentrado de la flota alienígena. La cápsula de escape de la NASA está lista para desplegar un nuevo caza de asalto.",
                color = Color(0xFFFFCDD2),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retry_mission_button")
            ) {
                Text("REINTENTAR MISIÓN", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RETIRARSE AL MENÚ", color = Color(0xFFB0BEC5))
            }
        }
    )
}

@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onResume,
        containerColor = Color(0xFF0D1424),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "OPERACIONES EN PAUSA",
                color = Color(0xFF00E5FF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("REANUDAR COMBATE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("REINICIAR SECTOR", color = Color.White)
                }
                TextButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ABANDONAR AL MENÚ", color = Color(0xFFFF5252))
                }
            }
        },
        confirmButton = {}
    )
}
