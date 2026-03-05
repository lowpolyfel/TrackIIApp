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
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIIDropdownField
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.PartialScrapViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.PartialScrapViewModelFactory

@Composable
fun PartialScrapScreen(
    lotNumber: String,
    partNumber: String,
    difference: Int,
    onComplete: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: PartialScrapViewModel = viewModel(
        factory = PartialScrapViewModelFactory(
            ServiceLocator.scannerRepository(context),
            AppSession(context)
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val rightSoundPlayer = rememberRawSoundPlayer("right")

    var showSuccessOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(lotNumber, partNumber, difference) {
        viewModel.initialize(lotNumber, partNumber, difference)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            showSuccessOverlay = true
            rightSoundPlayer.play()
            Toast.makeText(context, "Scrap registrado con éxito", Toast.LENGTH_LONG).show()
            kotlinx.coroutines.delay(1200)
            onComplete()
        }
    }

    val infoItems = listOf(
        PartialScrapInfoItem("No. Lote", uiState.lotNumber, Icons.Rounded.Inventory2),
        PartialScrapInfoItem("No. Parte", uiState.partNumber, Icons.Rounded.QrCode),
        PartialScrapInfoItem("Faltantes", "${uiState.difference} piezas", Icons.Rounded.Warning)
    )

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Header
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScannerHeader(taskTitle = "Registro de Scrap")
            }

            // Contenedor Central para el Texto y el GlassCard
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Texto superior alineado
                Text(
                    text = "Por favor justifica la diferencia de piezas faltantes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                )

                // GlassCard Central
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .zIndex(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // Tarjetas informativas
                        PartialScrapInfoGrid(items = infoItems)

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
                            label = "Comentarios (Opcional)",
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
                            text = if (uiState.isSubmitting) "Guardando..." else "Confirmar Scrap",
                            onClick = {
                                keyboardController?.hide()
                                viewModel.submit()
                            },
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

            ScanResultOverlay(visible = showSuccessOverlay, success = true, message = "Scrap registrado")
        }
    }
}

// =========================================================================
// Componentes visuales exclusivos para Parcial Scrap
// =========================================================================

private data class PartialScrapInfoItem(val title: String, val value: String, val icon: ImageVector)

@Composable
private fun PartialScrapInfoGrid(items: List<PartialScrapInfoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    // Destaca el cuadro de "Faltantes" en rojo para prevenir errores del operador
                    val colorTint = if (item.title == "Faltantes") TTRed else TTGreen
                    val bgTint = if (item.title == "Faltantes") Color(0xFFFFF0F0) else TTGreenTint

                    androidx.compose.material3.Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.background(Brush.linearGradient(listOf(bgTint, Color.White))).padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = item.icon, contentDescription = null, tint = colorTint, modifier = Modifier.size(18.dp))
                                Text(text = item.title, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
                            }
                            Text(
                                text = item.value,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}