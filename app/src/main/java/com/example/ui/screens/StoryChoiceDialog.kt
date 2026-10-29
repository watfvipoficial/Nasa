package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChoiceOption
import com.example.model.StoryChoice

@Composable
fun StoryChoiceDialog(
    storyChoice: StoryChoice,
    onOptionSelected: (ChoiceOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF0F1726),
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = storyChoice.title,
                    color = Color(0xFF00E5FF),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = storyChoice.dilemma,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Text(
                    text = "Tu decisión afectará la moral de la humanidad y el final de la guerra espacial.",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                // Option A Card
                OutlinedCard(
                    onClick = { onOptionSelected(storyChoice.optionA) },
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0x3300E5FF)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choice_option_a")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Opción A: ${storyChoice.optionA.text}",
                            color = Color(0xFF80F2FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = storyChoice.optionA.consequence,
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "+${storyChoice.optionA.creditsReward} CR Adicionales",
                            color = Color(0xFFFFB300),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Option B Card
                OutlinedCard(
                    onClick = { onOptionSelected(storyChoice.optionB) },
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0x33FF1744)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF1744)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choice_option_b")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Opción B: ${storyChoice.optionB.text}",
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = storyChoice.optionB.consequence,
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "+${storyChoice.optionB.creditsReward} CR Adicionales",
                            color = Color(0xFFFFB300),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
