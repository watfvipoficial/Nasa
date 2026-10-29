package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MissionType
import com.example.model.SecondaryMission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryMissionsScreen(
    missions: List<SecondaryMission>,
    onLaunchSecondary: (SecondaryMission) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF060913),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CONTRATOS DE EXPLORACIÓN Y DEFENSA",
                            color = Color(0xFF00E5FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Misiones Secundarias de Alta Rejugabilidad de la NASA",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("secondary_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1424)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = Color(0x33FFB300),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFB300))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Las misiones secundarias ofrecen combate táctico procedural, recompensas elevadas de créditos para la compra de naves y perfeccionamiento de habilidades orbitales.",
                            color = Color(0xFFFFECB3),
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            items(missions) { mission ->
                MissionCard(
                    mission = mission,
                    onLaunch = { onLaunchSecondary(mission) }
                )
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: SecondaryMission,
    onLaunch: () -> Unit
) {
    val icon = when (mission.type) {
        MissionType.ASTEROID_MINING_DEFENSE -> Icons.Default.Shield
        MissionType.ORBITAL_SALVAGE_RUN -> Icons.Default.AutoAwesome
        MissionType.SURVIVOR_ESCORT -> Icons.Default.Groups
        MissionType.ALIEN_RAIDER_HUNT -> Icons.Default.GpsFixed
    }

    val typeTag = when (mission.type) {
        MissionType.ASTEROID_MINING_DEFENSE -> "DEFENSA DE MINERÍA"
        MissionType.ORBITAL_SALVAGE_RUN -> "RECUPERACIÓN ORBITAL"
        MissionType.SURVIVOR_ESCORT -> "ESCOLTA CIENTÍFICA"
        MissionType.ALIEN_RAIDER_HUNT -> "CAZA DE RECOMPENSAS"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0D172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0x3300E5FF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = typeTag,
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = mission.sectorName,
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mission.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = mission.description,
                color = Color(0xFFB0BEC5),
                fontSize = 11.5.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recompensa: +${mission.rewardCredits} CR",
                    color = Color(0xFFFFB300),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onLaunch,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    modifier = Modifier.testTag("launch_secondary_${mission.id}")
                ) {
                    Text("ACEPTAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
