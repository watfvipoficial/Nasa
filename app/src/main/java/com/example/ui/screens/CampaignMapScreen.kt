package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CampaignSector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignMapScreen(
    sectors: List<CampaignSector>,
    onSectorSelected: (CampaignSector) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedSectorForModal by remember { mutableStateOf<CampaignSector?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF060913),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CAMPAÑA DEL SISTEMA SOLAR",
                            color = Color(0xFF00E5FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Ruta de Reconquista y Defensa Planetaria NASA",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("campaign_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar al Menú",
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = Color(0x3300E5FF),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E5FF))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Avanza sector por sector liberando los mundos clave del pozo gravitatorio solar. Cada victoria otorga tecnología y fondos para mejorar tu nave nodriza.",
                            color = Color(0xFFE0F7FA),
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            items(sectors) { sector ->
                SectorCard(
                    sector = sector,
                    onClick = {
                        if (sector.isUnlocked) {
                            selectedSectorForModal = sector
                        }
                    }
                )
            }

            item {
                Spacer(Modifier.height(30.dp))
            }
        }

        // Sector Launch Modal
        selectedSectorForModal?.let { sector ->
            SectorBriefingModal(
                sector = sector,
                onDismiss = { selectedSectorForModal = null },
                onLaunch = {
                    val s = selectedSectorForModal
                    selectedSectorForModal = null
                    if (s != null) onSectorSelected(s)
                }
            )
        }
    }
}

@Composable
private fun SectorCard(
    sector: CampaignSector,
    onClick: () -> Unit
) {
    val isLocked = !sector.isUnlocked
    val cardBg = if (isLocked) Color(0xFF090E17) else Color(0xFF0D172A)
    val borderColor = when {
        sector.isCompleted -> Color(0xFF00E676)
        sector.isUnlocked -> Color(0xFF00E5FF)
        else -> Color(0xFF1E293B)
    }

    Surface(
        onClick = onClick,
        enabled = sector.isUnlocked,
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sector_card_${sector.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isLocked) Color(0xFF1E293B) else Color(0x3300E5FF),
                                CircleShape
                            )
                            .border(1.dp, borderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sector.id}",
                            color = if (isLocked) Color(0xFF64748B) else Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = sector.name,
                            color = if (isLocked) Color(0xFF64748B) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sector.location,
                            color = if (isLocked) Color(0xFF475569) else Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = when {
                        sector.isCompleted -> Color(0x3300E676)
                        sector.isUnlocked -> Color(0x3300E5FF)
                        else -> Color(0x3364748B)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when {
                            sector.isCompleted -> "LIBERADO"
                            sector.isUnlocked -> "EN COMBATE"
                            else -> "BLOQUEADO"
                        },
                        color = when {
                            sector.isCompleted -> Color(0xFF00E676)
                            sector.isUnlocked -> Color(0xFF00E5FF)
                            else -> Color(0xFF64748B)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = sector.description,
                color = if (isLocked) Color(0xFF475569) else Color(0xFFB0BEC5),
                fontSize = 11.5.sp,
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            // Boss & Fleet Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x40000000), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isLocked) Color(0xFF475569) else Color(sector.bossLeader.portraitColor),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Líder: ${sector.bossLeader.name}",
                        color = if (isLocked) Color(0xFF64748B) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${sector.rewardCredits} CR",
                        color = if (isLocked) Color(0xFF64748B) else Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "+${sector.rewardExoticMatter} MT",
                        color = if (isLocked) Color(0xFF64748B) else Color(0xFFD500F9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SectorBriefingModal(
    sector: CampaignSector,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1424),
        title = {
            Column {
                Text(
                    text = "ORDEN DE OPERACIONES NASA",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = sector.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = sector.description,
                    color = Color(0xFFCFD8DC),
                    fontSize = 12.5.sp
                )

                Surface(
                    color = Color(0x331A0A10),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FF1744))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "AMENAZA ALIENÍGENA IDENTIFICADA:",
                            color = Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${sector.bossLeader.name} - ${sector.bossLeader.title}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"${sector.bossLeader.introQuote}\"",
                            color = Color(0xFFFFCDD2),
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Ataque Especial: ${sector.bossLeader.specialAttack}",
                            color = Color(0xFFFFB300),
                            fontSize = 10.5.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Flota Hostil: ~${sector.enemyFleetSize} Cazas", color = Color(0xFF90A4AE), fontSize = 11.sp)
                    Text("Recompensa: ${sector.rewardCredits} CR / ${sector.rewardExoticMatter} MT", color = Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                modifier = Modifier.testTag("launch_mission_button")
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(6.dp))
                Text("DESPLEGAR FLOTA", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color(0xFF90A4AE))
            }
        }
    )
}
