package com.ttelectronics.trackiiapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTGreenTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

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
    val partRegex = remember { Regex("^[A-Za-z].+") }
    var lotScanState by remember { mutableStateOf(StableScanState()) }
    var partScanState by remember { mutableStateOf(StableScanState()) }
    var playedScanForCodes by remember { mutableStateOf(setOf<String>()) }
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
            if (lotNumber.isBlank() && partNumber.isBlank()) {
                playedScanForCodes = emptySet()
            }
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
                    val rawValues = barcodes.mapNotNull { it.rawValue?.trim() }.filter { it.isNotEmpty() }
                    hasBarcodeInFrame = rawValues.isNotEmpty()
                    if (rawValues.isEmpty()) {
                        lotScanState = lotScanState.clear()
                        partScanState = partScanState.clear()
                    } else {
                        val lotCandidate = rawValues.firstOrNull { lotRegex.matches(it) }
                        val partCandidate = rawValues.firstOrNull { partRegex.matches(it) }
                        if (lotNumber.isBlank() && lotCandidate != null) {
                            lotScanState = lotScanState.record(lotCandidate)
                            if (lotScanState.canAccept(now)) {
                                onLotFound(lotCandidate)
                                lotScanState = lotScanState.markAccepted(now)
                                val completesBoth = partNumber.isNotBlank()
                                if (!completesBoth && !playedScanForCodes.contains(lotCandidate)) {
                                    scanSoundPlayer.play()
                                    playedScanForCodes = playedScanForCodes + lotCandidate
                                }
                            }
                        }
                        if (partNumber.isBlank() && partCandidate != null) {
                            val normalizedPart = partCandidate.uppercase()
                            partScanState = partScanState.record(normalizedPart)
                            if (partScanState.canAccept(now)) {
                                onPartFound(normalizedPart)
                                partScanState = partScanState.markAccepted(now)
                                val completesBoth = lotNumber.isNotBlank()
                                if (!completesBoth && !playedScanForCodes.contains(normalizedPart)) {
                                    scanSoundPlayer.play()
                                    playedScanForCodes = playedScanForCodes + normalizedPart
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

    DisposableEffect(Unit) {
        onDispose {
            analysis.clearAnalyzer()
            barcodeScanner.close()
            executor.shutdown()
        }
    }

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

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

    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = (-30).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1.4f)
                        .align(Alignment.Center)
                        .offset(y = (-48).dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.Black.copy(alpha = 0.12f))
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                    ScannerFrameOverlay(showFrame = !hasBarcodeInFrame)
                }

                ScannerOverlay(
                    taskTitle = taskType.title,
                    lotNumber = lotNumber,
                    partNumber = partNumber,
                    onReset = {
                        lotNumber = ""
                        partNumber = ""
                        showOrderFound = false
                        hasAutoNavigated = false
                        playedScanForCodes = emptySet()
                    },
                    onBack = onBack,
                    onContinue = {
                        onComplete(lotNumber, partNumber)
                    },
                    canContinue = canContinue
                )
            } else {
                PermissionFallback(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }

            OrderFoundOverlay(visible = showOrderFound, highlightSuccess = canContinue)
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            )
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
    onContinue: () -> Unit,
    canContinue: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(TTBlueDark.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = "Escaneo inteligente",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = taskTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, TTBlueDark.copy(alpha = 0.16f))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScannerInfoCard(lotNumber = lotNumber, partNumber = partNumber)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SoftActionButton(
                    text = "Reiniciar",
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                )
                PrimaryGlowButton(
                    text = "Continuar",
                    onClick = onContinue,
                    enabled = canContinue,
                    modifier = Modifier.weight(1f)
                )
            }
            SoftActionButton(
                text = "Volver",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OrderFoundOverlay(visible: Boolean, highlightSuccess: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.9f, animationSpec = tween(260)),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f, animationSpec = tween(200))
    ) {
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Escanea el No. Lote (7 dígitos) y No. Parte (inicia con letra).",
                style = MaterialTheme.typography.bodySmall,
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = if (value.isBlank()) placeholder else value,
                style = MaterialTheme.typography.bodySmall,
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
                    style = MaterialTheme.typography.bodySmall,
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

private data class StableScanState(
    val lastValue: String = "",
    val stableCount: Int = 0,
    val lastAcceptedAt: Long = 0L
) {
    fun record(value: String): StableScanState {
        val newCount = if (value == lastValue) stableCount + 1 else 1
        return copy(lastValue = value, stableCount = newCount)
    }

    fun canAccept(now: Long): Boolean {
        return stableCount >= REQUIRED_STABLE_READS && now - lastAcceptedAt > MIN_ACCEPT_INTERVAL_MS
    }

    fun markAccepted(now: Long): StableScanState {
        return copy(lastValue = "", stableCount = 0, lastAcceptedAt = now)
    }

    fun clear(): StableScanState = copy(lastValue = "", stableCount = 0)
}



private const val REQUIRED_STABLE_READS = 3
private const val MIN_ACCEPT_INTERVAL_MS = 1200L
