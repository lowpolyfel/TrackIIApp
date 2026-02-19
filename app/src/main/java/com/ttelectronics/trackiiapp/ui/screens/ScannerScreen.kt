package com.ttelectronics.trackiiapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.ShieldMoon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    taskType: TaskType,
    onBack: () -> Unit,
    onComplete: (String, String) -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var lotNumber by rememberSaveable { mutableStateOf("") }
    var partNumber by rememberSaveable { mutableStateOf("") }
    var hasBarcodeInFrame by remember { mutableStateOf(false) }
    var showOrderFound by remember { mutableStateOf(false) }
    var hasAutoNavigated by remember { mutableStateOf(false) }

    val lotRegex = remember { Regex("^[0-9]{7}$") }
    val partRegex = remember { Regex("^[A-Z](?=.*[0-9])[A-Z0-9._/-]{3,}$") }
    var lotScanState by remember { mutableStateOf(StableScanState()) }
    var partScanState by remember { mutableStateOf(StableScanState()) }
    val scanSoundPlayer = rememberRawSoundPlayer("scan")
    val rightSoundPlayer = rememberRawSoundPlayer("right")

    val onLotFound by rememberUpdatedState<(String) -> Unit> { value ->
        if (lotNumber.isBlank()) {
            lotNumber = value
        }
    }
    val onPartFound by rememberUpdatedState<(String) -> Unit> { value ->
        if (partNumber.isBlank()) {
            partNumber = value
        }
    }

    LaunchedEffect(lotNumber, partNumber) {
        if (lotNumber.isBlank() || partNumber.isBlank()) {
            showOrderFound = false
            hasAutoNavigated = false
        }
    }

    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val analysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val analyzer = remember {
        ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return@Analyzer
            }
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            barcodeScanner.process(inputImage)
                .addOnSuccessListener(mainExecutor) { barcodes ->
                    val now = System.currentTimeMillis()
                    val barcodeCandidates = barcodes.mapNotNull { barcode ->
                        val rawValue = barcode.rawValue
                            ?.trim()
                            ?.uppercase()
                            ?.replace(" ", "")
                            ?.takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null

                        val box = barcode.boundingBox
                        val areaRatio = if (box == null || imageProxy.width == 0 || imageProxy.height == 0) {
                            0f
                        } else {
                            (box.width().toFloat() * box.height().toFloat()) / (imageProxy.width * imageProxy.height).toFloat()
                        }
                        BarcodeCandidate(value = rawValue, areaRatio = areaRatio)
                    }

                    hasBarcodeInFrame = barcodeCandidates.isNotEmpty()
                    if (barcodeCandidates.isEmpty()) {
                        lotScanState = lotScanState.clear()
                        partScanState = partScanState.clear()
                    } else {
                        val lotCandidate = barcodeCandidates
                            .filter { lotRegex.matches(it.value) }
                            .maxByOrNull { it.areaRatio }
                        val partCandidate = barcodeCandidates
                            .filter { partRegex.matches(it.value) }
                            .maxByOrNull { it.areaRatio }

                        if (lotNumber.isBlank() && lotCandidate != null) {
                            val requiredReads = requiredStableReads(lotCandidate.areaRatio)
                            lotScanState = lotScanState.record(lotCandidate.value)
                            if (lotScanState.canAccept(now, requiredReads)) {
                                onLotFound(lotCandidate.value)
                                lotScanState = lotScanState.markAccepted(now)
                                if (partNumber.isBlank()) {
                                    scanSoundPlayer.play()
                                }
                            }
                        }
                        if (partNumber.isBlank() && partCandidate != null) {
                            val normalizedPart = partCandidate.value
                            val requiredReads = requiredStableReads(partCandidate.areaRatio)
                            partScanState = partScanState.record(normalizedPart)
                            if (partScanState.canAccept(now, requiredReads)) {
                                onPartFound(normalizedPart)
                                partScanState = partScanState.markAccepted(now)
                                if (lotNumber.isBlank()) {
                                    scanSoundPlayer.play()
                                }
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) {
        onDispose {
            analysis.clearAnalyzer()
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
            barcodeScanner.close()
            executor.shutdown()
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                analysis.setAnalyzer(executor, analyzer)
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    val canContinue = lotNumber.isNotBlank() && partNumber.isNotBlank()
    LaunchedEffect(canContinue) {
        if (canContinue && !hasAutoNavigated) {
            hasAutoNavigated = true
            showOrderFound = true
            rightSoundPlayer.play()
            delay(1100)
            onComplete(lotNumber, partNumber)
        }
    }

    TrackIIBackground(glowOffsetX = 0.dp, glowOffsetY = (-180).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScannerHeader()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.54f)
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.Black.copy(alpha = 0.16f))
                        ) {
                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.84f)
                            )
                            ScannerFrameOverlay(showFrame = hasBarcodeInFrame)
                        }
                    }
                    ScannerBottomPanel(
                        lotNumber = lotNumber,
                        partNumber = partNumber,
                        modifier = Modifier.fillMaxWidth(0.54f)
                    )
                }
            } else {
                PermissionFallback(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }

            OrderFoundOverlay(visible = showOrderFound, highlightSuccess = canContinue)
        }
    }
}

@Composable
private fun ScannerHeader(taskTitle: String = "Escaneo de Orden") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = taskTitle,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            maxLines = 1
        )
        Text(
            text = "TT Electronics",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
private fun ScannerBottomPanel(
    lotNumber: String,
    partNumber: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.95f), TTBlueTint.copy(alpha = 0.32f))),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScannerInfoCard(lotNumber = lotNumber, partNumber = partNumber)
    }
}

@Composable
private fun OrderFoundOverlay(visible: Boolean, highlightSuccess: Boolean) {
    if (!visible) return
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (highlightSuccess) TTGreen.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.38f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(TTGreenTint, Color.White)
                            )
                        )
                        .padding(horizontal = 36.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = TTGreen,
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "Orden encontrada",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Continuando automáticamente...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TTTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
}

@Composable
private fun ScannerFrameOverlay(showFrame: Boolean) {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val lineOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineOffset"
    )
    val frameAlpha by animateFloatAsState(
        targetValue = if (showFrame) 1f else 0f,
        animationSpec = tween(360),
        label = "frameAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = frameAlpha }
                .clip(RoundedCornerShape(26.dp))
                .borderGlow()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (lineOffset * 190).dp)
                    .graphicsLayer { alpha = frameAlpha }
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, TTAccent, Color.Transparent)
                        )
                    )
                    .blur(2.dp)
            )
            Icon(
                imageVector = Icons.Rounded.CenterFocusStrong,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f * frameAlpha),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
    }
}

@Composable
private fun ScannerInfoCard(lotNumber: String, partNumber: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, TTBlueTint.copy(alpha = 0.55f))
                    )
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Escanea el No. Lote (7 dígitos) y No. Parte (inicia con letra).",
                style = MaterialTheme.typography.bodyMedium,
                color = TTTextSecondary
            )
            StatusRow(
                label = "No. Lote",
                value = lotNumber,
                placeholder = "Esperando código de 7 dígitos"
            )
            StatusRow(
                label = "No. Parte",
                value = partNumber,
                placeholder = "Esperando código con letra inicial"
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, placeholder: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (value.isBlank()) Color.White.copy(alpha = 0.7f) else TTGreen.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (value.isBlank()) TTBlueTint else TTGreenTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (value.isBlank()) Icons.Rounded.DocumentScanner else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (value.isBlank()) TTBlue else TTGreen
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = if (value.isBlank()) placeholder else value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isBlank()) TTTextSecondary else TTGreen
            )
        }
        if (value.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TTGreen.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Detectado",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TTGreen
                )
            }
        }
    }
}

@Composable
private fun PermissionFallback(onRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(TTBlueTint, Color.White)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ShieldMoon,
                    contentDescription = null,
                    tint = TTBlue,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Activa la cámara para escanear",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Necesitamos acceso para detectar el No. Lote y No. Parte en tu hoja.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = TTBlue)
                ) {
                    Text(text = "Permitir cámara", color = Color.White)
                }
            }
        }
    }
}

private fun Modifier.borderGlow(): Modifier {
    return this
        .graphicsLayer {
            shadowElevation = 24f
            shape = RoundedCornerShape(28.dp)
            clip = true
        }
        .background(
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.5f), TTAccent.copy(alpha = 0.4f))
            )
        )
}
