@file:OptIn(ExperimentalMaterial3Api::class)

package com.ttelectronics.trackiiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.data.local.AppSession
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.ScannerHeader
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ReworkViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ReworkViewModelFactory

@Composable
fun ReworkScreen(
    lotNumber: String,
    partNumber: String,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val viewModel: ReworkViewModel = viewModel(
        factory = ReworkViewModelFactory(
            ServiceLocator.scannerRepository(context),
            AppSession(context)
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val rightSoundPlayer = rememberRawSoundPlayer("right")
    val isCurrentlyHold = uiState.workContext?.wipStatus?.uppercase() == "HOLD"

    var isReleaseAction by remember(uiState.workContext?.wipStatus) { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White.copy(alpha = 0.94f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
        focusedBorderColor = TTBlueDark,
        unfocusedBorderColor = TTBlueDark.copy(alpha = 0.45f),
        focusedTextColor = TTBlueDark,
        unfocusedTextColor = TTBlueDark,
        focusedLabelColor = TTBlueDark,
        unfocusedLabelColor = TTTextSecondary
    )

    LaunchedEffect(lotNumber, partNumber) { viewModel.initialize(lotNumber, partNumber) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            rightSoundPlayer.play()
            Toast.makeText(context, "Retrabajo registrado", Toast.LENGTH_LONG).show()
            onComplete()
        }
    }

    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = (-30).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScannerHeader(taskTitle = "Registro de Retrabajo")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
                        modifier = Modifier.fillMaxWidth().liquidGlass(20)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Lote", color = TTTextSecondary, style = MaterialTheme.typography.labelMedium)
                                Text(lotNumber, color = TTBlueDark, style = MaterialTheme.typography.titleMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Parte", color = TTTextSecondary, style = MaterialTheme.typography.labelMedium)
                                Text(partNumber, color = TTBlueDark, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    if (isCurrentlyHold) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)),
                            modifier = Modifier.fillMaxWidth().liquidGlass(12)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Estado actual: HOLD", color = TTRed, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    FilterChip(
                                        selected = !isReleaseAction,
                                        onClick = { isReleaseAction = false },
                                        label = { Text("Seguir Retrabajando") }
                                    )
                                    FilterChip(
                                        selected = isReleaseAction,
                                        onClick = { isReleaseAction = true },
                                        label = { Text("Liberar a ACTIVE") }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            "Esta acción enviará el material a estado HOLD.",
                            color = TTBlueDark,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Text("Tarea realizada (Localidad)", style = MaterialTheme.typography.labelLarge, color = TTBlueDark)
                    ExposedDropdownMenuBox(
                        expanded = locationExpanded,
                        onExpandedChange = { locationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.locations.firstOrNull { it.locationId == uiState.selectedLocationId }?.locationName.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth().liquidGlass(12),
                            colors = fieldColors,
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = locationExpanded,
                            onDismissRequest = { locationExpanded = false }
                        ) {
                            uiState.locations.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location.locationName) },
                                    onClick = {
                                        viewModel.selectLocation(location.locationId)
                                        locationExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Piezas afectadas", style = MaterialTheme.typography.labelLarge, color = TTBlueDark)
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = {
                            if (it.all(Char::isDigit)) {
                                viewModel.updateQuantity(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().liquidGlass(12),
                        colors = fieldColors,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    Text("Motivo del retrabajo", style = MaterialTheme.typography.labelLarge, color = TTBlueDark)
                    OutlinedTextField(
                        value = uiState.reason,
                        onValueChange = { viewModel.updateReason(it) },
                        modifier = Modifier.fillMaxWidth().height(100.dp).liquidGlass(12),
                        colors = fieldColors,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { keyboardController?.hide() })
                    )

                    if (uiState.isLoading || uiState.isSubmitting) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TTBlueDark)
                        }
                    }

                    uiState.errorMessage?.let {
                        Text(text = it, color = TTRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val effectiveRelease = if (isCurrentlyHold) isReleaseAction else false
                            viewModel.submitRework(isRelease = effectiveRelease)
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TTBlueDark),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !uiState.isSubmitting
                    ) {
                        Text(if (isReleaseAction) "Confirmar y Liberar" else "Confirmar Retrabajo")
                    }

                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !uiState.isSubmitting
                    ) {
                        Text("Volver", color = TTBlueDark)
                    }
                }
            }

            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
            )
        }
    }
}

private fun Modifier.liquidGlass(corner: Int): Modifier {
    return this
        .background(Color.White.copy(alpha = 0.24f), RoundedCornerShape(corner.dp))
        .border(1.dp, TTBlueDark.copy(alpha = 0.24f), RoundedCornerShape(corner.dp))
}
