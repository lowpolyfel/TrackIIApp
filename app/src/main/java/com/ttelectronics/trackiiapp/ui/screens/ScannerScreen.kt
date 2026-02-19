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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.ui.layout.ContentScale
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
                ScannerShell(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 18.dp)
                ) {
                    ScannerHeader(taskTitle = taskType.title)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(28.dp))
                                .border(width = 2.dp, color = TTBlue.copy(alpha = 0.85f), shape = RoundedCornerShape(28.dp))
                                .background(Color.Black.copy(alpha = 0.24f))
                        ) {
                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier.fillMaxSize()
                            )
                            ScannerFrameOverlay(showFrame = hasBarcodeInFrame)
                        }
                    }
                    ScannerBottomPanel(
                        lotNumber = lotNumber,
                        partNumber = partNumber,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, bottom = 6.dp, top = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButtonBox(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            tint = TTGreen,
                            borderColor = TTGreen
                        )
                        IconButtonBox(
                            onClick = onHome,
                            icon = Icons.Rounded.Close,
                            tint = Color(0xFFD4172C),
                            borderColor = Color(0xFFD4172C)
                        )
                    }
                }
            } else {
                PermissionFallback(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }

            OrderFoundOverlay(visible = showOrderFound, highlightSuccess = canContinue)
        }
    }
}

@Composable
private fun ScannerShell(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .border(2.dp, TTBlue.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.88f), TTBlueTint.copy(alpha = 0.42f))
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        content()
    }
}

@Composable
private fun ScannerHeader(taskTitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .border(2.dp, TTBlue.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.ttlogo),
                contentDescription = "TT Logo",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }

        Box(
            modifier = Modifier
                .width(148.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TTBlueTint.copy(alpha = 0.65f))
                .border(1.dp, TTBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = taskTitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TTBlue,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
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
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.92f), TTBlueTint.copy(alpha = 0.5f))
                )
            )
            .border(1.dp, TTBlue.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(14.dp),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatusRow(label = "No. Parte", value = partNumber, modifier = Modifier.weight(1f))
        StatusRow(label = "No. Lote", value = lotNumber, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatusRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (value.isBlank()) Color.White.copy(alpha = 0.72f) else TTGreenTint.copy(alpha = 0.4f))
            .border(1.dp, TTBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TTBlue
        )
        Text(
            text = value.ifBlank { "Pendiente" },
            style = MaterialTheme.typography.labelSmall,
            color = if (value.isBlank()) TTTextSecondary else TTGreen,
            maxLines = 1
        )
    }
}

@Composable
private fun IconButtonBox(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    borderColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, borderColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .background(TTBlueTint.copy(alpha = 0.22f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
    }
}

private fun Modifier.borderGlow(): Modifier {
    return this.border(2.dp, TTBlue.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
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
