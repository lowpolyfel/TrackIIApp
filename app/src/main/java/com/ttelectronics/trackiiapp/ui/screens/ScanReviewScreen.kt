package com.ttelectronics.trackiiapp.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCode
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ScanReviewViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ScanReviewViewModelFactory

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
    val context = LocalContext.current
    val vm: ScanReviewViewModel = viewModel(factory = ScanReviewViewModelFactory(ServiceLocator.scannerRepository(context)))
    val wrongSoundPlayer = rememberRawSoundPlayer("wrong")
    val uiState by vm.uiState.collectAsState()
    val session = ServiceLocator.authRepository(context).sessionSnapshot()

    LaunchedEffect(partNumber, lotNumber, orderFound) {
        if (orderFound) vm.loadData(partNumber, lotNumber, session.deviceId)
    }

    val partInfo = uiState.partInfo
    val workContext = uiState.contextInfo

    TrackIIBackground(glowOffsetX = 10.dp, glowOffsetY = (-10).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (orderFound) "Orden encontrada" else "Orden no encontrada",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = if (orderFound) MaterialTheme.colorScheme.onSurface else TTRed
                )
                Text(
                    text = when {
                        !orderFound -> errorMessage.ifBlank { "No se encontró la orden para la parte escaneada." }
                        workContext?.isNew == true -> "Orden sin comenzar"
                        else -> "Confirma los datos antes de continuar."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (orderFound) TTTextSecondary else TTRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.background(Brush.verticalGradient(listOf(if (orderFound) TTGreenTint else Color(0xFFFFE6E6), Color.White))).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ScanHighlightRow(label = "No. Lote", value = lotNumber, orderFound = orderFound)
                        ScanHighlightRow(label = "No. Parte", value = partNumber, orderFound = orderFound)

                        if (orderFound) {
                            if (uiState.isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    androidx.compose.material3.CircularProgressIndicator(color = TTBlueDark)
                                }
                            } else {
                                InfoLine("Área", partInfo?.areaName ?: "Sin área", Icons.Rounded.Factory)
                                InfoLine("Familia", partInfo?.familyName ?: "Sin familia", Icons.Rounded.Category)
                                InfoLine("Subfamilia", partInfo?.subfamilyName ?: "Sin subfamilia", Icons.Rounded.Inventory2)
                                InfoLine("Versión de ruta", workContext?.routeName ?: partInfo?.activeRouteId?.toString() ?: "Sin ruta activa", Icons.Rounded.Route)

                                if (workContext?.isNew == true) {
                                    InfoLine("Ruta actual", "Orden no empezada", Icons.Rounded.Route)
                                    InfoLine("Siguiente localidad", workContext.currentStepName ?: "Paso 1", Icons.Rounded.Route)
                                } else {
                                    InfoLine("Localidad actual", workContext?.currentStepName ?: "Desconocida", Icons.Rounded.Route)
                                    InfoLine("Siguiente localidad", workContext?.nextSteps?.firstOrNull()?.locationName ?: "Fin de ruta", Icons.Rounded.Route)
                                }
                            }
                        }
                        uiState.errorMessage?.let { Text(it, color = TTRed, style = MaterialTheme.typography.bodySmall) }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SoftActionButton(text = "Escanear nuevamente", onClick = onRescan, modifier = Modifier.weight(1f))
                    if (orderFound) {
                        SoftActionButton(
                            text = "Continuar",
                            onClick = {
                                if (taskType == TaskType.CancelOrder && workContext?.isNew == true) {
                                    wrongSoundPlayer.play()
                                    android.widget.Toast.makeText(
                                        context,
                                        "Esta orden no puede cancelarse porque aún no empieza.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    onConfirm()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            FloatingHomeButton(onClick = onHome, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))
        }
    }
}

@Composable
private fun ScanHighlightRow(label: String, value: String, orderFound: Boolean) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.size(56.dp).background(brush = Brush.linearGradient(listOf(TTAccent, TTBlueDark)), shape = RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Rounded.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
                Text(text = value.ifBlank { "Pendiente de escaneo" }, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (value.isBlank()) TTTextSecondary else TTBlueDark)
            }
            Icon(imageVector = if (orderFound) Icons.Rounded.CheckCircle else Icons.Rounded.Error, contentDescription = null, tint = if (orderFound) TTGreen else TTRed)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String, icon: ImageVector) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = TTBlueDark)
            Text(label, color = TTTextSecondary, modifier = Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}
