package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.core.isNetworkAvailable
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay
import com.ttelectronics.trackiiapp.ui.components.SuccessOverlay
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ProductAdvanceFinalReviewViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ProductAdvanceFinalReviewViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun ProductAdvanceFinalReviewScreen(
    lotNumber: String,
    partNumber: String,
    qtyIn: Int,
    scrap: Int,
    errorCodeId: Int,
    errorCodeName: String,
    comments: String,
    onCancel: () -> Unit,
    onEditPieces: () -> Unit,
    onEditScrap: () -> Unit,
    onComplete: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val vm: ProductAdvanceFinalReviewViewModel = viewModel(
        factory = ProductAdvanceFinalReviewViewModelFactory(ServiceLocator.scannerRepository(context), AppSession(context))
    )
    val uiState by vm.uiState.collectAsState()

    // Estado para la validación de red
    var showNetworkError by remember { mutableStateOf(false) }

    LaunchedEffect(partNumber) { vm.loadPartInfo(partNumber) }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(1200)
            onComplete()
        }
    }

    if (showNetworkError) {
        LaunchedEffect(Unit) {
            delay(2500)
            onHome()
        }
    }

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.align(Alignment.Center).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                // Título movido justo arriba de la tarjeta
                Text(
                    text = "Confirmación de registro",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TTBlueDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth(0.9f).zIndex(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {

                        // 1. Grid Limpio
                        ReviewInfoGrid(
                            items = listOf(
                                ReviewInfoItem("No. Lote", lotNumber, Icons.Rounded.Inventory2),
                                ReviewInfoItem("No. Parte", partNumber, Icons.Rounded.QrCode)
                            )
                        )

                        // 2. Tarjetas Limpias para Piezas y Scrap
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TTBlueTint),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Piezas a avanzar", style = MaterialTheme.typography.labelMedium, color = TTTextSecondary)
                                    Text("$qtyIn", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = TTBlueDark)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, if(scrap > 0) TTRed.copy(alpha=0.3f) else TTGreen.copy(alpha=0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Scrap reportado", style = MaterialTheme.typography.labelMedium, color = TTTextSecondary)
                                    Text("$scrap", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = if(scrap > 0) TTRed else TTGreen)
                                }
                            }
                        }

                        // 3. Detalles del Scrap (Discreto)
                        if (scrap > 0) {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, TTRed.copy(alpha = 0.2f))) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "Motivo de rechazo: $errorCodeName", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = TTRed)
                                    if (comments.isNotBlank()) { Text(text = "Notas: $comments", style = MaterialTheme.typography.bodySmall, color = TTTextSecondary) }
                                }
                            }
                        }

                        // 4. Pregunta de confirmación SIN cuadro naranja gigante
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = TTBlue, modifier = Modifier.size(20.dp))
                            Text(
                                text = " ¿Estás seguro de registrar esta información?",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TTBlueDark
                            )
                        }

                        uiState.errorMessage?.let {
                            Text(text = it, color = TTRed, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }

                        // 5. Botones de Acción MÁS PEQUEÑOS y en fila
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            androidx.compose.material3.Button(
                                onClick = onCancel,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = TTRed.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) { Text("Cancelar", fontWeight = FontWeight.Bold) }

                            FinalReviewGreenButton(
                                text = if (uiState.isSubmitting) "Guardando..." else "Registrar",
                                onClick = {
                                    if (!isNetworkAvailable(context)) {
                                        showNetworkError = true
                                    } else {
                                        vm.submitAll(lotNumber = lotNumber, partNumber = partNumber, qtyIn = qtyIn, scrap = scrap, errorCodeId = errorCodeId, comments = comments)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSubmitting
                            )
                        }

                        // 6. Sección de Edición (Condicional)
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "Modificar Captura", style = MaterialTheme.typography.labelSmall, color = TTTextSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = onEditPieces,
                                border = BorderStroke(1.dp, TTBlue),
                                shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).height(44.dp)
                            ) { Text("Editar Piezas", fontWeight = FontWeight.SemiBold, color = TTBlue) }

                            // CONDICIONAL: Solo mostrar editar scrap si realmente hay scrap
                            if (scrap > 0) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = onEditScrap,
                                    border = BorderStroke(1.dp, TTRed.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).height(44.dp)
                                ) { Text("Editar Scrap", fontWeight = FontWeight.SemiBold, color = TTRed.copy(alpha = 0.8f)) }
                            }
                        }
                    }
                }
            }

            FloatingHomeButton(onClick = onHome, modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 20.dp).scale(0.8f))

            if (uiState.isSuccess) {
                Box(modifier = Modifier.fillMaxSize().zIndex(10f)) { SuccessOverlay(message = "¡Registro completado exitosamente!") }
            }

            if (showNetworkError) {
                Box(modifier = Modifier.fillMaxSize().zIndex(20f)) {
                    ScanResultOverlay(
                        visible = true,
                        success = false,
                        message = "Error de conexión a internet.\nRegresando al menú principal..."
                    )
                }
            }
        }
    }
}

private data class ReviewInfoItem(val title: String, val value: String, val icon: ImageVector)

@Composable
private fun ReviewInfoGrid(items: List<ReviewInfoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = TTBlueTint.copy(alpha = 0.3f))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = item.icon, contentDescription = null, tint = TTBlue, modifier = Modifier.size(18.dp))
                                Text(text = item.title, style = MaterialTheme.typography.labelMedium, color = TTTextSecondary)
                            }
                            Text(text = item.value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TTBlueDark)
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FinalReviewGreenButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "btnGlow")
    val pulseAlpha by transition.animateFloat(initialValue = 0.1f, targetValue = 0.3f, animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "pAlpha")
    val gradient = Brush.linearGradient(colors = listOf(Color(0xFF00C853), Color(0xFF64DD17), Color(0xFF00C853)), start = Offset(0f, 0f), end = Offset(400f, 0f))
    Box(modifier = modifier) {
        Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(14.dp)).background(Color(0xFF64DD17).copy(alpha = pulseAlpha)))
        androidx.compose.material3.Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(48.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(if(enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray))), contentAlignment = Alignment.Center) {
                Text(text = text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}