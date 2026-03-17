package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.SuccessOverlay
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ProductAdvanceFinalReviewViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ProductAdvanceFinalReviewViewModelFactory

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
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val vm: ProductAdvanceFinalReviewViewModel = viewModel(
        factory = ProductAdvanceFinalReviewViewModelFactory(
            ServiceLocator.scannerRepository(context),
            AppSession(context)
        )
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(partNumber) {
        vm.loadPartInfo(partNumber)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            kotlinx.coroutines.delay(1200)
            onComplete()
        }
    }

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Confirmación de registro",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .zIndex(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ReviewInfoGrid(
                            items = listOf(
                                ReviewInfoItem("No. Lote", lotNumber, Icons.Rounded.Inventory2),
                                ReviewInfoItem("No. Parte", partNumber, Icons.Rounded.QrCode),
                                ReviewInfoItem("Área", uiState.partInfo?.areaName ?: "Pendiente", Icons.Rounded.Factory),
                                ReviewInfoItem("Familia", uiState.partInfo?.familyName ?: "Pendiente", Icons.Rounded.Category),
                                ReviewInfoItem("Subfamilia", uiState.partInfo?.subfamilyName ?: "Pendiente", Icons.Rounded.Inventory2)
                            )
                        )

                        Text(text = "Piezas: $qtyIn", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Scrap: $scrap", style = MaterialTheme.typography.titleMedium)

                        if (scrap > 0) {
                            Text(text = "Código: $errorCodeName", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Comentarios: $comments", style = MaterialTheme.typography.bodyMedium)
                        }

                        Text(
                            text = "¿Estás seguro de registrarlo?",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )

                        uiState.errorMessage?.let {
                            Text(text = it, color = TTRed, style = MaterialTheme.typography.bodySmall)
                        }

                        PrimaryGlowButton(
                            text = if (uiState.isSubmitting) "Registrando..." else "Registrar",
                            onClick = {
                                vm.submitAll(
                                    lotNumber = lotNumber,
                                    partNumber = partNumber,
                                    qtyIn = qtyIn,
                                    scrap = scrap,
                                    errorCodeId = errorCodeId,
                                    comments = comments
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )
                        SoftActionButton(text = "Cancelar", onClick = onCancel, modifier = Modifier.fillMaxWidth())
                        SoftActionButton(text = "Editar piezas y scrap", onClick = onEdit, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            FloatingHomeButton(onClick = onHome, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))

            if (uiState.isSuccess) {
                Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                    SuccessOverlay(message = "¡Registro completado exitosamente!")
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
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = item.icon, contentDescription = null, tint = TTGreen)
                                Text(text = item.title, style = MaterialTheme.typography.labelLarge, color = TTTextSecondary)
                            }
                            Text(text = item.value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
