@file:OptIn(ExperimentalMaterial3Api::class)

package com.ttelectronics.trackiiapp.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerHeader
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIIDropdownField
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ScrapOrderViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ScrapOrderViewModelFactory

@Composable
fun ScrapOrderScreen(
    lotNumber: String,
    partNumber: String,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: ScrapOrderViewModel = viewModel(
        factory = ScrapOrderViewModelFactory(
            ServiceLocator.scannerRepository(context),
            AppSession(context)
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val rightSoundPlayer = rememberRawSoundPlayer("right")

    var showSuccessOverlay by remember { mutableStateOf(false) }
    // 🔥 Variable para mostrar el diálogo intermedio
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lotNumber, partNumber) {
        viewModel.initialize(lotNumber, partNumber)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            showSuccessOverlay = true
            rightSoundPlayer.play()
            Toast.makeText(context, "Cancelación registrada", Toast.LENGTH_LONG).show()
            kotlinx.coroutines.delay(1200)
            onComplete()
        }
    }

    val infoItems = listOf(
        ScrapInfoItem("No. Lote", uiState.lotNumber, Icons.Rounded.Inventory2),
        ScrapInfoItem("No. Parte", uiState.partNumber, Icons.Rounded.QrCode)
    )

    // 🔥 PANTALLA INTERMEDIA (DIÁLOGO DE CONFIRMACIÓN)
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Está seguro?", fontWeight = FontWeight.Bold) },
            text = { Text("Una vez cancelada la orden, no volverá a estar activa y se registrará como scrap total.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    keyboardController?.hide()
                    viewModel.submit() // Se guarda hasta que confirman aquí
                }) {
                    Text("Sí, Cancelar", color = TTRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Volver", color = TTTextSecondary)
                }
            },
            containerColor = Color.White,
            titleContentColor = TTRed,
            textContentColor = TTTextSecondary
        )
    }

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Header
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScannerHeader(taskTitle = "Cancelar Orden")
            }

            // Contenedor Central
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Proceso de cancelación y registro de scrap.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .zIndex(1f) // Esto causaba el solapamiento, pero ya lo arreglamos abajo
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        ScrapInfoGrid(items = infoItems)

                        TrackIITextField(
                            label = "Piezas a cancelar",
                            value = uiState.qtyInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() }) viewModel.onQtyChange(newValue)
                            }
                        )

                        TrackIIDropdownField(
                            label = "Categoría de falla",
                            options = uiState.categories.map { it.name },
                            helper = "Selecciona una categoría",
                            selectedOption = uiState.selectedCategory?.name.orEmpty(),
                            onOptionSelected = { selectedName ->
                                val cat = uiState.categories.find { it.name == selectedName }
                                if (cat != null) viewModel.onCategorySelected(cat)
                            }
                        )

                        TrackIIDropdownField(
                            label = "Código de falla",
                            options = uiState.codes.map { "${it.code} - ${it.description}" },
                            helper = "Selecciona un código",
                            selectedOption = uiState.selectedCode?.let { "${it.code} - ${it.description}" }.orEmpty(),
                            onOptionSelected = { selectedString ->
                                val code = uiState.codes.find { "${it.code} - ${it.description}" == selectedString }
                                if (code != null) viewModel.onCodeSelected(code)
                            }
                        )

                        TrackIITextField(
                            label = "Comentarios del operador",
                            value = uiState.comments,
                            onValueChange = viewModel::onCommentsChange
                        )

                        if (uiState.isLoadingCategories || uiState.isLoadingCodes || uiState.isSubmitting) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.errorMessage?.let {
                            Text(it, color = TTRed, style = MaterialTheme.typography.bodySmall)
                        }

                        PrimaryGlowButton(
                            text = if (uiState.isSubmitting) "Guardando..." else "Confirmar Cancelación",
                            onClick = {
                                keyboardController?.hide()
                                showConfirmDialog = true // 🔥 AHORA ABRE EL DIÁLOGO EN LUGAR DE GUARDAR DIRECTO
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )

                        SoftActionButton(
                            text = "Volver",
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )
                    }
                }
            }

            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
            )

            // 🔥 SOLUCIÓN DEL SOLAPAMIENTO: Le ponemos zIndex(10f) para que SIEMPRE tape al GlassCard
            Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                ScanResultOverlay(visible = showSuccessOverlay, success = true, message = "Cancelación exitosa")
            }
        }
    }
}

private data class ScrapInfoItem(val title: String, val value: String, val icon: ImageVector)

@Composable
private fun ScrapInfoGrid(items: List<ScrapInfoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    ScrapInfoTile(title = item.title, value = item.value, icon = item.icon, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ScrapInfoTile(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.background(Brush.linearGradient(listOf(TTGreenTint, Color.White))).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = TTGreen, modifier = Modifier.size(18.dp))
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