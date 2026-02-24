package com.ttelectronics.trackiiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
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
    val successSound = rememberRawSoundPlayer("right")
    val errorSound = rememberRawSoundPlayer("wrong")

    LaunchedEffect(partNumber, lotNumber, orderFound) {
        if (orderFound) {
            viewModel.fetchOrderContext(partNumber, lotNumber, session.deviceId)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            errorSound.play()
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            successSound.play()
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
            onConfirm()
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
