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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerHeader
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
    var categoryExpanded by remember { mutableStateOf(false) }
    var codeExpanded by remember { mutableStateOf(false) }

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

    val glassContainer = Color.White.copy(alpha = 0.22f)
    val cardContainer = Color.White.copy(alpha = 0.90f)
    val cardBrush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.92f), Color(0xFFEFF5FF)))

    TrackIIBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScannerHeader(taskTitle = "Cancelar Orden")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = glassContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBrush)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("No. Lote", style = MaterialTheme.typography.labelMedium, color = TTTextSecondary)
                                    Text(uiState.lotNumber, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("No. Parte", style = MaterialTheme.typography.labelMedium, color = TTTextSecondary)
                                    Text(uiState.partNumber, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    Text(
                        "Piezas a cancelar",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = uiState.qtyInput,
                        onValueChange = { newValue -> if (newValue.all { it.isDigit() }) viewModel.onQtyChange(newValue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardContainer, RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardContainer,
                            unfocusedContainerColor = cardContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedCategory?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría de falla") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .background(cardContainer, RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = cardContainer,
                                unfocusedContainerColor = cardContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
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
                            label = { Text("Código de falla") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .background(cardContainer, RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = cardContainer,
                                unfocusedContainerColor = cardContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = codeExpanded,
                            onDismissRequest = { codeExpanded = false }
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

                    Text(
                        "Comentarios del operador",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = uiState.comments,
                        onValueChange = viewModel::onCommentsChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(cardContainer, RoundedCornerShape(12.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardContainer,
                            unfocusedContainerColor = cardContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Describe el motivo de la cancelación...") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )

                    if (uiState.isLoadingCategories || uiState.isLoadingCodes || uiState.isSubmitting) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.errorMessage?.let {
                        Text(it, color = TTRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.submit()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TTRed),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !uiState.isSubmitting
                    ) {
                        Text(
                            "Confirmar Cancelación",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    SoftActionButton(
                        text = "Volver",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmitting
                    )
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
