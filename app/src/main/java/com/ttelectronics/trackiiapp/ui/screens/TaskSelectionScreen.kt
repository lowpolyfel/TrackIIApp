package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.TaskCard
import com.ttelectronics.trackiiapp.ui.components.TopAccountButton
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
    onAccount: () -> Unit,
    onLogout: () -> Unit,
    username: String,
    locationName: String,
    deviceName: String
) {
    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = 80.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Selecciona una tarea",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Elige el flujo que necesitas ejecutar en esta estación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TaskCard(
                            title = "Seguimiento de hojas viajeras",
                            description = "Consulta y registra el estatus del viajero durante su recorrido.",
                            icon = Icons.Rounded.Description,
                            onClick = { onTaskSelected(TaskType.TravelSheet) },
                            accentColor = TTGreen,
                            accentDark = TTGreenDark,
                            accentTint = TTGreenTint
                        )
                        TaskCard(
                            title = "Avanzar producto",
                            description = "Registra piezas completadas y mueve la orden al siguiente paso.",
                            icon = Icons.Rounded.Description,
                            onClick = { onTaskSelected(TaskType.ProductAdvance) },
                            accentColor = TTBlue,
                            accentDark = TTBlueDark,
                            accentTint = TTBlueTint
                        )
                        TaskCard(
                            title = "Cancelar orden",
                            description = "Detén una orden activa y registra su cierre de forma controlada.",
                            icon = Icons.Rounded.HighlightOff,
                            onClick = { onTaskSelected(TaskType.CancelOrder) },
                            accentColor = TTRed,
                            accentDark = TTRedDark,
                            accentTint = TTRedTint
                        )
                        TaskCard(
                            title = "Retrabajo",
                            description = "Envía piezas a retrabajo o gestiona su liberación según la localidad.",
                            icon = Icons.Rounded.Build,
                            onClick = { onTaskSelected(TaskType.Rework) },
                            accentColor = TTYellow,
                            accentDark = TTYellowDark,
                            accentTint = TTYellowTint
                        )
                    }
                }
            }

            TopAccountButton(
                onClick = onAccount,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 20.dp)
            ) {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Localidad",
                            style = MaterialTheme.typography.labelSmall,
                            color = TTTextSecondary
                        )
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TTBlueDark
                        )
                        OutlinedButton(onClick = onLogout) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Rounded.Logout,
                                contentDescription = "Cerrar sesión",
                                modifier = Modifier.size(16.dp),
                                tint = TTBlue
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(text = "Cerrar")
                        }
                    }
                }
            }

            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }
}
