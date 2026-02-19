package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIIDropdownField
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

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
    val routeStatus = remember(taskType, lotNumber, partNumber) {
        mockRouteStatusFromScan(taskType = taskType, lot = lotNumber, part = partNumber)
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
                        ProductRouteDashboard(status = routeStatus)

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
                                TaskType.TravelSheet -> "Visualizar estado"
                                TaskType.ProductAdvance -> "Agregar"
                                else -> "Guardar"
                            },
                            onClick = onComplete,
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
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
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

private data class InfoItem(val title: String, val value: String, val icon: ImageVector)
private data class ProductRouteStatus(
    val previousStep: String,
    val currentStep: String,
    val nextStep: String,
    val source: String,
    val destination: String
)

private fun mockRouteStatusFromScan(taskType: TaskType, lot: String, part: String): ProductRouteStatus {
    val steps = when (taskType) {
        TaskType.ProductAdvance -> listOf("Recepción", "Inspección", "Ensamble", "Empaque")
        TaskType.TravelSheet -> listOf("Inicio", "Proceso", "Final")
        TaskType.CancelOrder -> listOf("Captura", "Validación", "Cancelada")
        TaskType.Rework -> listOf("Entrada", "Retrabajo", "Liberación")
    }
    val idxSeed = kotlin.math.abs((lot + part + taskType.route).hashCode()) % steps.size
    val currentIndex = idxSeed.coerceIn(0, steps.lastIndex)
    val prev = if (currentIndex > 0) steps[currentIndex - 1] else "Sin anterior"
    val next = if (currentIndex < steps.lastIndex) steps[currentIndex + 1] else "Sin siguiente"
    return ProductRouteStatus(
        previousStep = prev,
        currentStep = steps[currentIndex],
        nextStep = next,
        source = steps.first(),
        destination = steps.last()
    )
}

@Composable
private fun ProductRouteDashboard(status: ProductRouteStatus) {
    val breath = rememberInfiniteTransition(label = "breath")
    val scale = breath.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "breathScale"
    ).value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(TTBlueTint.copy(alpha = 0.5f), Color.White)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Ruta actual del producto",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TTTextSecondary
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Salida: ${status.source}",
                style = MaterialTheme.typography.labelMedium,
                color = TTTextSecondary
            )
            Text(
                text = "Destino: ${status.destination}",
                style = MaterialTheme.typography.labelMedium,
                color = TTTextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RouteNode(label = "Anterior", value = status.previousStep, isCurrent = false)
            RouteNode(label = "Actual", value = status.currentStep, isCurrent = true, scale = scale)
            RouteNode(label = "Siguiente", value = status.nextStep, isCurrent = false)
        }
    }
}

@Composable
private fun RouteNode(label: String, value: String, isCurrent: Boolean, scale: Float = 1f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(if (isCurrent) 84.dp else 56.dp)
                .scale(if (isCurrent) scale else 1f)
                .background(
                    color = if (isCurrent) TTGreen.copy(alpha = 0.24f) else TTBlue.copy(alpha = 0.14f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isCurrent) TTGreen else TTBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TTTextSecondary)
    }
}

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
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoTile(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(TTGreenTint, Color.White)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TTGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
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
private fun ScanHighlightCard(
    lotNumber: String,
    partNumber: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(TTAccent.copy(alpha = 0.3f), TTBlueTint.copy(alpha = 0.6f))),
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
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
        Text(
            text = value.ifBlank { "Pendiente" },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
