@file:OptIn(ExperimentalMaterial3Api::class)

package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.ScannerHeader
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.SuccessOverlayDialog
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
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
    val vm: PartialScrapViewModel = viewModel(
        factory = PartialScrapViewModelFactory(
            ServiceLocator.scannerRepository(context),
            AppSession(context)
        )
    )
    val uiState by vm.uiState.collectAsState()
    var categoryExpanded by remember { mutableStateOf(false) }
    var codeExpanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lotNumber, partNumber, difference) { vm.initialize(lotNumber, partNumber, difference) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            showSuccessDialog = true
            kotlinx.coroutines.delay(1400)
            showSuccessDialog = false
            onComplete()
        }
    }

    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = (-30).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScannerHeader(taskTitle = "Scrap parcial")
                Text(
                    text = "Hay ${uiState.difference} piezas de diferencia con el paso anterior. ¿Desea registrarlas en scrap?",
                    style = MaterialTheme.typography.titleMedium
                )

                if (uiState.shouldRegister == null) {
                    PrimaryGlowButton(text = "Sí", onClick = { vm.answerShouldRegister(true) }, modifier = Modifier.fillMaxWidth())
                    SoftActionButton(text = "No", onClick = onComplete, modifier = Modifier.fillMaxWidth())
                }

                if (uiState.shouldRegister == true) {
                    OutlinedTextField(
                        value = uiState.difference.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cantidad (piezas)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                        OutlinedTextField(
                            value = uiState.selectedCategory?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría de falla") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            uiState.categories.forEach { category ->
                                DropdownMenuItem(text = { Text(category.name) }, onClick = {
                                    vm.onCategorySelected(category)
                                    categoryExpanded = false
                                })
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = codeExpanded,
                        onExpandedChange = { if (uiState.selectedCategory != null) codeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedCode?.let { "${it.code} - ${it.description}" }.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            enabled = uiState.selectedCategory != null,
                            label = { Text("Código de falla") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = codeExpanded, onDismissRequest = { codeExpanded = false }) {
                            uiState.codes.forEach { code ->
                                DropdownMenuItem(text = { Text("${code.code} - ${code.description}") }, onClick = {
                                    vm.onCodeSelected(code)
                                    codeExpanded = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.comments,
                        onValueChange = vm::onCommentsChange,
                        label = { Text("Comentarios (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.isLoadingCategories || uiState.isLoadingCodes || uiState.isSubmitting) {
                        CircularProgressIndicator()
                    }

                    uiState.errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

                    PrimaryGlowButton(
                        text = if (uiState.isSubmitting) "Guardando..." else "Guardar",
                        onClick = vm::submit,
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SoftActionButton(text = "No", onClick = onComplete, modifier = Modifier.fillMaxWidth())
                }
            }

            FloatingHomeButton(onClick = onHome, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))

            SuccessOverlayDialog(
                title = "Registro exitoso",
                message = "Scrap parcial registrado correctamente.",
                show = showSuccessDialog
            )
        }
    }
}
