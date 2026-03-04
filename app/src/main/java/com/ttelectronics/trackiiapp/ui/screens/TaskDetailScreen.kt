package com.ttelectronics.trackiiapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIIDropdownField
import com.ttelectronics.trackiiapp.ui.components.SuccessOverlay
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.TaskDetailViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.TaskDetailViewModelFactory

data class InfoItem(val title: String, val value: String, val icon: ImageVector)

data class ProductRouteStatus(
    val isStarted: Boolean,
    val isEligible: Boolean,
    val currentLocationName: String,
    val previousLocationName: String,
    val nextLocationName: String
)

@Composable
fun TaskDetailScreen(
    taskType: TaskType,
    lotNumber: String,
    partNumber: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onNavigateToPartialScrap: (Int) -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val rightSoundPlayer = rememberRawSoundPlayer("right")
    val authRepository = ServiceLocator.authRepository(context)
    val auth = authRepository.sessionSnapshot()
    val vm: TaskDetailViewModel = viewModel(
        factory = TaskDetailViewModelFactory(
            ServiceLocator.scannerRepository(context),
            authRepository
        )
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(partNumber, lotNumber, auth.deviceId) {
        vm.loadData(partNumber, lotNumber, auth.deviceId)
    }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            rightSoundPlayer.play()
            kotlinx.coroutines.delay(1800)

            if (uiState.piecesDifference > 0) {
                onNavigateToPartialScrap(uiState.piecesDifference)
            } else {
                onComplete()
            }
        }
    }

    val isDataReady = !uiState.isLoading
    val part = uiState.partInfo
    val ctx = if (isDataReady) uiState.contextInfo else null
    val userLocation = auth.locationName.trim()

    val isEligible = if (ctx?.isNew == true) {
        ctx.currentStepName?.trim()?.equals(userLocation, ignoreCase = true) ?: false
    } else {
        ctx?.nextSteps?.firstOrNull()?.locationName?.trim()?.equals(userLocation, ignoreCase = true) ?: false
    }

    val nextLoc = if (ctx?.isNew == true) {
        ctx.nextSteps?.firstOrNull { it.stepNumber == 2 }?.locationName ?: "Paso 2"
    } else {
        ctx?.nextSteps?.firstOrNull()?.locationName ?: "Fin de ruta"
    }

    val routeStatus = ProductRouteStatus(
        isStarted = ctx?.isNew == false,
        isEligible = isEligible,
        currentLocationName = ctx?.currentStepName ?: "Localidad desconocida",
        previousLocationName = ctx?.currentStepName ?: "Sin localidad previa",
        nextLocationName = nextLoc // <- Usamos la nueva variable aquí
    )

    val infoItems = listOf(
        InfoItem("No. Lote", lotNumber, Icons.Rounded.Inventory2),
        InfoItem("No. Parte", partNumber, Icons.Rounded.QrCode),
        InfoItem("Área", part?.areaName ?: "Pendiente", Icons.Rounded.Factory),
        InfoItem("Familia", part?.familyName ?: "Pendiente", Icons.Rounded.Category),
        InfoItem("Subfamilia", part?.subfamilyName ?: "Pendiente", Icons.Rounded.Inventory2),
        InfoItem("Versión Ruta", formatRouteName(ctx?.routeName) ?: part?.activeRouteId?.toString() ?: "Pendiente", Icons.Rounded.Route)
    )

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = taskType.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                Text(text = "Información capturada desde API.", style = MaterialTheme.typography.bodyMedium, color = TTTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp, bottom = 22.dp))

                uiState.errorMessage?.let { Text(it, color = TTRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
            }

            GlassCard(
                modifier = Modifier
                    .align(Alignment.TopCenter) // 1. Cambiamos de TopEnd a TopCenter
                    .padding(top = 120.dp) // 2. Quitamos el 'end = 24.dp' para que no lo empuje a la izquierda
                    .fillMaxWidth(0.9f) // Esto asegura que ocupe el 90% de la pantalla y quede perfectamente centrado

            ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoGrid(items = infoItems)
                        ProductRouteDashboard(status = routeStatus)

                        when (taskType) {
                            TaskType.ProductAdvance -> {
                                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                                val glowAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.2f,
                                    targetValue = 0.8f,
                                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                                    label = "glowAlpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(2.dp, TTBlueDark.copy(alpha = glowAlpha), RoundedCornerShape(12.dp))
                                        .background(TTBlueDark.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(8.dp)
                                ) {
                                    TrackIITextField(
                                        label = "Piezas",
                                        value = uiState.qtyInput,
                                        onValueChange = vm::onQtyChange
                                    )
                                }
                                Text(
                                    text = "No mayor a piezas del paso anterior si aplica.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TTTextSecondary
                                )
                            }
                            TaskType.CancelOrder -> TrackIIDropdownField(
                                label = "Motivo de cancelación",
                                options = listOf("Error de calidad", "Material incorrecto", "Orden duplicada"),
                                helper = "Selecciona un motivo"
                            )
                            TaskType.Rework -> {
                                TrackIITextField(
                                    label = "Cantidad (Piezas)",
                                    value = uiState.qtyInput,
                                    onValueChange = vm::onQtyChange
                                )
                                TrackIIDropdownField(
                                    label = "Localidad de retrabajo",
                                    options = uiState.reworkLocations.map { it.name },
                                    helper = "Selecciona una localidad",
                                    selectedOption = uiState.selectedReworkLocation?.name.orEmpty(),
                                    onOptionSelected = vm::onReworkLocationSelected
                                )
                                TrackIITextField(
                                    label = "Motivo / Comentarios (Opcional)",
                                    value = uiState.reworkReason,
                                    onValueChange = vm::onReworkReasonChange
                                )
                                AnimatedVisibility(uiState.reworkLocations.isEmpty()) {
                                    Text(
                                        text = "No hay localidades disponibles para retrabajo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TTTextSecondary
                                    )
                                }
                            }
                            TaskType.TravelSheet -> Unit
                        }

                        PrimaryGlowButton(
                            text = if (uiState.isLoading) "Guardando..." else "Guardar",
                            onClick = {
                                vm.saveScan(
                                    taskType = taskType,
                                    workOrderNumber = lotNumber,
                                    partNumber = partNumber,
                                    userId = auth.userId,
                                    deviceId = auth.deviceId,
                                    locationName = auth.locationName
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading && !uiState.isSubmitting
                        )
                        SoftActionButton(text = "Volver", onClick = onBack, modifier = Modifier.fillMaxWidth())
                    }
                }

            FloatingHomeButton(onClick = onHome, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))

            if (uiState.saveSuccess) {
                SuccessOverlay(message = "¡Registro completado exitosamente!")
            }
        }
    }
}

@Composable
private fun ProductRouteDashboard(status: ProductRouteStatus) {
    val breath = rememberInfiniteTransition(label = "breath")
    val scale = breath.animateFloat(initialValue = 1f,
        targetValue = 1.50f,
        animationSpec = infiniteRepeatable(animation = tween(900),
            repeatMode = RepeatMode.Reverse),
        label = "breathScale").value

    Column(
        modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(TTBlueTint.copy(alpha = 0.5f), Color.White)), shape = RoundedCornerShape(20.dp)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (status.isStarted || status.isEligible) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (status.isEligible) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                    contentDescription = null,
                    tint = if (status.isEligible) TTGreen else TTRed,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (status.isEligible) "Producto en ruta correcta" else "Fuera de ruta / Acción inválida",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (status.isEligible) TTGreen else TTRed
                )
            }
        }

        val showNewOrder = !status.isStarted

        // Textos superiores de la ruta
        if (showNewOrder) {
            Text("Esta orden se abrirá por primera vez", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = TTTextSecondary, textAlign = TextAlign.Center)
        } else {
            androidx.compose.material3.Card(shape = RoundedCornerShape(12.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = TTBlue.copy(alpha = 0.1f))) {
                Text(
                    text = "Localidad actual: ${status.currentLocationName}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TTBlueDark,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Asignación inteligente de variables según si empezó o no
        val node1Label = if (showNewOrder) "Localidad inicial" else "Localidad previa"
        val node1Value = if (showNewOrder) status.currentLocationName else status.previousLocationName
        val node1Current = showNewOrder

        val node2Label = if (showNewOrder) "Localidad destino" else "Siguiente localidad"
        val node2Value = status.nextLocationName
        val node2Current = !showNewOrder

        // Renderizado de la barra de progreso
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                // 1. Línea conectora base dibujada primero (atrás)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f) // Ocupa el 50% conectando ambos centros geométricos
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    if (node1Current) TTGreen.copy(alpha = 0.6f) else TTBlue.copy(alpha = 0.3f),
                                    if (node2Current) TTGreen.copy(alpha = 0.6f) else TTBlue.copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                // 2. Círculos dibujados sobre la línea
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        RouteCircle(value = node1Value, isCurrent = node1Current, scale = if (node1Current) scale else 0.85f)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        RouteCircle(value = node2Value, isCurrent = node2Current, scale = if (node2Current) scale else 0.85f)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Textos informativos alineados debidamente debajo de sus círculos
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(text = node1Label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TTTextSecondary, textAlign = TextAlign.Center)
                Text(text = node2Label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TTTextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}


@Composable
private fun RouteCircle(value: String, isCurrent: Boolean, scale: Float = 1f) {
    val size = if (isCurrent) 84.dp else 56.dp

    Box(contentAlignment = Alignment.Center) {
        // 1. Capa de Animación (Respiro) - Está al fondo de todo
        // Solo se muestra y anima si es el nodo actual
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale) // El respiro solo afecta a esta capa
                    .background(
                        color = TTGreen.copy(alpha = 0.25f),
                        shape = CircleShape
                    )
            )
        }

        // 2. Capa Sólida Principal - Está encima de la línea y de la animación
        // El color blanco sólido asegura que la línea conectora no se vea a través del círculo
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
                .border(
                    width = if (isCurrent) 2.dp else 1.dp,
                    color = if (isCurrent) TTGreen else TTBlue.copy(alpha = 0.3f),
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
    }
}
@Composable
private fun InfoGrid(items: List<InfoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    InfoTile(title = item.title, value = item.value, icon = item.icon, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoTile(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    androidx.compose.material3.Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.background(Brush.linearGradient(listOf(TTGreenTint, Color.White))).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = TTGreen, modifier = Modifier.size(18.dp))
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
            }
            Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}


private fun formatRouteName(rawRouteName: String?): String? {
    val routeName = rawRouteName?.trim().orEmpty()
    if (routeName.isBlank()) return null

    val looksLikeNumericId = routeName.matches(Regex("^\\d+$"))
    val looksLikeStepPlaceholder = routeName.matches(Regex("^paso\\s*\\d+$", RegexOption.IGNORE_CASE))

    return if (looksLikeNumericId || looksLikeStepPlaceholder) null else routeName
}
