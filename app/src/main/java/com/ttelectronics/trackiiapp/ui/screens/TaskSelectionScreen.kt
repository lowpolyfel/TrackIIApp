package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TaskCard
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun TaskSelectionScreen(onBackToLogin: () -> Unit) {
    TrackIIBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    TaskCard(title = "Seguimiento de hojas viajeras")
                    TaskCard(title = "Cancelar Orden")
                    TaskCard(title = "Retrabajo")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            SoftActionButton(text = "Volver al inicio", onClick = onBackToLogin)
        }
    }
}
