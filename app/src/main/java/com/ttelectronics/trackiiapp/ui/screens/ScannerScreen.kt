package com.ttelectronics.trackiiapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.TabletAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.core.camera.CameraManager
import com.ttelectronics.trackiiapp.ui.components.PermissionFallback
import com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerFrameOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerOverlay
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ScannerViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ScannerViewModelFactory
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

private const val SCAN_WINDOW_LEFT = 0.12f
private const val SCAN_WINDOW_TOP = 0.18f
private const val SCAN_WINDOW_RIGHT = 0.88f
private const val SCAN_WINDOW_BOTTOM = 0.82f

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    taskType: TaskType,
    onBack: () -> Unit,
    onComplete: (String, String, Boolean, String) -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var showInstructions by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3000L)
        showInstructions = false
    }

    var hasBarcodeInFrame by remember { mutableStateOf(false) }
    var showResultOverlay by remember { mutableStateOf(false) }
    var overlaySuccess by remember { mutableStateOf(false) }
    var overlayText by rememberSaveable { mutableStateOf("") }

    val scanSoundPlayer = rememberRawSoundPlayer("scan")
    val rightSoundPlayer = rememberRawSoundPlayer("right")
    val wrongSoundPlayer = rememberRawSoundPlayer("wrong")

    val scannerViewModel: ScannerViewModel = viewModel(factory = ScannerViewModelFactory(ServiceLocator.scannerRepository(context)))
    val scannerUiState by scannerViewModel.uiState.collectAsState()

    val lotNumber = scannerUiState.scannedLot
    val partNumber = scannerUiState.scannedPart
    val canValidate = lotNumber.isNotBlank() && partNumber.isNotBlank()

    LaunchedEffect(canValidate) {
        if (canValidate && !scannerUiState.isValidating && !showResultOverlay) {
            scannerViewModel.validatePart(partNumber)
        }
    }

    LaunchedEffect(scannerUiState.isLotFound, scannerUiState.isPartFound) {
        if (scannerUiState.isLotFound || scannerUiState.isPartFound) {
            scanSoundPlayer.play()
            scannerViewModel.consumeScanEffects()
        }
    }

    LaunchedEffect(scannerUiState.shouldNavigate) {
        if (scannerUiState.shouldNavigate) {
            if (scannerUiState.isProductFound) {
                overlaySuccess = true
                overlayText = context.getString(R.string.order_found)
                showResultOverlay = true
                rightSoundPlayer.play()
                delay(1000)
                onComplete(lotNumber, partNumber, true, "")
            } else {
                overlaySuccess = false
                overlayText = scannerUiState.validationError?.let { context.getString(it) }
                    ?: context.getString(R.string.error_order_not_found_for_part)
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
            if (showInstructions) {
                ScannerInstructionsScreen()
            } else if (hasCameraPermission) {
                CameraScanPanel(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-20).dp),
                    onRawValuesDetected = { rawValues ->
                        hasBarcodeInFrame = rawValues.isNotEmpty()
                        scannerViewModel.procesarFotograma(rawValues)
                    },
                    onScanFailure = { hasBarcodeInFrame = false },
                    hasBarcodeInFrame = hasBarcodeInFrame
                )

                ScannerOverlay(
                    taskTitle = taskType.title,
                    lotNumber = lotNumber,
                    partNumber = partNumber,
                    onReset = {
                        showResultOverlay = false
                        overlayText = ""
                        scannerViewModel.resetScan()
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
private fun ScannerInstructionsScreen() {
    val transition = rememberInfiniteTransition(label = "instruction")
    val scanOffset by transition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(animation = tween(1300), repeatMode = RepeatMode.Reverse),
        label = "instructionLine"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cómo escanear", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Text(
            "Coloca la hoja detrás de la tableta y alinea el código dentro del área verde.",
            style = MaterialTheme.typography.bodyMedium,
            color = TTTextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .aspectRatio(1.4f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Black.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TabletAndroid,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(124.dp)
            )
            Icon(
                imageVector = Icons.Rounded.DocumentScanner,
                contentDescription = null,
                tint = TTGreen,
                modifier = Modifier
                    .size(78.dp)
                    .offset(y = scanOffset.dp)
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraScanPanel(
    modifier: Modifier = Modifier,
    onRawValuesDetected: (List<String>) -> Unit,
    onScanFailure: () -> Unit,
    hasBarcodeInFrame: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val executor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val analysis = remember { ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build() }
    val analyzer = remember {
        ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image ?: run { imageProxy.close(); return@Analyzer }
            mediaImage.setCropRect(buildCropRect(imageProxy.width, imageProxy.height))
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener(mainExecutor) { barcodes ->
                    onRawValuesDetected(barcodes.mapNotNull { it.rawValue })
                }
                .addOnFailureListener(mainExecutor) { onScanFailure() }
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

    val cameraManager = remember { CameraManager(context) }
    val previewView = remember { cameraManager.createPreviewView() }
    DisposableEffect(lifecycleOwner) {
        cameraManager.bind(lifecycleOwner, previewView, analysis, executor, analyzer)
        onDispose { }
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.96f)
            .aspectRatio(2.05f)
            .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        ScannerCropMask()
        ScannerFrameOverlay(showFrame = !hasBarcodeInFrame)
    }
}

@Composable
private fun ScannerCropMask() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
    ) {
        drawRect(Color.Black.copy(alpha = 0.6f))
        val left = size.width * SCAN_WINDOW_LEFT
        val top = size.height * SCAN_WINDOW_TOP
        val width = size.width * (SCAN_WINDOW_RIGHT - SCAN_WINDOW_LEFT)
        val height = size.height * (SCAN_WINDOW_BOTTOM - SCAN_WINDOW_TOP)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
            blendMode = BlendMode.Clear
        )
    }
}

private fun buildCropRect(imageWidth: Int, imageHeight: Int): Rect {
    val left = (imageWidth * SCAN_WINDOW_LEFT).toInt()
    val top = (imageHeight * SCAN_WINDOW_TOP).toInt()
    val right = (imageWidth * SCAN_WINDOW_RIGHT).toInt().coerceAtMost(imageWidth)
    val bottom = (imageHeight * SCAN_WINDOW_BOTTOM).toInt().coerceAtMost(imageHeight)
    return Rect(left, top, right, bottom)
}
