package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.ScannerHeader
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun ScanReviewScreen(
    taskType: TaskType,
    lotNumber: String,
    partNumber: String,
    orderFound: Boolean,
    errorMessage: String,
    onConfirm: () -> Unit,
    onRescan: () -> Unit,
    onHome: () -> Unit
) {
    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Header superior
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScannerHeader(taskTitle = taskType.title)
            }

            // Contenedor Central para la Revisión
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (orderFound) "Lectura correcta, por favor revisa la información." else "Atención, ocurrió un problema con la lectura.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .zIndex(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icono de estado central
                        val iconColor = if (orderFound) TTGreen else TTRed
                        val statusIcon = if (orderFound) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline

                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(iconColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        // Textos de Lote y Parte
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Lote: $lotNumber",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Parte: $partNumber",
                                style = MaterialTheme.typography.titleMedium,
                                color = TTTextSecondary
                            )
                        }

                        if (!orderFound && errorMessage.isNotBlank()) {
                            androidx.compose.material3.Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = TTRed.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = TTRed,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botones de acción centralizados
                        if (orderFound) {
                            PrimaryGlowButton(
                                text = "Confirmar y Continuar",
                                onClick = onConfirm,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        SoftActionButton(
                            text = "Volver a Escanear",
                            onClick = onRescan,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
            )
        }
    }
}