package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.TopAccountButton
import com.ttelectronics.trackiiapp.ui.components.TaskCard
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenDark
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTRedDark
import com.ttelectronics.trackiiapp.ui.theme.TTRedTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.theme.TTYellow
import com.ttelectronics.trackiiapp.ui.theme.TTYellowDark
import com.ttelectronics.trackiiapp.ui.theme.TTYellowTint

@Composable
fun TaskSelectionScreen(
    onTaskSelected: (TaskType) -> Unit,
    onHome: () -> Unit,
    onAccount: () -> Unit
) {
    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = 80.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Selecciona una tarea",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Procesos optimizados para TT Electronics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
                )
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TaskCard(
                            title = "Seguimiento de hojas viajeras",
                            icon = Icons.Rounded.Description,
                            onClick = { onTaskSelected(TaskType.TravelSheet) },
                            accentColor = TTGreen,
                            accentDark = TTGreenDark,
                            accentTint = TTGreenTint
                        )
                        TaskCard(
                            title = "Avanzar producto",
                            icon = Icons.Rounded.Description,
                            onClick = { onTaskSelected(TaskType.ProductAdvance) },
                            accentColor = TTBlue,
                            accentDark = TTBlueDark,
                            accentTint = TTBlueTint
                        )
                        TaskCard(
                            title = "Cancelar Orden",
                            icon = Icons.Rounded.HighlightOff,
                            onClick = { onTaskSelected(TaskType.CancelOrder) },
                            accentColor = TTRed,
                            accentDark = TTRedDark,
                            accentTint = TTRedTint
                        )
                        TaskCard(
                            title = "Retrabajo",
                            icon = Icons.Rounded.Build,
                            onClick = { onTaskSelected(TaskType.Rework) },
                            accentColor = TTYellow,
                            accentDark = TTYellowDark,
                            accentTint = TTYellowTint
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            TopAccountButton(
                onClick = onAccount,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
            )
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }
}
