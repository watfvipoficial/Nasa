package com.example.ui.hud

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CombatEventType
import com.example.model.CombatLogEvent

enum class LogFilter(val label: String) {
    ALL("TODOS"),
    COMBAT("COMBATE"),
    SYSTEMS("SISTEMAS"),
    ALERTS("ALERTAS")
}

@Composable
fun CombatLogOverlay(
    combatLog: List<CombatLogEvent>,
    onClearLog: () -> Unit,
    onUiTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(LogFilter.ALL) }
    val listState = rememberLazyListState()

    // Automatically scroll to the latest event when a new item is appended
    LaunchedEffect(combatLog.size, isExpanded) {
        if (combatLog.isNotEmpty() && isExpanded) {
            listState.animateScrollToItem(combatLog.size - 1)
        }
    }

    val filteredLog = remember(combatLog, selectedFilter) {
        when (selectedFilter) {
            LogFilter.ALL -> combatLog
            LogFilter.COMBAT -> combatLog.filter {
                it.type in listOf(CombatEventType.WEAPONS_FIRE, CombatEventType.ENEMY_DESTROYED, CombatEventType.BOSS_THREAT)
            }
            LogFilter.SYSTEMS -> combatLog.filter {
                it.type in listOf(CombatEventType.TACTICAL_INFO, CombatEventType.SYSTEM_DIVERT, CombatEventType.SALVAGE_ACQUIRED, CombatEventType.ORBITAL_EVENT)
            }
            LogFilter.ALERTS -> combatLog.filter {
                it.type in listOf(CombatEventType.SHIELD_WARNING, CombatEventType.HULL_CRITICAL, CombatEventType.BOSS_THREAT)
            }
        }
    }

    Column(
        modifier = modifier
            .testTag("combat_log_container")
            .widthIn(min = 280.dp, max = 380.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // COMPACT / COLLAPSED PILL BAR (Always visible)
        Surface(
            onClick = {
                onUiTap?.invoke()
                isExpanded = !isExpanded
            },
            shape = RoundedCornerShape(10.dp),
            color = Color(0xD909101E),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isExpanded) Color(0xFF00E5FF) else Color(0x6600E5FF)
            ),
            modifier = Modifier.testTag("combat_log_toggle_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsing Status Dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00E5FF), CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "LOG TÁCTICO",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.width(8.dp))

                    // Most recent log message preview if collapsed
                    if (!isExpanded && combatLog.isNotEmpty()) {
                        val latest = combatLog.last()
                        Text(
                            text = latest.message,
                            color = Color(latest.color),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else if (!isExpanded) {
                        Text(
                            text = "Esperando telemetría...",
                            color = Color(0xFF546E7A),
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Event Counter Badge
                    Surface(
                        color = Color(0x3300E5FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${combatLog.size}",
                            color = Color(0xFF80F2FF),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isExpanded) "Minimizar Registro" else "Expandir Registro",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // EXPANDED SCROLLABLE TERMINAL OVERLAY
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xE6080E1C),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Header with Filter Chips & Clear Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVENTOS DE COMBATE EN VIVO",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        IconButton(
                            onClick = {
                                onUiTap?.invoke()
                                onClearLog()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("combat_log_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpiar Registro",
                                tint = Color(0xFF78909C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LogFilter.entries.forEach { filter ->
                            val isSelected = (selectedFilter == filter)
                            Surface(
                                onClick = {
                                    onUiTap?.invoke()
                                    selectedFilter = filter
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0x331E3A5F),
                                modifier = Modifier
                                    .heightIn(min = 28.dp)
                                    .testTag("filter_chip_${filter.name.lowercase()}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = filter.label,
                                        color = if (isSelected) Color.Black else Color(0xFF90A4AE),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0x331E3A5F),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Scrollable Events Feed
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        if (filteredLog.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sin eventos registrados en este filtro",
                                    color = Color(0xFF546E7A),
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredLog, key = { it.id }) { event ->
                                    CombatLogItem(event = event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatLogItem(event: CombatLogEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x22000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp Tag (e.g. T+00:15)
        Text(
            text = event.timeLabel,
            color = Color(0xFF78909C),
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.width(6.dp))

        // Type Badge
        Surface(
            color = Color(event.color).copy(alpha = 0.22f),
            shape = RoundedCornerShape(3.dp),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(event.color).copy(alpha = 0.6f))
        ) {
            Text(
                text = event.type.tag,
                color = Color(event.color),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        Spacer(Modifier.width(6.dp))

        // Message body
        Text(
            text = event.message,
            color = Color(0xFFECEFF1),
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
