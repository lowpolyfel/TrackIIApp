package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.delay

data class InfoItem(val title: String, val value: String, val icon: ImageVector)

data class ProductRouteStatus(
    val isStarted: Boolean,
    val isEligible: Boolean,
    val currentLocationName: String,
    val previousLocationName: String,
    val nextLocationName: String
)

enum class StepState { DONE, CURRENT, PENDING, CANCELLED }

data class TimelineStepData(
    val locationName: String,
    val statusText: String,
    val pieces: String,
    val scrap: String,
    val state: StepState,
    val errorCode: String? = null,
    val comments: String? = null
)

@Composable
fun TaskDetailScreen(
    taskType: TaskType,
    lotNumber: String,
    partNumber: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onNavigateToPartialScrap: (Int, Int) -> Unit,
    onNavigateToFinalReview: (Int, Int) -> Unit,
    initialQty: String = "",
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val rightSoundPlayer = rememberRawSoundPlayer("right")
    val authRepository = ServiceLocator.authRepository(context)
    val auth = authRepository.sessionSnapshot()
    val vm: TaskDetailViewModel = viewModel(factory = TaskDetailViewModelFactory(ServiceLocator.scannerRepository(context), authRepository))
    val uiState by vm.uiState.collectAsState()

    var manualQtyInput by remember { mutableStateOf(false) }

    LaunchedEffect(partNumber, lotNumber, auth.deviceId) { vm.loadData(partNumber, lotNumber, auth.deviceId) }
    LaunchedEffect(initialQty) { vm.setInitialQtyInput(initialQty) }
    LaunchedEffect(uiState.contextInfo?.previousQuantity, taskType) {
        if (taskType == TaskType.ProductAdvance) vm.ensureDefaultQtyFromPrevious()
    }

    LaunchedEffect(uiState.pendingReady, uiState.piecesDifference, uiState.pendingQtyIn) {
        if (taskType == TaskType.ProductAdvance && uiState.pendingReady) {
            if (uiState.piecesDifference > 0) onNavigateToPartialScrap(uiState.piecesDifference, uiState.pendingQtyIn)
            else onNavigateToFinalReview(uiState.pendingQtyIn, 0)
            vm.clearPendingRegistration()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess && taskType != TaskType.ProductAdvance) {
            rightSoundPlayer.play()
            kotlinx.coroutines.delay(1800)
            onComplete()
        }
    }

    val isDataReady = !uiState.isLoading
    val part = uiState.partInfo
    val ctx = if (isDataReady) uiState.contextInfo else null
    val userLocation = auth.locationName.trim()

    val isEligible = if (ctx?.isNew == true) ctx.currentStepName?.trim()?.equals(userLocation, ignoreCase = true) ?: false else ctx?.nextSteps?.firstOrNull()?.locationName?.trim()?.equals(userLocation, ignoreCase = true) ?: false
    val nextLoc = if (ctx?.isNew == true) ctx.nextSteps?.firstOrNull { it.stepNumber == 2 }?.locationName ?: "Paso 2" else ctx?.nextSteps?.firstOrNull()?.locationName ?: "Fin de ruta"

    val routeStatus = ProductRouteStatus(
        isStarted = ctx?.isNew == false, isEligible = isEligible,
        currentLocationName = ctx?.currentStepName ?: "Localidad desconocida",
        previousLocationName = ctx?.currentStepName ?: "Sin localidad previa", nextLocationName = nextLoc
    )

    // Calculamos el timeline aquí arriba para poder compartirlo con el dashboard
    val timelineSteps = ctx?.timeline?.map { dto ->
        TimelineStepData(
            locationName = dto.locationName,
            statusText = when (dto.state) { "DONE" -> "Completado" "CURRENT" -> "En proceso" "CANCELLED" -> "Cancelada / Scrap" else -> "Pendiente" },
            pieces = dto.pieces,
            scrap = dto.scrap,
            errorCode = dto.errorCode,
            comments = dto.comments,
            state = when (dto.state) { "DONE" -> StepState.DONE "CURRENT" -> StepState.CURRENT "CANCELLED" -> StepState.CANCELLED else -> StepState.PENDING }
        )
    } ?: emptyList()

    // Mostrar siempre únicamente Lote y Número de Parte para ahorrar espacio
    val infoItems = listOf(
        InfoItem("No. Lote", lotNumber, Icons.Rounded.Inventory2),
        InfoItem("No. Parte", partNumber, Icons.Rounded.QrCode)
    )

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = taskType.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                Text(text = "Información capturada desde API.", style = MaterialTheme.typography.bodyMedium, color = TTTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp, bottom = 22.dp))
                uiState.errorMessage?.let { Text(it, color = TTRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
            }

            GlassCard(modifier = Modifier.align(Alignment.TopCenter).padding(top = 120.dp, bottom = 100.dp).fillMaxWidth(0.9f)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoGrid(items = infoItems)

                    val orderStatus = ctx?.orderStatus.orEmpty()
                    val wipStatus = ctx?.wipStatus.orEmpty()
                    val isBlocked = orderStatus.equals("Cancelled", ignoreCase = true) || wipStatus.equals("Hold", ignoreCase = true) || wipStatus.equals("Scrapped", ignoreCase = true) || orderStatus.equals("Finished", ignoreCase = true) || wipStatus.equals("Finished", ignoreCase = true)

                    if (isBlocked && taskType != TaskType.TravelSheet) {
                        OrderStatusBanner(orderStatus, wipStatus, ctx?.statusUpdatedAt, ctx?.currentStepName)
                        Spacer(modifier = Modifier.height(8.dp))

                    } else if (taskType == TaskType.TravelSheet) {
                        if (isBlocked) OrderStatusBanner(orderStatus, wipStatus, ctx?.statusUpdatedAt, ctx?.currentStepName)

                        Text(text = "Historial de Ruta", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TTBlueDark, modifier = Modifier.padding(horizontal = 8.dp).padding(top = 8.dp))

                        SnakeRouteTimeline(steps = timelineSteps)
                        Spacer(modifier = Modifier.height(8.dp))

                    } else {
                        ProductRouteDashboard(status = routeStatus, taskType = taskType, timelineSteps = timelineSteps)

                        when (taskType) {
                            TaskType.ProductAdvance -> {
                                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                                val glowAlpha by infiniteTransition.animateFloat(initialValue = 0.2f, targetValue = 0.8f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "glowAlpha")

                                // NUEVO LIMITE: 20000 para nuevas, y cantidad previa para las demás
                                val maxQty = if (uiState.contextInfo?.isNew == true) 20000 else (uiState.contextInfo?.previousQuantity ?: 0).coerceAtLeast(0)
                                val focusManager = LocalFocusManager.current // Herramienta para ocultar teclado

                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).border(2.dp, TTBlueDark.copy(alpha = glowAlpha), RoundedCornerShape(14.dp)).background(TTBlueDark.copy(alpha = 0.05f), RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 14.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Piezas", style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
                                            TextButton(onClick = {
                                                manualQtyInput = !manualQtyInput
                                            }) {
                                                Text(text = if (manualQtyInput) "Usar slider" else "Ingresar manual")
                                            }
                                        }

                                        if (manualQtyInput) {
                                            androidx.compose.material3.TextField(
                                                value = uiState.qtyInput,
                                                onValueChange = vm::onProductAdvanceQtyChange,
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                label = { Text(text = "Piezas") },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Done // Tecla de Enter/Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        focusManager.clearFocus() // Oculta el teclado
                                                        vm.validateManualQty()    // Evalúa si se pasó
                                                    }
                                                ),
                                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                                    focusedContainerColor = TTBlueTint,
                                                    unfocusedContainerColor = TTBlueTint,
                                                    disabledContainerColor = TTBlueTint,
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                ),
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                        } else {
                                            val sliderRangeMax = maxQty.toFloat().coerceAtLeast(1f)
                                            val sliderValue = (uiState.qtyInput.toFloatOrNull() ?: maxQty.toFloat()).coerceIn(0f, sliderRangeMax)

                                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = uiState.qtyInput.ifBlank { "0" },
                                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TTBlueDark,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                                    val controlWidth = maxWidth * 0.18f
                                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                        HoldableActionButton(text = "-", onAction = { delta -> vm.adjustProductAdvanceQty(delta) }, modifier = Modifier.width(controlWidth), isIncrement = false)
                                                        Slider(
                                                            value = sliderValue,
                                                            onValueChange = vm::onProductAdvanceSliderChange,
                                                            valueRange = 0f..sliderRangeMax,
                                                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                                                        )
                                                        HoldableActionButton(text = "+", onAction = { delta -> vm.adjustProductAdvanceQty(delta) }, modifier = Modifier.width(controlWidth), isIncrement = true)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Text(text = "Máximo permitido: $maxQty piezas.", style = MaterialTheme.typography.bodySmall, color = TTTextSecondary)
                            }
                            TaskType.CancelOrder -> TrackIIDropdownField(label = "Motivo de cancelación", options = listOf("Error de calidad", "Material incorrecto", "Orden duplicada"), helper = "Selecciona un motivo")
                            TaskType.Rework -> {
                                TrackIITextField(label = "Cantidad (Piezas)", value = uiState.qtyInput, onValueChange = vm::onQtyChange)
                                TrackIIDropdownField(label = "Localidad de retrabajo", options = uiState.reworkLocations.map { it.name }, helper = "Selecciona una localidad", selectedOption = uiState.selectedReworkLocation?.name.orEmpty(), onOptionSelected = vm::onReworkLocationSelected)
                                TrackIITextField(label = "Motivo / Comentarios (Opcional)", value = uiState.reworkReason, onValueChange = vm::onReworkReasonChange)
                                AnimatedVisibility(uiState.reworkLocations.isEmpty()) { Text(text = "No hay localidades disponibles para retrabajo.", style = MaterialTheme.typography.bodySmall, color = TTTextSecondary) }
                            }
                            TaskType.TravelSheet -> Unit
                        }

                        GreenGlowButton(text = if (uiState.isLoading) "Guardando..." else "Guardar", onClick = {
                            if (taskType == TaskType.ProductAdvance) {
                                vm.prepareProductAdvanceRegistration(workOrderNumber = lotNumber, locationName = auth.locationName)
                            } else {
                                vm.saveScan(taskType = taskType, workOrderNumber = lotNumber, partNumber = partNumber, userId = auth.userId, deviceId = auth.deviceId, locationName = auth.locationName)
                            }
                        }, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading && !uiState.isSubmitting)
                    }
                }
            }

// Botón de Inicio reubicado arriba a la derecha, más discreto
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 20.dp).scale(0.8f)
            )

            // Nuevo botón de volver global, circular e inferior izquierdo
            androidx.compose.material3.FloatingActionButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.BottomStart).padding(24.dp).size(64.dp),
                containerColor = TTBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Volver", modifier = Modifier.size(32.dp))
            }

            if (uiState.saveSuccess) { Box(modifier = Modifier.fillMaxSize().zIndex(10f)) { SuccessOverlay(message = "¡Registro completado exitosamente!") } }
            // NUEVO: Pantalla Roja en caso de equivocarse escribiendo
            if (uiState.showQtyErrorOverlay) {
                Box(modifier = Modifier.fillMaxSize().zIndex(15f)) {
                    com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay(
                        visible = true,
                        success = false,
                        message = uiState.qtyErrorText
                    )
                }
                LaunchedEffect(uiState.showQtyErrorOverlay) {
                    delay(3500) // Desaparece solita después de 3.5 segundos
                    vm.dismissQtyError()
                }
            }
        }
    }
}


// =========================================================================================

@Composable
private fun SnakeRouteTimeline(steps: List<TimelineStepData>) {
    val columns = 3
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        // Líneas de fondo dibujadas con Canvas para hacer el efecto serpiente (S-shape)
        Canvas(modifier = Modifier.matchParentSize()) {
            val xStep = size.width / columns
            val yStep = 90.dp.toPx() // Altura de cada fila

            for (i in 0 until steps.size - 1) {
                val startRow = i / columns
                val startCol = if (startRow % 2 == 0) i % columns else columns - 1 - (i % columns)
                val endRow = (i + 1) / columns
                val endCol = if (endRow % 2 == 0) (i + 1) % columns else columns - 1 - ((i + 1) % columns)

                val startX = (startCol + 0.5f) * xStep
                val startY = (startRow + 0.5f) * yStep
                val endX = (endCol + 0.5f) * xStep
                val endY = (endRow + 0.5f) * yStep

                val lineColor = if (steps[i].state == StepState.DONE || steps[i].state == StepState.CURRENT)
                    TTGreen else TTBlue.copy(alpha = 0.3f)

                drawLine(
                    color = lineColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Nodos encima de las líneas
        Column(modifier = Modifier.fillMaxWidth()) {
            val chunked = steps.chunked(columns)
            chunked.forEachIndexed { rowIndex, chunk ->
                val isEven = rowIndex % 2 == 0
                Row(
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = if (isEven) chunk else chunk.reversed()
                    if (!isEven && chunk.size < columns) {
                        repeat(columns - chunk.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                    items.forEach { step ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SnakeRouteCircle(step = step)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = step.locationName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TTBlueDark,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                    if (isEven && chunk.size < columns) {
                        repeat(columns - chunk.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnakeRouteCircle(step: TimelineStepData) {
    val size = 46.dp
    val color = when (step.state) {
        StepState.DONE -> TTGreen
        StepState.CURRENT -> TTBlue
        StepState.CANCELLED -> TTRed
        StepState.PENDING -> Color.LightGray
    }
    val pulse = if (step.state == StepState.CURRENT) {
        val transition = rememberInfiniteTransition(label = "pulse")
        transition.animateFloat(initialValue = 1f, targetValue = 1.15f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse").value
    } else 1f

    Box(
        modifier = Modifier
            .size(size)
            .scale(pulse)
            .background(Color.White, CircleShape)
            .border(3.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (step.state == StepState.DONE) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = TTGreen, modifier = Modifier.size(28.dp))
        } else if (step.state == StepState.CANCELLED) {
            Icon(Icons.Rounded.Cancel, contentDescription = null, tint = TTRed, modifier = Modifier.size(28.dp))
        } else {
            Text(text = step.pieces.ifBlank { "0" }, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

@Composable
private fun OrderStatusBanner(orderStatus: String, wipStatus: String, updatedAt: String?, stepName: String?) {
    val isScrapped = orderStatus.equals("Cancelled", true) || wipStatus.equals("Scrapped", true)
    val isHold = wipStatus.equals("Hold", true)
    val isFinished = orderStatus.equals("Finished", true) || wipStatus.equals("Finished", true)

    val statusColor = when { isScrapped -> TTRed; isHold -> Color(0xFFFFB300); isFinished -> TTGreen; else -> TTBlue }
    val statusTitle = when { orderStatus.equals("Cancelled", true) -> "Orden Cancelada"; wipStatus.equals("Scrapped", true) -> "Orden Rechazada (Scrap Total)"; wipStatus.equals("Hold", true) -> "Orden en Retrabajo (Hold)"; isFinished -> "Orden Finalizada"; else -> "Estado Bloqueado" }
    val statusIcon = when { isScrapped -> Icons.Rounded.Error; isHold -> Icons.Rounded.Warning; isFinished -> Icons.Rounded.CheckCircle; else -> Icons.Rounded.Info }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)), border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))) {
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

@Composable
private fun ProductRouteDashboard(status: ProductRouteStatus, taskType: TaskType, timelineSteps: List<TimelineStepData> = emptyList()) {
    val breath = rememberInfiniteTransition(label = "breath")
    val scale = breath.animateFloat(initialValue = 1f, targetValue = 1.15f, animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse), label = "breathScale").value
    var showFullRoute by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(TTBlueTint.copy(alpha = 0.5f), Color.White)), shape = RoundedCornerShape(20.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {

        AnimatedVisibility(
            visible = showFullRoute,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ruta Completa", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TTBlueDark)
                    TextButton(onClick = { showFullRoute = false }) { Text("Ocultar") }
                }
                SnakeRouteTimeline(steps = timelineSteps)
            }
        }

        if (!showFullRoute) {
            if ((status.isStarted || status.isEligible) && taskType != TaskType.Rework) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = if (status.isEligible) Icons.Rounded.CheckCircle else Icons.Rounded.Error, contentDescription = null, tint = if (status.isEligible) TTGreen else TTRed, modifier = Modifier.size(22.dp))
                    Text(text = if (status.isEligible) "Producto en ruta correcta" else "Fuera de ruta / Acción inválida", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (status.isEligible) TTGreen else TTRed)
                }
            }

            val showNewOrder = !status.isStarted
            if (showNewOrder) {
                Text("Esta orden se abrirá por primera vez", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = TTTextSecondary, textAlign = TextAlign.Center)
            } else {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = TTBlue.copy(alpha = 0.1f))) {
                    Text(text = "Localidad actual: ${status.currentLocationName}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = TTBlueDark, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }

            val prevLoc = if (showNewOrder) "Inicio" else status.previousLocationName
            val currLoc = status.currentLocationName
            val nextLoc = status.nextLocationName

            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.fillMaxWidth(0.66f).height(4.dp).background(brush = Brush.horizontalGradient(colors = listOf(TTBlue.copy(alpha = 0.3f), TTGreen.copy(alpha = 0.6f), TTBlue.copy(alpha = 0.3f))), shape = RoundedCornerShape(2.dp)))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { RouteCircle(value = prevLoc, isCurrent = false, scale = 0.85f) }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { RouteCircle(value = currLoc, isCurrent = true, scale = scale) }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { RouteCircle(value = nextLoc, isCurrent = false, scale = 0.85f) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(text = "Anterior", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TTTextSecondary, textAlign = TextAlign.Center)
                    Text(text = "Actual", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TTTextSecondary, textAlign = TextAlign.Center)
                    Text(text = "Siguiente", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = TTTextSecondary, textAlign = TextAlign.Center)
                }
            }

            if (timelineSteps.isNotEmpty() && taskType != TaskType.TravelSheet) {
                TextButton(onClick = { showFullRoute = true }) {
                    Text("Ver ruta completa", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun RouteCircle(value: String, isCurrent: Boolean, scale: Float = 1f) {
    val size = if (isCurrent) 84.dp else 56.dp
    Box(contentAlignment = Alignment.Center) {
        if (isCurrent) { Box(modifier = Modifier.size(size).scale(scale).background(color = TTGreen.copy(alpha = 0.25f), shape = CircleShape)) }
        Box(modifier = Modifier.size(size).background(color = Color.White, shape = CircleShape).border(width = if (isCurrent) 2.dp else 1.dp, color = if (isCurrent) TTGreen else TTBlue.copy(alpha = 0.3f), shape = CircleShape), contentAlignment = Alignment.Center) {
            Text(text = value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (isCurrent) TTGreen else TTBlue, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

@Composable
private fun InfoGrid(items: List<InfoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item -> InfoTile(title = item.title, value = item.value, icon = item.icon, modifier = Modifier.weight(1f)) }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoTile(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(
            modifier = Modifier.background(Brush.linearGradient(listOf(TTGreenTint, Color.White))).padding(14.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = TTGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
            }
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TTBlueDark, textAlign = TextAlign.Center)
        }
    }
}

private fun formatRouteName(rawRouteName: String?): String? {
    val routeName = rawRouteName?.trim().orEmpty()
    if (routeName.isBlank()) return null
    return if (routeName.matches(Regex("^\\d+$")) || routeName.matches(Regex("^paso\\s*\\d+$", RegexOption.IGNORE_CASE))) null else routeName
}
@Composable
fun HoldableActionButton(
    text: String,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isIncrement: Boolean
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val baseDelta = if (isIncrement) 1 else -1

    Box(
        modifier = modifier
            .height(48.dp) // Le damos la altura estándar de un botón
            .clip(RoundedCornerShape(12.dp))
            .background(com.ttelectronics.trackiiapp.ui.theme.TTBlue)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val job = coroutineScope.launch {
                            var elapsedMs = 0L
                            val repeatDelay = 150L // Velocidad ultra fluida

                            while(true) {
                                if (elapsedMs == 0L) {
                                    onAction(baseDelta * 1)
                                    kotlinx.coroutines.delay(500)
                                    elapsedMs += 500
                                } else {
                                    val currentMultiplier = when {
                                        elapsedMs >= 3000L -> 200 // Más de 3s: 200 en 200
                                        elapsedMs >= 2000L -> 100 // Más de 2s: 100 en 100
                                        elapsedMs >= 1000L -> 10  // Más de 1s: 10 en 10
                                        else -> 1                 // 0.5 a 1s: 1 en 1
                                    }
                                    onAction(baseDelta * currentMultiplier)
                                    kotlinx.coroutines.delay(repeatDelay)
                                    elapsedMs += repeatDelay
                                }
                            }
                        }
                        // tryAwaitRelease() espera pacientemente a que levantes el dedo sin conflictos
                        tryAwaitRelease()
                        job.cancel() // En cuanto levantas el dedo, detiene el contador al instante
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
fun GreenGlowButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "buttonGlow")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.15f, targetValue = 0.45f, animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pulseAlpha"
    )
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF00C853), Color(0xFF64DD17), Color(0xFF00C853)),
        start = Offset(0f, 0f), end = Offset(400f, 0f)
    )
    Box(modifier = modifier) {
        Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(20.dp)).background(Color(0xFF64DD17).copy(alpha = pulseAlpha)))
        androidx.compose.material3.Button(
            onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(if(enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray))),
                contentAlignment = Alignment.Center
            ) { Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White) }
        }
    }
}