package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIReadOnlyField
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun TaskDetailScreen(
    taskType: TaskType,
    lotNumber: String,
    partNumber: String,
    onBack: () -> Unit
) {
    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = taskType.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Información capturada desde el escaneo.",
                style = MaterialTheme.typography.bodyMedium,
                color = TTTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
            )
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TrackIIReadOnlyField(
                        label = "No. Lote",
                        value = lotNumber,
                        helper = "Aún no capturado"
                    )
                    TrackIIReadOnlyField(
                        label = "No. Parte",
                        value = partNumber,
                        helper = "Aún no capturado"
                    )
                    when (taskType) {
                        TaskType.TravelSheet -> Unit
                        TaskType.CancelOrder -> CancelReasonDropdown()
                        TaskType.Rework -> TrackIITextField(label = "Motivo del retrabajo")
                    }
                    PrimaryGlowButton(
                        text = if (taskType == TaskType.TravelSheet) "Agregar" else "Guardar",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            SoftActionButton(
                text = "Volver",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CancelReasonDropdown() {
    val reasons = listOf(
        "Motivo genérico",
        "Daño en material",
        "Falta de especificación",
        "Orden duplicada"
    )
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(reasons.first()) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selected,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(text = "Motivo de cancelación") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TTBlueTint,
                unfocusedContainerColor = TTBlueTint,
                disabledContainerColor = TTBlueTint,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            reasons.forEach { reason ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(reason) },
                    onClick = {
                        selected = reason
                        expanded = false
                    }
                )
            }
        }
    }
}
