@file:OptIn(ExperimentalMaterial3Api::class)

package com.ttelectronics.trackiiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
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
    val viewModel: ScrapOrderViewModel = viewModel(
        factory = ScrapOrderViewModelFactory(
            ServiceLocator.scannerRepository(context),
            AppSession(context)
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val rightSoundPlayer = rememberRawSoundPlayer("right")

    var showSuccessOverlay by remember { mutableStateOf(false) }

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

    TrackIIBackground(glowOffsetX = 18.dp, glowOffsetY = 20.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cancelar Orden",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Completa los datos para registrar la cancelación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )

                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.lotNumber,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("No. Lote") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )
                        OutlinedTextField(
                            value = uiState.partNumber,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("No. Parte") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )

                        var categoryExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedCategory?.name.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría de Falla") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            )
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                uiState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            viewModel.onCategorySelected(category)
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var codeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = codeExpanded,
                            onExpandedChange = { isExpanded ->
                                if (uiState.selectedCategory != null) codeExpanded = isExpanded
                            }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedCode?.let { "${it.code} - ${it.description}" }.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                enabled = uiState.selectedCategory != null,
                                label = { Text("Código de Falla") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codeExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            )
                            DropdownMenu(
                                expanded = codeExpanded,
                                onDismissRequest = { codeExpanded = false },
                                modifier = Modifier.exposedDropdownSize()
                            ) {
                                uiState.codes.forEach { code ->
                                    DropdownMenuItem(
                                        text = { Text("${code.code} - ${code.description}") },
                                        onClick = {
                                            viewModel.onCodeSelected(code)
                                            codeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.qtyInput,
                            onValueChange = viewModel::onQtyChange,
                            label = { Text("Piezas a cancelar") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )

                        OutlinedTextField(
                            value = uiState.comments,
                            onValueChange = viewModel::onCommentsChange,
                            label = { Text("Comentarios (Opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(18.dp)
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
                            text = "Confirmar Cancelación",
                            onClick = viewModel::submit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
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
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
            ScanResultOverlay(visible = showSuccessOverlay, success = true, message = "Cancelación exitosa")
        }
    }
}
