package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipyardScreen(
    currentCustomization: ShipCustomization,
    credits: Int,
    exoticMatter: Int,
    onSaveCustomization: (ShipCustomization, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedClass by remember { mutableStateOf(currentCustomization.shipClass) }
    var selectedWeapon by remember { mutableStateOf(currentCustomization.primaryWeapon) }
    var selectedOrdnance by remember { mutableStateOf(currentCustomization.secondaryOrdnance) }
    var selectedReactor by remember { mutableStateOf(currentCustomization.reactor) }
    var selectedThruster by remember { mutableStateOf(currentCustomization.thruster) }
    var selectedArmor by remember { mutableStateOf(currentCustomization.armor) }
    var selectedColor by remember { mutableLongStateOf(currentCustomization.paintColor) }
    var shipNameInput by remember { mutableStateOf(currentCustomization.shipName) }

    var currentCredits by remember { mutableIntStateOf(credits) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val activeCustomization = remember(selectedClass, selectedWeapon, selectedOrdnance, selectedReactor, selectedThruster, selectedArmor, selectedColor, shipNameInput) {
        ShipCustomization(
            shipClass = selectedClass,
            primaryWeapon = selectedWeapon,
            secondaryOrdnance = selectedOrdnance,
            reactor = selectedReactor,
            thruster = selectedThruster,
            armor = selectedArmor,
            paintColor = selectedColor,
            shipName = shipNameInput
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF060913),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ASTILLERO ESPACIAL NASA",
                            color = Color(0xFF00E5FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Personalización Técnica de Naves Nodrizas y Flota",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("shipyard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        color = Color(0x33FFB300),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$currentCredits CR", color = Color(0xFFFFB300), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1424)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LIVE SHIP TELEMETRY & STATS CARD
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = Color(0xFF0D172A),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(selectedColor))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedClass.displayName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = selectedClass.role,
                                color = Color(selectedColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Ship Icon Preview
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0x33000000), CircleShape)
                                .border(2.dp, Color(selectedColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = Color(selectedColor),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "CASCO TOTAL", value = "${activeCustomization.totalMaxHull.roundToInt()} HP", color = Color(0xFF00E676))
                        StatItem(label = "ESCUDOS", value = "${activeCustomization.totalMaxShield.roundToInt()} MW", color = Color(0xFF00E5FF))
                        StatItem(label = "REACTOR", value = "${activeCustomization.totalMaxEnergy.roundToInt()} GW", color = Color(0xFFFFB300))
                        StatItem(label = "VELOCIDAD", value = "${activeCustomization.totalSpeed.roundToInt()}", color = Color(0xFFD500F9))
                    }
                }
            }

            // 1. CHASSIS / SHIP CLASS SELECTION
            Text(
                text = "1. SELECCIONA EL CHASIS ESPACIAL",
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ShipClass.entries) { sClass ->
                    val isSelected = (sClass == selectedClass)
                    Surface(
                        onClick = {
                            if (currentCredits >= sClass.cost || sClass.cost == 0) {
                                selectedClass = sClass
                            } else {
                                statusMessage = "Fondos insuficientes para este chasis"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0x3300E5FF) else Color(0xFF0E1A2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E2D4A)
                        ),
                        modifier = Modifier.width(180.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(sClass.displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(sClass.role, color = Color(0xFF90A4AE), fontSize = 10.5.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(sClass.description, color = Color(0xFF78909C), fontSize = 10.sp, maxLines = 3)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (sClass.cost == 0) "EQUIPADO" else "${sClass.cost} CR",
                                color = if (sClass.cost == 0) Color(0xFF00E676) else Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 2. PRIMARY WEAPON SELECTION
            Text(
                text = "2. BATERÍA DE ARMAS PRINCIPALES",
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(WeaponType.entries) { weapon ->
                    val isSelected = (weapon == selectedWeapon)
                    Surface(
                        onClick = { selectedWeapon = weapon },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0x3300E5FF) else Color(0xFF0E1A2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) Color(weapon.color) else Color(0xFF1E2D4A)
                        ),
                        modifier = Modifier.width(170.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(weapon.displayName, color = Color(weapon.color), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Text("Daño: ${weapon.damage.roundToInt()}  |  Cadencia: ${weapon.fireRate}/s", color = Color(0xFFB0BEC5), fontSize = 10.5.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(weapon.description, color = Color(0xFF78909C), fontSize = 10.sp, maxLines = 2)
                        }
                    }
                }
            }

            // 3. SECONDARY TACTICAL ORDNANCE
            Text(
                text = "3. SISTEMA TÁCTICO SECUNDARIO",
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(OrdnanceType.entries) { ord ->
                    val isSelected = (ord == selectedOrdnance)
                    Surface(
                        onClick = { selectedOrdnance = ord },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0x33FF6D00) else Color(0xFF0E1A2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) Color(0xFFFF6D00) else Color(0xFF1E2D4A)
                        ),
                        modifier = Modifier.width(170.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(ord.displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Text("Enfriamiento: ${ord.cooldown}s", color = Color(0xFFFFB300), fontSize = 10.5.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(ord.description, color = Color(0xFF78909C), fontSize = 10.sp, maxLines = 2)
                        }
                    }
                }
            }

            // 4. PAINT SCHEME / COLOR
            Text(
                text = "4. DISTINTIVO DE CASCO Y ESCUDO NASA",
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            val colors = listOf(
                0xFF00E5FF to "Cian Artemis",
                0xFFFFB300 to "Oro Solar",
                0xFFFF1744 to "Carmesí Titan",
                0xFFD500F9 to "Plasma Cuántico",
                0xFF00E676 to "Esmeralda Iónico"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                colors.forEach { (cLong, label) ->
                    val isSelected = (selectedColor == cLong)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedColor = cLong }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(cLong), CircleShape)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(label, color = if (isSelected) Color.White else Color(0xFF78909C), fontSize = 9.sp)
                    }
                }
            }

            // Status feedback message if any
            statusMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // SAVE & APPLY BUTTON
            Button(
                onClick = {
                    onSaveCustomization(activeCustomization, currentCredits)
                    statusMessage = "¡Configuración guardada en la matriz de la flota NASA!"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_shipyard_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CONFIRMAR ARMAMENTO Y CASCO",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color(0xFF90A4AE), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}
