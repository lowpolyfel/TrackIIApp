package com.ttelectronics.trackiiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
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
    lotNumber: String,
    partNumber: String,
    orderFound: Boolean,
    errorMessage: String,
    onConfirm: () -> Unit,
    onRescan: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ScanReviewViewModel = viewModel(factory = ScanReviewViewModelFactory(ServiceLocator.scannerRepository(context)))
    val uiState by viewModel.uiState.collectAsState()
    val session = ServiceLocator.authRepository(context).sessionSnapshot()
    val rightSound = rememberRawSoundPlayer("right")
    val wrongSound = rememberRawSoundPlayer("wrong")

    LaunchedEffect(partNumber, lotNumber, orderFound) {
        if (orderFound) vm.fetchOrderContext(partNumber, lotNumber, session.deviceId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            wrongSound.play()
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            rightSound.play()
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearMessages()
            onConfirm()
        }
    }

    val partInfo = uiState.partInfo

    TrackIIBackground(glowOffsetX = 10.dp, glowOffsetY = (-10).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
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
                    text = if (orderFound) "Confirma y registra desde esta pantalla." else errorMessage.ifBlank { "No se encontró la orden para la parte escaneada." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (orderFound) TTTextSecondary else TTRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.verticalGradient(listOf(if (orderFound) TTGreenTint else Color(0xFFFFE6E6), Color.White)))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ScanHighlightRow(label = "No. Lote", value = lotNumber, orderFound = orderFound)
                        ScanHighlightRow(label = "No. Parte", value = partNumber, orderFound = orderFound)
                        if (orderFound) {
                            InfoLine("Área", partInfo?.area ?: "Cargando...", Icons.Rounded.Factory)
                            InfoLine("Familia", partInfo?.family ?: "Cargando...", Icons.Rounded.Category)
                            InfoLine("Subfamilia", partInfo?.subfamily ?: "Cargando...", Icons.Rounded.Inventory2)
                            InfoLine("No. de ruta", partInfo?.routeNumber ?: "Cargando...", Icons.Rounded.Route)
                            InfoLine("Estado de ruta", uiState.stepInfoText, Icons.Rounded.Route)

                            OutlinedTextField(
                                value = uiState.quantityInput,
                                onValueChange = vm::updateQuantity,
                                label = { Text("Cantidad") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !uiState.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoftActionButton(text = "Escanear nuevamente", onClick = onRescan, modifier = Modifier.weight(1f))
                    if (orderFound) {
                        PrimaryGlowButton(
                            text = if (uiState.isLoading) "Registrando..." else "Registrar",
                            onClick = {
                                vm.registerScan(
                                    workOrderNumber = lotNumber,
                                    deviceId = session.deviceId,
                                    isAlloyTablet = session.deviceName.contains("alloy", ignoreCase = true)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading && uiState.quantityInput.isNotEmpty()
                        )
                    }
                }
            }

            FloatingHomeButton(onClick = onHome, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))
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
            Box(
                modifier = Modifier.size(56.dp).background(
                    brush = Brush.linearGradient(listOf(TTAccent, TTBlueDark)),
                    shape = RoundedCornerShape(16.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
                Text(
                    text = value.ifBlank { "Pendiente de escaneo" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (value.isBlank()) TTTextSecondary else TTBlueDark
                )
            }
            Icon(
                imageVector = if (orderFound) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                contentDescription = null,
                tint = if (orderFound) TTGreen else TTRed
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!orderFound) {
            Text(text = errorMessage.ifBlank { "Orden no encontrada" }, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRescan) { Text("Escanear nuevamente") }
            return@Column
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Orden de trabajo: $lotNumber")
        Text(text = "Número de Parte: ${uiState.scannedPartNumber}")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = uiState.stepInfoText, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.quantityInput,
            onValueChange = { viewModel.updateQuantity(it) },
            label = { Text("Cantidad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.registerScan(lotNumber, session.deviceId) },
            enabled = !uiState.isLoading && uiState.quantityInput.isNotEmpty()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRescan, enabled = !uiState.isLoading) { Text("Volver a escanear") }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onHome, enabled = !uiState.isLoading) { Text("Inicio") }
    }
}
