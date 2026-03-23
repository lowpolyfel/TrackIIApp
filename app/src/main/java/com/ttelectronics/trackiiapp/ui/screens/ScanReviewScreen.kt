package com.ttelectronics.trackiiapp.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.core.demo.DemoMode
import com.ttelectronics.trackiiapp.data.models.scanner.PartLookupResponse
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.ScannerHeader
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

private data class ScanInfoItem(val title: String, val value: String, val icon: ImageVector)

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
    var partInfo by remember { mutableStateOf<PartLookupResponse?>(null) }

    LaunchedEffect(partNumber, orderFound) {
        if (orderFound && partNumber.isNotBlank()) {
            partInfo = if (taskType == TaskType.ProductAdvance && DemoMode.isProductAdvanceDemoEnabled()) {
                DemoMode.demoPartInfo(partNumber.trim())
            } else {
                runCatching {
                    ServiceLocator.scannerRepository(context).lookupPart(partNumber.trim())
                }.getOrNull()
            }
        }
    }

    val infoItems = listOf(
        ScanInfoItem("No. Lote", lotNumber, Icons.Rounded.Inventory2),
        ScanInfoItem("No. Parte", partNumber, Icons.Rounded.QrCode),
        ScanInfoItem("Área", partInfo?.areaName ?: "Pendiente", Icons.Rounded.Factory),
        ScanInfoItem("Familia", partInfo?.familyName ?: "Pendiente", Icons.Rounded.Category),
        ScanInfoItem("Subfamilia", partInfo?.subfamilyName ?: "Pendiente", Icons.Rounded.Inventory2)
    )

    TrackIIBackground(glowOffsetX = 24.dp, glowOffsetY = 120.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScannerHeader(taskTitle = taskType.title)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (orderFound) "Lectura correcta, por favor revisa la información." else "Atención, ocurrió un problema con la lectura.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .zIndex(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val iconColor = if (orderFound) TTGreen else TTRed
                        val statusIcon = if (orderFound) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline

                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(iconColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        if (orderFound) {
                            ScanInfoGrid(items = infoItems)
                        }

                        if (!orderFound && errorMessage.isNotBlank()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = TTRed.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = TTRed,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (orderFound) {
                            PrimaryGlowButton(
                                text = "Confirmar y Continuar",
                                onClick = onConfirm,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        SoftActionButton(
                            text = "Volver a Escanear",
                            onClick = onRescan,
                            modifier = Modifier.fillMaxWidth()
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
        }
    }
}

@Composable
private fun ScanInfoGrid(items: List<ScanInfoItem>) {
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
                                .background(Brush.linearGradient(listOf(TTGreenTint, Color.White)))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = item.icon, contentDescription = null, tint = TTGreen, modifier = Modifier.size(18.dp))
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
