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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ScannerViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ScannerViewModelFactory
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    taskType: TaskType,
    onBack: () -> Unit,
    onComplete: (String, String, Boolean, String) -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var lotNumber by rememberSaveable { mutableStateOf("") }
    var partNumber by rememberSaveable { mutableStateOf("") }
    var hasBarcodeInFrame by remember { mutableStateOf(false) }
    var showResultOverlay by remember { mutableStateOf(false) }
    var overlaySuccess by remember { mutableStateOf(false) }
    var overlayText by remember { mutableStateOf("") }

    val lotRegex = remember { Regex("^[0-9]{7}$") }
    val partRegex = remember { Regex("^[A-Z](?=.*[0-9])[A-Z0-9._/-]{3,}$") }
    var lotScanState by remember { mutableStateOf(StableScanState()) }
    var partScanState by remember { mutableStateOf(StableScanState()) }
    val scanSoundPlayer = rememberRawSoundPlayer("scan")
    val rightSoundPlayer = rememberRawSoundPlayer("right")
    val wrongSoundPlayer = rememberRawSoundPlayer("wrong")

    val scannerViewModel: ScannerViewModel = viewModel(factory = ScannerViewModelFactory(ServiceLocator.scannerRepository(context)))
    val scannerUiState by scannerViewModel.uiState.collectAsState()

    val onLotFound by rememberUpdatedState<(String) -> Unit> { if (lotNumber.isBlank()) lotNumber = it }
    val onPartFound by rememberUpdatedState<(String) -> Unit> { if (partNumber.isBlank()) partNumber = it }

    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val analysis = remember { ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build() }
    val analyzer = remember {
        ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image ?: run { imageProxy.close(); return@Analyzer }
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener(mainExecutor) { barcodes ->
                    val now = System.currentTimeMillis()
                    val barcodeCandidates = barcodes.mapNotNull { barcode ->
                        val rawValue = barcode.rawValue?.trim()?.uppercase()?.replace(" ", "")?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                        val box = barcode.boundingBox
                        val areaRatio = if (box == null || imageProxy.width == 0 || imageProxy.height == 0) 0f
                        else (box.width().toFloat() * box.height().toFloat()) / (imageProxy.width * imageProxy.height).toFloat()
                        BarcodeCandidate(value = rawValue, areaRatio = areaRatio)
                    }

                    hasBarcodeInFrame = barcodeCandidates.isNotEmpty()
                    if (barcodeCandidates.isEmpty()) {
                        lotScanState = lotScanState.clear()
                        partScanState = partScanState.clear()
                    } else {
                        val lotCandidate = barcodeCandidates.filter { lotRegex.matches(it.value) }.maxByOrNull { it.areaRatio }
                        val partCandidate = barcodeCandidates.filter { partRegex.matches(it.value) }.maxByOrNull { it.areaRatio }

                        if (lotNumber.isBlank() && lotCandidate != null) {
                            val requiredReads = requiredStableReads(lotCandidate.areaRatio)
                            lotScanState = lotScanState.record(lotCandidate.value)
                            if (lotScanState.canAccept(now, requiredReads)) {
                                onLotFound(lotCandidate.value)
                                lotScanState = lotScanState.markAccepted(now)
                                if (partNumber.isBlank()) scanSoundPlayer.play()
                            }
                        }
                        if (partNumber.isBlank() && partCandidate != null) {
                            val requiredReads = requiredStableReads(partCandidate.areaRatio)
                            partScanState = partScanState.record(partCandidate.value)
                            if (partScanState.canAccept(now, requiredReads)) {
                                onPartFound(partCandidate.value)
                                partScanState = partScanState.markAccepted(now)
                                if (lotNumber.isBlank()) scanSoundPlayer.play()
                            }
                        }
                    }
                }
                .addOnFailureListener(mainExecutor) { hasBarcodeInFrame = false }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            analysis.clearAnalyzer()
            barcodeScanner.close()
            executor.shutdown()
        }
    }

    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            analysis.setAnalyzer(executor, analyzer)
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
        onDispose { }
    }

    val canValidate = lotNumber.isNotBlank() && partNumber.isNotBlank()
    LaunchedEffect(canValidate) {
        if (canValidate && !scannerUiState.isValidating && !showResultOverlay) {
            scannerViewModel.validatePart(partNumber)
        }
    }

    LaunchedEffect(scannerUiState.shouldNavigate) {
        if (scannerUiState.shouldNavigate) {
            if (scannerUiState.isProductFound) {
                overlaySuccess = true
                overlayText = "Orden encontrada"
                showResultOverlay = true
                rightSoundPlayer.play()
                delay(1000)
                onComplete(lotNumber, partNumber, true, "")
            } else {
                overlaySuccess = false
                overlayText = scannerUiState.validationError ?: "No se encontró la orden para esta parte."
                showResultOverlay = true
                wrongSoundPlayer.play()
                delay(1300)
                onComplete(lotNumber, partNumber, false, overlayText)
            }
            scannerViewModel.consumeNavigation()
        }
    }

    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = (-30).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.96f).aspectRatio(1.65f).align(Alignment.Center).offset(y = (-20).dp)
                        .clip(RoundedCornerShape(26.dp)).background(Color.Black.copy(alpha = 0.12f))
                ) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                    ScannerFrameOverlay(showFrame = !hasBarcodeInFrame)
                }

                ScannerOverlay(
                    taskTitle = taskType.title,
                    lotNumber = lotNumber,
                    partNumber = partNumber,
                    onReset = {
                        lotNumber = ""
                        partNumber = ""
                        showResultOverlay = false
                        overlayText = ""
                    },
                    onBack = onBack,
                    statusText = when {
                        scannerUiState.isValidating -> "Validando parte en BD..."
                        canValidate -> "Escaneo completo, esperando validación..."
                        else -> "Escanea lote y parte"
                    }
                )
            } else {
                PermissionFallback(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }

            ScanResultOverlay(visible = showResultOverlay, success = overlaySuccess, message = overlayText)
        }
    }
}

@Composable
private fun ScannerOverlay(
    taskTitle: String,
    lotNumber: String,
    partNumber: String,
    onReset: () -> Unit,
    onBack: () -> Unit,
    statusText: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(TTBlueDark.copy(alpha = 0.18f), Color.Transparent)))
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = R.drawable.ttlogo), contentDescription = "TT logo", modifier = Modifier.fillMaxWidth(0.42f).height(38.dp))
            Text(text = taskTitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, TTBlueDark.copy(alpha = 0.16f))))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScannerInfoCard(lotNumber = lotNumber, partNumber = partNumber)
            Text(statusText, color = Color.White, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftActionButton(text = "Reiniciar", onClick = onReset, modifier = Modifier.weight(1f))
                SoftActionButton(text = "Volver", onClick = onBack, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ScanResultOverlay(visible: Boolean, success: Boolean, message: String) {
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
private fun ScannerFrameOverlay(showFrame: Boolean) {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val lineOffset by transition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(2400), repeatMode = RepeatMode.Reverse), label = "scanLineOffset")
    val frameAlpha by animateFloatAsState(targetValue = if (showFrame) 1f else 0f, animationSpec = tween(360), label = "frameAlpha")

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.matchParentSize().graphicsLayer { alpha = frameAlpha }.clip(RoundedCornerShape(26.dp)).borderGlow()) {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).offset(y = (lineOffset * 240f).dp).background(TTGreen.copy(alpha = 0.85f)).blur(2.dp))
            CornerMarkers(modifier = Modifier.matchParentSize())
            ScannerHint(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun ScannerHint(modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.DocumentScanner, contentDescription = null, tint = TTBlueDark)
            Text(text = "Alinea código de barras en el marco", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ScannerInfoCard(lotNumber: String, partNumber: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Escaneo", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            ScannerInfoRow(label = "No. Lote", value = lotNumber)
            ScannerInfoRow(label = "No. Parte", value = partNumber)
        }
    }
}

@Composable
private fun ScannerInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TTTextSecondary)
        Text(text = if (value.isBlank()) "Pendiente" else value, fontWeight = FontWeight.SemiBold)
    }
}

private data class BarcodeCandidate(val value: String, val areaRatio: Float)
private data class StableScanState(val candidate: String = "", val count: Int = 0, val lastAcceptedAt: Long = 0L) {
    fun record(value: String): StableScanState = if (candidate == value) copy(count = count + 1) else StableScanState(candidate = value, count = 1, lastAcceptedAt = lastAcceptedAt)
    fun canAccept(now: Long, requiredReads: Int): Boolean = count >= requiredReads && now - lastAcceptedAt > 800L
    fun markAccepted(now: Long): StableScanState = copy(lastAcceptedAt = now)
    fun clear(): StableScanState = if (candidate.isEmpty() && count == 0) this else StableScanState(lastAcceptedAt = lastAcceptedAt)
}

private fun requiredStableReads(areaRatio: Float): Int = when {
    areaRatio >= 0.14f -> 1
    areaRatio >= 0.08f -> 2
    else -> 3
}

private fun Modifier.borderGlow(): Modifier = this.background(
    Brush.radialGradient(
        colors = listOf(TTGreen.copy(alpha = 0.25f), Color.Transparent),
        radius = 780f
    )
)

@Composable
private fun CornerMarkers(modifier: Modifier = Modifier) {
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
private fun PermissionFallback(onRequest: () -> Unit) {
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
