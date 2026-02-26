package com.ttelectronics.trackiiapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ShieldMoon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun ScannerHeader(taskTitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ttlogo),
            contentDescription = "TT logo",
            modifier = Modifier.height(32.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TTBlueDark.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Text(
                text = taskTitle,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TTBlueDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ScannerControlsPanel(
    lotNumber: String,
    partNumber: String,
    isValidating: Boolean,
    canValidate: Boolean,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val statusText = when {
        isValidating -> "Validando en BD..."
        canValidate -> "Escaneo listo, validando..."
        else -> "Escanea lote y parte para continuar"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Datos Capturados",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TTBlueDark
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("No. Lote", style = MaterialTheme.typography.labelMedium, color = TTTextSecondary)
                        Text(
                            text = if (lotNumber.isBlank()) "---" else lotNumber,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (lotNumber.isBlank()) TTTextSecondary else Color.Black
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("No. Parte", style = MaterialTheme.typography.labelMedium, color = TTTextSecondary)
                        Text(
                            text = if (partNumber.isBlank()) "---" else partNumber,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (partNumber.isBlank()) TTTextSecondary else Color.Black
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (canValidate) Icons.Rounded.CheckCircle else Icons.Rounded.DocumentScanner,
                        contentDescription = null,
                        tint = if (canValidate) TTGreen else TTTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canValidate) TTGreen else TTTextSecondary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SoftActionButton(text = "Reiniciar", onClick = onReset, modifier = Modifier.weight(1f))
            SoftActionButton(text = "Volver", onClick = onBack, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ScanResultOverlay(visible: Boolean, success: Boolean, message: String) {
    if (!visible) return
    Box(
        modifier = Modifier.fillMaxSize().background(if (success) TTGreen.copy(alpha = 0.75f) else TTRed.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.background(Brush.verticalGradient(listOf(if (success) TTGreenTint else Color(0xFFFFE6E6), Color.White))).padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (success) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                    contentDescription = null,
                    tint = if (success) TTGreen else TTRed,
                    modifier = Modifier.size(72.dp)
                )
                Text(text = if (success) "Orden encontrada" else "Orden no encontrada", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                Text(text = if (success) "Continuando..." else message, style = MaterialTheme.typography.bodyMedium, color = TTTextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ScannerFrameOverlay(showFrame: Boolean) {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val lineOffset by transition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(2400), repeatMode = RepeatMode.Reverse), label = "scanLineOffset")
    val frameAlpha by animateFloatAsState(targetValue = if (showFrame) 1f else 0f, animationSpec = tween(360), label = "frameAlpha")

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.matchParentSize().graphicsLayer { alpha = frameAlpha }.clip(RoundedCornerShape(26.dp)).borderGlow()) {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).offset(y = (lineOffset * 240f).dp).background(TTGreen.copy(alpha = 0.85f)).blur(2.dp))
            CornerMarkers(modifier = Modifier.matchParentSize())
            if (showFrame) {
                Icon(
                    imageVector = Icons.Rounded.DocumentScanner,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.Center).size(92.dp)
                )
            }
            ScannerHint(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun ScannerHint(modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.DocumentScanner, contentDescription = null, tint = TTBlueDark)
            Text(text = "Alinea código de barras en el marco", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun ScannerInfoCard(lotNumber: String, partNumber: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Escaneo", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            ScannerInfoRow(label = "No. Lote", value = lotNumber)
            ScannerInfoRow(label = "No. Parte", value = partNumber)
        }
    }
}

@Composable
fun ScannerInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TTTextSecondary)
        Text(text = if (value.isBlank()) "Pendiente" else value, fontWeight = FontWeight.SemiBold)
    }
}

private fun Modifier.borderGlow(): Modifier = this.background(
    Brush.radialGradient(
        colors = listOf(TTGreen.copy(alpha = 0.25f), Color.Transparent),
        radius = 780f
    )
)

@Composable
fun CornerMarkers(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CornerMarker(modifier = Modifier.align(Alignment.TopStart))
        CornerMarker(modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { rotationZ = 90f })
        CornerMarker(modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer { rotationZ = 180f })
        CornerMarker(modifier = Modifier.align(Alignment.BottomStart).graphicsLayer { rotationZ = 270f })
    }
}

@Composable
private fun CornerMarker(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(10.dp).size(40.dp)) {
        Box(modifier = Modifier.align(Alignment.TopStart).fillMaxWidth(0.72f).height(4.dp).background(TTGreen, RoundedCornerShape(8.dp)))
        Box(modifier = Modifier.align(Alignment.TopStart).fillMaxSize(0.72f).background(TTGreen, RoundedCornerShape(8.dp)))
    }
}

@Composable
fun PermissionFallback(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(imageVector = Icons.Rounded.ShieldMoon, contentDescription = null, tint = TTBlueDark, modifier = Modifier.size(66.dp))
                Text(text = "Activa la cámara para escanear", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = TTBlueDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.CenterFocusStrong, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Permitir cámara")
                }
            }
        }
    }
}
