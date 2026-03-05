package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.SuccessOverlay
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIIDropdownField
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

// Modelos para la Línea de Tiempo
enum class StepState { DONE, CURRENT, PENDING }

data class TimelineStepData(
    val locationName: String,
    val statusText: String,
    val pieces: String,
    val scrap: String,
    val state: StepState
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
        nextLocationName = nextLoc
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
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp, bottom = 100.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoGrid(items = infoItems)

                    // 🔴 Lógica de Bloqueo de Estados
                    val isBlocked = ctx?.orderStatus == "Cancelled" || ctx?.wipStatus == "Hold" || ctx?.wipStatus == "Scrapped" || ctx?.orderStatus == "Finished" || ctx?.wipStatus == "Finished"

                    if (isBlocked && taskType != TaskType.TravelSheet) {
                        // Si está bloqueada y quieren avanzar/scrap, no los deja. Muestra el banner y botón de regreso.
                        OrderStatusBanner(ctx?.orderStatus, ctx?.wipStatus, ctx?.statusUpdatedAt, ctx?.currentStepName)
                        Spacer(modifier = Modifier.height(8.dp))
                        SoftActionButton(text = "Volver al Menú", onClick = onBack, modifier = Modifier.fillMaxWidth())

                    } else if (taskType == TaskType.TravelSheet) {
                        // MODO VISOR: Si está bloqueada, muestra el banner, pero TAMBIÉN permite ver el Timeline abajo
                        if (isBlocked) {
                            OrderStatusBanner(ctx?.orderStatus, ctx?.wipStatus, ctx?.statusUpdatedAt, ctx?.currentStepName)
                        }

                        // ========================================================
                        Text(
                            text = "Historial de Ruta",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TTBlueDark,
                            modifier = Modifier.padding(horizontal = 8.dp).padding(top = 8.dp) // <-- Encadenados
                        )

                        // Mapeamos directamente la respuesta de la base de datos (API) al componente visual
                        val timelineSteps = ctx?.timeline?.map { dto ->
                            TimelineStepData(
                                locationName = dto.locationName,
                                statusText = when (dto.state) {
                                    "DONE" -> "Completado"
                                    "CURRENT" -> "En proceso"
                                    else -> "Pendiente"
                                },
                                pieces = dto.pieces,
                                scrap = dto.scrap,
                                state = when (dto.state) {
                                    "DONE" -> StepState.DONE
                                    "CURRENT" -> StepState.CURRENT
                                    else -> StepState.PENDING
                                }
                            )
                        } ?: emptyList() // Si la API aún no carga o manda null, se muestra vacío
                        TravelSheetTimeline(steps = timelineSteps)

                        Spacer(modifier = Modifier.height(8.dp))
                        SoftActionButton(text = "Volver al Menú", onClick = onBack, modifier = Modifier.fillMaxWidth())

                    } else {
                        // ========================================================
                        // MODO OPERATIVO NORMAL (REGISTROS Y AVANCES)
                        // ========================================================
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
            }

            FloatingHomeButton(onClick = onHome, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))

            if (uiState.saveSuccess) {
                SuccessOverlay(message = "¡Registro completado exitosamente!")
            }
        }
    }
}

// =========================================================================================
// NUEVO COMPONENTE: LÍNEA DE TIEMPO (TIMELINE) DE HOJA VIAJERA
// =========================================================================================
@Composable
private fun TravelSheetTimeline(steps: List<TimelineStepData>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        steps.forEachIndexed { index, step ->
            val isFirst = index == 0
            val isLast = index == steps.size - 1

            // IntrinsicSize permite que la línea vertical mida exactamente lo que mide la tarjeta
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

                // --- LADO IZQUIERDO: CONECTORES Y CÍRCULOS ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight().width(32.dp)
                ) {
                    // Línea Superior
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(24.dp)
                            .background(if (isFirst) Color.Transparent else if (step.state == StepState.PENDING) Color.LightGray else TTBlue.copy(alpha = 0.5f))
                    )

                    // Círculo de Estado
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = when (step.state) {
                                    StepState.DONE -> TTGreen.copy(alpha = 0.2f)
                                    StepState.CURRENT -> TTBlue.copy(alpha = 0.2f)
                                    StepState.PENDING -> Color.White
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = when (step.state) {
                                    StepState.DONE -> TTGreen
                                    StepState.CURRENT -> TTBlue
                                    StepState.PENDING -> Color.LightGray
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.state == StepState.DONE) {
                            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = TTGreen, modifier = Modifier.size(16.dp))
                        } else if (step.state == StepState.CURRENT) {
                            // Círculo interno animado para la localidad actual
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulse by infiniteTransition.animateFloat(initialValue = 0.6f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse")
                            Box(modifier = Modifier.size(10.dp).scale(pulse).background(TTBlue, CircleShape))
                        }
                    }

                    // Línea Inferior (Toma todo el espacio sobrante hacia abajo)
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(if (isLast) Color.Transparent else if (step.state == StepState.DONE) TTBlue.copy(alpha = 0.5f) else Color.LightGray)
                    )
                }

// --- LADO DERECHO: TARJETA DE INFORMACIÓN ---
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp), // ⬅️ Esquinas mucho más redondeadas y modernas
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (step.state == StepState.CURRENT) 6.dp else 1.dp),
                    border = if (step.state == StepState.CURRENT) BorderStroke(2.dp, TTGreen.copy(alpha = 0.6f)) else BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                // ⬅️ Degradado verde hermoso solo para el paso actual
                                if (step.state == StepState.CURRENT)
                                    Brush.linearGradient(listOf(TTGreenTint, Color.White))
                                else
                                    Brush.linearGradient(listOf(Color.White, Color.White))
                            )
                            .padding(16.dp), // ⬅️ Un poco más de espacio interno para que no se vea apretado
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = step.locationName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                // El texto resalta en verde si es el actual
                                color = if (step.state == StepState.CURRENT) TTGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = step.statusText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (step.state == StepState.CURRENT) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = when (step.state) {
                                    StepState.DONE -> TTGreen
                                    StepState.CURRENT -> TTGreen
                                    StepState.PENDING -> Color.Gray
                                }
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Piezas procesadas", style = MaterialTheme.typography.labelSmall, color = TTTextSecondary)
                                Text(step.pieces, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Scrap", style = MaterialTheme.typography.labelSmall, color = TTTextSecondary)
                                Text(
                                    text = step.scrap,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (step.scrap != "0" && step.scrap != "-") TTRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================

@Composable
private fun ProductRouteDashboard(status: ProductRouteStatus) {
    val breath = rememberInfiniteTransition(label = "breath")
    val scale = breath.animateFloat(initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(animation = tween(1500),
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

        val node1Label = if (showNewOrder) "Localidad inicial" else "Localidad previa"
        val node1Value = if (showNewOrder) status.currentLocationName else status.previousLocationName
        val node1Current = showNewOrder

        val node2Label = if (showNewOrder) "Localidad destino" else "Siguiente localidad"
        val node2Value = status.nextLocationName
        val node2Current = !showNewOrder

        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
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
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .background(
                        color = TTGreen.copy(alpha = 0.25f),
                        shape = CircleShape
                    )
            )
        }

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

@Composable
private fun OrderStatusBanner(orderStatus: String?, wipStatus: String?, updatedAt: String?, stepName: String?) {
    val isScrapped = orderStatus == "Cancelled" || wipStatus == "Scrapped"
    val isHold = wipStatus == "Hold"
    val isFinished = orderStatus == "Finished" || wipStatus == "Finished"

    val statusColor = when {
        isScrapped -> TTRed
        isHold -> Color(0xFFFFB300) // Amarillo de Alerta
        isFinished -> TTGreen
        else -> TTBlue
    }

    val statusTitle = when {
        orderStatus == "Cancelled" -> "Orden Cancelada"
        wipStatus == "Scrapped" -> "Orden Rechazada (Scrap Total)"
        wipStatus == "Hold" -> "Orden en Retrabajo (Hold)"
        isFinished -> "Orden Finalizada"
        else -> "Estado Bloqueado"
    }

    val statusIcon = when {
        isScrapped -> Icons.Rounded.Error
        isHold -> Icons.Rounded.Warning
        isFinished -> Icons.Rounded.CheckCircle
        else -> Icons.Rounded.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
                Text(text = statusTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = statusColor)
            }
            Text(text = "Fecha de movimiento: ${updatedAt ?: "Sin registro"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Paso en el que se detuvo: ${stepName ?: "Desconocido"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}