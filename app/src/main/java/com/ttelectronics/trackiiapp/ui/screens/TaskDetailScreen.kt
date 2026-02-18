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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.SuccessOverlayDialog
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIIDropdownField
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import kotlinx.coroutines.delay

@Composable
fun TaskDetailScreen(
    taskType: TaskType,
    lotNumber: String,
    partNumber: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onHome: () -> Unit
) {
    val infoItems = listOf(
        InfoItem("Área", "Pendiente API", Icons.Rounded.Factory),
        InfoItem("Familia", "Pendiente API", Icons.Rounded.Category),
        InfoItem("Subfamilia", "Pendiente API", Icons.Rounded.Inventory2),
        InfoItem("No. de ruta", "Pendiente API", Icons.Rounded.Route),
        InfoItem("Cantidad de piezas", "Pendiente API", Icons.Rounded.FormatListNumbered)
    )
    val localities = listOf("Localidad A", "Localidad B", "Localidad C")
    val flowSteps = remember(taskType) {
        when (taskType) {
            TaskType.ProductAdvance -> listOf("Recepción", "Inspección", "Ensamble", "Empaque")
            TaskType.TravelSheet -> listOf("Línea A", "Prueba", "Calidad", "Salida")
            TaskType.CancelOrder -> listOf("Captura", "Validación", "Autorización", "Cancelada")
            TaskType.Rework -> listOf("Entrada", "Diagnóstico", "Retrabajo", "Liberación")
        }
    }
    val currentStepIndex = remember(taskType, lotNumber, partNumber) {
        if (flowSteps.size <= 1) 0
        else {
            val seed = (lotNumber + partNumber + taskType.route).hashCode().let { if (it == Int.MIN_VALUE) 0 else kotlin.math.abs(it) }
            1 + (seed % (flowSteps.size - 1))
        }
    }
    var showSuccess by remember { mutableStateOf(false) }
    val rightSoundPlayer = rememberRawSoundPlayer("right")

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            rightSoundPlayer.play()
        }
    }

    if (showSuccess) {
        LaunchedEffect(Unit) {
            delay(1400)
            onComplete()
        }
    }

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                        ScanHighlightCard(lotNumber = lotNumber, partNumber = partNumber)
                        InfoGrid(items = infoItems)
                        ProductFlowDashboard(
                            steps = flowSteps,
                            currentStepIndex = currentStepIndex,
                            modifier = Modifier.fillMaxWidth()
                        )
                        when (taskType) {
                            TaskType.ProductAdvance,
                            TaskType.TravelSheet -> Unit
                            TaskType.CancelOrder -> CancelReasonDropdown()
                            TaskType.Rework -> TrackIIDropdownField(
                                label = "Localidad de retrabajo",
                                options = localities,
                                helper = "Opciones desde API"
                            )
                        }
                        PrimaryGlowButton(
                            text = when (taskType) {
                                TaskType.ProductAdvance -> "Agregar"
                                TaskType.TravelSheet -> "Visualizar estado"
                                else -> "Guardar"
                            },
                            onClick = { showSuccess = true },
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
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            )
            SuccessOverlayDialog(
                title = "Registro exitoso",
                message = "Los datos fueron guardados correctamente.",
                show = showSuccess
            )
        }
    }
}

@Composable
private fun CancelReasonDropdown() {
    val reasons = listOf(
        "Motivo genérico",
        "Daño en material",
        "Falta de especificación",
        "Orden duplicada"
    )
    TrackIIDropdownField(
        label = "Motivo de cancelación",
        options = reasons,
        helper = "Opciones desde API"
    )
}

private data class InfoItem(val title: String, val value: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private data class FlowStep(val title: String, val isActive: Boolean, val isCompleted: Boolean)

@Composable
private fun InfoGrid(items: List<InfoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    InfoTile(
                        title = item.title,
                        value = item.value,
                        icon = item.icon,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InfoTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(TTGreenTint, Color.White)
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TTGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = TTTextSecondary
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProductFlowDashboard(
    steps: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    val safeIndex = currentStepIndex.coerceIn(0, (steps.lastIndex).coerceAtLeast(0))
    val flow = steps.mapIndexed { index, title ->
        FlowStep(
            title = title,
            isActive = index == safeIndex,
            isCompleted = index < safeIndex
        )
    }

    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(TTBlueTint.copy(alpha = 0.45f), Color.White)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Ruta actual del producto",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TTTextSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salida: ${flow.firstOrNull()?.title.orEmpty()}",
                style = MaterialTheme.typography.labelMedium,
                color = TTTextSecondary
            )
            Text(
                text = "Destino: ${flow.lastOrNull()?.title.orEmpty()}",
                style = MaterialTheme.typography.labelMedium,
                color = TTTextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            flow.forEachIndexed { index, step ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (step.isActive) 34.dp else 28.dp)
                            .background(
                                color = when {
                                    step.isCompleted -> TTGreen.copy(alpha = 0.18f)
                                    step.isActive -> TTBlue.copy(alpha = 0.18f)
                                    else -> Color.White
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                step.isCompleted -> Icons.Rounded.CheckCircle
                                step.isActive -> Icons.Rounded.RadioButtonUnchecked
                                else -> Icons.Rounded.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = when {
                                step.isCompleted -> TTGreen
                                step.isActive -> TTBlue
                                else -> TTTextSecondary
                            },
                            modifier = Modifier.size(if (step.isActive) 22.dp else 18.dp)
                        )
                    }
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (step.isActive) TTBlue else TTTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                if (index != flow.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .size(height = 3.dp, width = 24.dp)
                            .background(
                                color = if (index < safeIndex) TTGreen.copy(alpha = 0.7f) else TTTextSecondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanHighlightCard(lotNumber: String, partNumber: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(TTAccent.copy(alpha = 0.3f), TTBlueTint.copy(alpha = 0.6f))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Datos escaneados",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TTTextSecondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ScanValueBlock(label = "No. Lote", value = lotNumber, modifier = Modifier.weight(1f))
            ScanValueBlock(label = "No. Parte", value = partNumber, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ScanValueBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = TTTextSecondary
        )
        Text(
            text = value.ifBlank { "Pendiente" },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
