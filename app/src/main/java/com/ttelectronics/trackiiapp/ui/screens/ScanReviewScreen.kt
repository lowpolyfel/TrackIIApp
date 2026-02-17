package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun ScanReviewScreen(
    lotNumber: String,
    partNumber: String,
    onConfirm: () -> Unit,
    onRescan: () -> Unit,
    onHome: () -> Unit
) {
    TrackIIBackground(glowOffsetX = 10.dp, glowOffsetY = (-10).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Orden encontrada",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Confirma los datos antes de continuar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(TTGreenTint, Color.White)
                                )
                            )
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        ScanHighlightRow(label = "No. Lote", value = lotNumber)
                        ScanHighlightRow(label = "No. Parte", value = partNumber)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SoftActionButton(
                        text = "Escanear otra vez",
                        onClick = onRescan,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryGlowButton(
                        text = "Confirmar",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
            }
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
private fun ScanHighlightRow(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(TTAccent, TTBlueDark)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = TTTextSecondary
                )
                Text(
                    text = value.ifBlank { "Pendiente de escaneo" },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (value.isBlank()) TTTextSecondary else TTBlueDark
                )
            }
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (value.isBlank()) TTTextSecondary else TTGreen
            )
        }
    }
}
