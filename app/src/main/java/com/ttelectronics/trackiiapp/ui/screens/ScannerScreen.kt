package com.ttelectronics.trackiiapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.core.camera.CameraManager
import com.ttelectronics.trackiiapp.ui.components.PermissionFallback
import com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerFrameOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerOverlay
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
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

    var hasBarcodeInFrame by remember { mutableStateOf(false) }
    var showResultOverlay by remember { mutableStateOf(false) }
    var overlaySuccess by remember { mutableStateOf(false) }
    var overlayText by rememberSaveable { mutableStateOf("") }

    val scanSoundPlayer = rememberRawSoundPlayer("scan")
    val rightSoundPlayer = rememberRawSoundPlayer("right")
    val wrongSoundPlayer = rememberRawSoundPlayer("wrong")

    val scannerViewModel: ScannerViewModel = viewModel(factory = ScannerViewModelFactory(ServiceLocator.scannerRepository(context)))
    val scannerUiState by scannerViewModel.uiState.collectAsState()

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
                    val rawValues = barcodes.mapNotNull { it.rawValue }
                    hasBarcodeInFrame = rawValues.isNotEmpty()
                    scannerViewModel.procesarFotograma(rawValues)
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

    val cameraManager = remember { CameraManager(context) }
    val previewView = remember { cameraManager.createPreviewView() }
    DisposableEffect(lifecycleOwner) {
        cameraManager.bind(lifecycleOwner, previewView, analysis, executor, analyzer)
        onDispose { }
    }

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
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .aspectRatio(1.65f)
                        .align(Alignment.Center)
                        .offset(y = (-20).dp)
                        .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                ) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                    ScannerFrameOverlay(showFrame = !hasBarcodeInFrame)
                }

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
