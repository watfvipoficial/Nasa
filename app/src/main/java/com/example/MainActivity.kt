package com.example

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.audio.AudioEngine
import com.example.campaign.CampaignData
import com.example.game.GameManager
import com.example.haptics.HapticController
import com.example.haptics.HapticPattern
import com.example.model.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MAIN_MENU,
    CAMPAIGN_MAP,
    SHIPYARD,
    SECONDARY_MISSIONS,
    ENDINGS,
    GAME
}

class MainActivity : ComponentActivity() {
    private val audioEngine = AudioEngine()
    private lateinit var hapticController: HapticController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hapticController = HapticController(this)
        audioEngine.start()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF060913)
                ) {
                    AstroStrikeApp(
                        audioEngine = audioEngine,
                        hapticController = hapticController
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioEngine.isPaused = true
    }

    override fun onResume() {
        super.onResume()
        audioEngine.isPaused = false
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
    }
}

@Composable
fun AstroStrikeApp(
    audioEngine: AudioEngine,
    hapticController: HapticController
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var returnScreenAfterMission by remember { mutableStateOf(AppScreen.CAMPAIGN_MAP) }

    // Persistent Player State
    var credits by remember { mutableIntStateOf(1800) }
    var exoticMatter by remember { mutableIntStateOf(35) }
    var playerCustomization by remember { mutableStateOf(ShipCustomization()) }
    var sectors by remember { mutableStateOf(CampaignData.initialSectors) }
    var secondaryMissions by remember { mutableStateOf(CampaignData.secondaryMissions) }
    var choicesHistory by remember { mutableStateOf(listOf<String>()) }
    var unlockedEndings by remember { mutableStateOf(listOf<GameEnding>()) }

    val gameManager = remember {
        GameManager(
            audioEngine = audioEngine,
            onVibratePattern = { pattern ->
                hapticController.trigger(pattern)
            }
        )
    }

    when (currentScreen) {
        AppScreen.MAIN_MENU -> {
            MainMenuScreen(
                credits = credits,
                exoticMatter = exoticMatter,
                audioEngine = audioEngine,
                hapticController = hapticController,
                onStartCampaign = { currentScreen = AppScreen.CAMPAIGN_MAP },
                onOpenShipyard = { currentScreen = AppScreen.SHIPYARD },
                onOpenSecondaryMissions = { currentScreen = AppScreen.SECONDARY_MISSIONS },
                onOpenEndings = { currentScreen = AppScreen.ENDINGS }
            )
        }

        AppScreen.CAMPAIGN_MAP -> {
            CampaignMapScreen(
                sectors = sectors,
                onSectorSelected = { sector ->
                    returnScreenAfterMission = AppScreen.CAMPAIGN_MAP
                    gameManager.startMission(sector, playerCustomization)
                    currentScreen = AppScreen.GAME
                },
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }

        AppScreen.SHIPYARD -> {
            ShipyardScreen(
                currentCustomization = playerCustomization,
                credits = credits,
                exoticMatter = exoticMatter,
                onSaveCustomization = { newCustomization, newCredits ->
                    playerCustomization = newCustomization
                    credits = newCredits
                },
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }

        AppScreen.SECONDARY_MISSIONS -> {
            SecondaryMissionsScreen(
                missions = secondaryMissions,
                onLaunchSecondary = { mission ->
                    returnScreenAfterMission = AppScreen.SECONDARY_MISSIONS
                    // Launch a skirmish mission based on this contract
                    val skirmishSector = CampaignSector(
                        id = 99,
                        name = mission.title,
                        location = mission.sectorName,
                        description = mission.description,
                        primaryTarget = "Completar contrato de la NASA",
                        bossLeader = EnemyLeaderInfo(
                            name = "Comandante Raider",
                            title = "Corsario del Cinturón Exterior",
                            flagshipName = "Raider Dreadnought",
                            faction = "Corsarios Espaciales",
                            maxHealth = 2600f,
                            specialAttack = "Salva de Torpedos de Plasma",
                            portraitColor = 0xFFFF6D00,
                            introQuote = "¡Nadie intercepta nuestro botín! ¡Abran fuego!"
                        ),
                        enemyFleetSize = 10,
                        rewardCredits = mission.rewardCredits,
                        rewardExoticMatter = 30,
                        isUnlocked = true
                    )
                    gameManager.startMission(skirmishSector, playerCustomization)
                    currentScreen = AppScreen.GAME
                },
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }

        AppScreen.ENDINGS -> {
            EndingsScreen(
                unlockedEndings = unlockedEndings,
                choicesHistory = choicesHistory,
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }

        AppScreen.GAME -> {
            GameScreen(
                gameManager = gameManager,
                onMissionCompleted = { earnedCredits, earnedExotic, choiceSummary ->
                    gameManager.endMission()
                    credits += earnedCredits
                    exoticMatter += earnedExotic

                    if (choiceSummary != null) {
                        choicesHistory = choicesHistory + choiceSummary
                    }

                    val completedId = gameManager.currentSector.id
                    if (completedId == 99) {
                        currentScreen = returnScreenAfterMission
                    } else {
                        // Mark current sector as completed and unlock next
                        sectors = sectors.map { s ->
                            when {
                                s.id == completedId -> s.copy(isCompleted = true)
                                s.id == completedId + 1 -> s.copy(isUnlocked = true)
                                else -> s
                            }
                        }

                        // Check endings unlock
                        if (completedId == 5) {
                            val humanistCount = choicesHistory.count { it.contains("Civil", true) || it.contains("Paz", true) || it.contains("Rescate", true) }
                            val aggressiveCount = choicesHistory.count { it.contains("Militar", true) || it.contains("Ojivas", true) || it.contains("Fuego", true) }

                            val ending = when {
                                humanistCount >= 3 -> GameEnding.GUARDIAN_OF_SOLAR_SYSTEM
                                aggressiveCount >= 3 -> GameEnding.VOID_DREADNOUGHT
                                else -> GameEnding.QUANTUM_ASCENDANCE
                            }

                            if (!unlockedEndings.contains(ending)) {
                                unlockedEndings = unlockedEndings + ending
                            }
                        }

                        currentScreen = AppScreen.CAMPAIGN_MAP
                    }
                },
                onExitMission = {
                    gameManager.endMission()
                    currentScreen = returnScreenAfterMission
                }
            )
        }
    }
}
