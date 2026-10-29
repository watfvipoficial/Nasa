package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameEnding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndingsScreen(
    unlockedEndings: List<GameEnding>,
    choicesHistory: List<String>,
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
                            text = "CRÓNICAS Y FINALES DE LA GUERRA",
                            color = Color(0xFF00E5FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Desenlaces basados en tus decisiones tácticas",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("endings_back_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = Color(0x33D500F9),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66D500F9))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SISTEMA DE MÚLTIPLES FINALES DE LA NASA",
                            color = Color(0xFFE1BEE7),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Cada sector plantea dilemas entre preservar vidas civiles, apropiarse de tecnología alienígena o ejecutar ofensivas de aniquilación total. Tus elecciones desbloquean distintos destinos cósmicos.",
                            color = Color(0xFFF3E5F5),
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            items(GameEnding.entries) { ending ->
                val isUnlocked = unlockedEndings.contains(ending)
                EndingCard(ending = ending, isUnlocked = isUnlocked)
            }

            if (choicesHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "HISTORIAL DE DECISIONES TOMADAS:",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(choicesHistory) { choice ->
                    Surface(
                        color = Color(0xFF0D172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("•", color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text(choice, color = Color.White, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EndingCard(ending: GameEnding, isUnlocked: Boolean) {
    val borderColor = if (isUnlocked) Color(0xFFD500F9) else Color(0xFF1E293B)
    val bgColor = if (isUnlocked) Color(0xFF15102A) else Color(0xFF090E17)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ending.badge,
                    color = if (isUnlocked) Color(0xFFE1BEE7) else Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = if (isUnlocked) Color(0x3300E676) else Color(0x3364748B),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "DESBLOQUEADO" else "NO ALCANZADO",
                        color = if (isUnlocked) Color(0xFF00E676) else Color(0xFF64748B),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = ending.title,
                color = if (isUnlocked) Color.White else Color(0xFF64748B),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = ending.subtitle,
                color = if (isUnlocked) Color(0xFF00E5FF) else Color(0xFF475569),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isUnlocked) ending.description else "Completa la campaña tomando decisiones acordes a esta doctrina estratégica para revelar este final.",
                color = if (isUnlocked) Color(0xFFCFD8DC) else Color(0xFF475569),
                fontSize = 11.5.sp
            )
        }
    }
}
