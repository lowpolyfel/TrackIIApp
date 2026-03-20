package com.ttelectronics.trackiiapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import androidx.compose.animation.Crossfade
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Description
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.core.camera.CameraManager
import com.ttelectronics.trackiiapp.ui.components.PermissionFallback
import com.ttelectronics.trackiiapp.ui.components.ScanResultOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerFrameOverlay
import com.ttelectronics.trackiiapp.ui.components.ScannerControlsPanel
import com.ttelectronics.trackiiapp.ui.components.ScannerHeader
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.rememberRawSoundPlayer
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ScannerNavigationTarget
import com.ttelectronics.trackiiapp.ui.viewmodel.ScannerViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ScannerViewModelFactory
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

private const val SCAN_WINDOW_LEFT = 0f
private const val SCAN_WINDOW_TOP = 0f
private const val SCAN_WINDOW_RIGHT = 1f
private const val SCAN_WINDOW_BOTTOM = 1f
private const val INSTRUCTION_DURATION = 3000
private const val SHEET_LOOP_DURATION_MS = 2500

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    taskType: TaskType,
    onBack: () -> Unit,
    onComplete: (String, String, Boolean, String) -> Unit,
    onReworkTask: (String, String) -> Unit,
    onReworkRelease: (String, String) -> Unit,
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
        delay(INSTRUCTION_DURATION.toLong())
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
    val normalizedPartNumber = partNumber.trim().uppercase().replace(" ", "")
    val canValidate = lotNumber.isNotBlank() && partNumber.isNotBlank()

    LaunchedEffect(canValidate) {
        if (canValidate && !scannerUiState.isValidating && !showResultOverlay) {
            scannerViewModel.validateForTask(taskType, lotNumber, normalizedPartNumber)
        }
    }

    LaunchedEffect(scannerUiState.isLotFound, scannerUiState.isPartFound) {
        if (scannerUiState.isLotFound || scannerUiState.isPartFound) {
            scanSoundPlayer.play()
            scannerViewModel.consumeScanEffects()
        }
    }

    val orderFoundText = stringResource(R.string.order_found)
    val orderNotFoundText = stringResource(R.string.error_order_not_found_for_part)
    val validationErrorText = scannerUiState.validationError?.let { stringResource(id = it) }
    LaunchedEffect(scannerUiState.shouldNavigate) {
        if (scannerUiState.shouldNavigate) {
            when (scannerUiState.navigationTarget) {
                ScannerNavigationTarget.ReworkTask -> {
                    overlaySuccess = true
                    overlayText = orderFoundText
                    showResultOverlay = true
                    rightSoundPlayer.play()
                    delay(1000)
                    onReworkTask(lotNumber, normalizedPartNumber)
                }

                ScannerNavigationTarget.ReworkRelease -> {
                    overlaySuccess = true
                    overlayText = orderFoundText
                    showResultOverlay = true
                    rightSoundPlayer.play()
                    delay(1000)
                    onReworkRelease(lotNumber, normalizedPartNumber)
                }

                ScannerNavigationTarget.ScanReview -> {
                    if (scannerUiState.isProductFound) {
                        overlaySuccess = true
                        overlayText = orderFoundText
                        showResultOverlay = true
                        rightSoundPlayer.play()
                        delay(1000)
                        onComplete(lotNumber, normalizedPartNumber, true, "")
                    } else {
                        overlaySuccess = false
                        overlayText = scannerUiState.customValidationMessage
                            ?: validationErrorText // <-- USAMOS LA VARIABLE YA EXTRAÍDA
                                    ?: orderNotFoundText
                        showResultOverlay = true
                        wrongSoundPlayer.play()
                        delay(1300)
                        onComplete(lotNumber, normalizedPartNumber, false, overlayText)
                    }
                }
            }
            scannerViewModel.consumeNavigation()
        }
    }

    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = (-30).dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScannerHeader(taskTitle = taskType.title)

            Crossfade(
                targetState = showInstructions,
                animationSpec = tween(durationMillis = 800),
                modifier = Modifier.weight(1f),
                label = "scanner_transition"
            ) { isShowingInstructions ->
                if (isShowingInstructions) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null, // Esto elimina por completo la animación gris de clic
                                onClick = { showInstructions = false }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ScannerInstructionsScreen()
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (hasCameraPermission) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CameraScanPanel(
                                    modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f),
                                    onRawValuesDetected = { rawValues ->
                                        hasBarcodeInFrame = rawValues.isNotEmpty()
                                        scannerViewModel.procesarFotograma(rawValues)
                                    },
                                    onScanFailure = { hasBarcodeInFrame = false },
                                    hasBarcodeInFrame = hasBarcodeInFrame
                                )
                            }

                            ScannerControlsPanel(
                                lotNumber = lotNumber,
                                partNumber = partNumber,
                                isValidating = scannerUiState.isValidating,
                                canValidate = canValidate,
                                onReset = {
                                    showResultOverlay = false
                                    overlayText = ""
                                    scannerViewModel.resetScan()
                                },
                                onBack = onBack
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                PermissionFallback(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
                            }
                        }
                    }
                }
            }
        }

        com.ttelectronics.trackiiapp.ui.components.LoadingOverlay(visible = scannerUiState.isValidating)

        ScanResultOverlay(visible = showResultOverlay, success = overlaySuccess, message = overlayText)
    }
}

@Composable
private fun ScannerInstructionsScreen() {
    val transition = rememberInfiniteTransition(label = "instruction")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHEET_LOOP_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "instructionProgress"
    )
    val fastProgress = (progress * 1.45f).coerceIn(0f, 1f)
    val sheetOffset = (-56f) + (fastProgress * 124f)
    val sheetAlpha = when {
        fastProgress < 0.18f -> (fastProgress / 0.18f).coerceIn(0f, 1f)
        fastProgress > 0.82f -> ((1f - fastProgress) / 0.18f).coerceIn(0f, 1f)
        else -> 1f
    }

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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFE9F4FF),
                            Color(0xFFD8EAFF)
                        )
                    )
                )
                .border(1.dp, Color(0xFFBCD8F5), RoundedCornerShape(22.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Description,
                contentDescription = null,
                tint = TTGreen.copy(alpha = sheetAlpha),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(88.dp)
                    .offset(y = sheetOffset.dp)
            )
            Icon(
                imageVector = Icons.Rounded.TabletAndroid,
                contentDescription = null,
                tint = Color(0xFF2268AE),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(124.dp)
            )
            Icon(
                imageVector = Icons.Rounded.DocumentScanner,
                contentDescription = null,
                tint = Color(0xFF1A5EA6),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-6).dp)
                    .size(44.dp)
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
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_QR_CODE
            )
            .build()
        BarcodeScanning.getClient(options)
    }
    val analysis = remember {
        ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val analyzer = remember {
        ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image ?: run { imageProxy.close(); return@Analyzer }
            val cropRect = buildCropRect(imageProxy)
            mediaImage.setCropRect(cropRect)
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener(mainExecutor) { barcodes ->
                    val acceptedRawValues = barcodes
                        .filter { barcode ->
                            val bounds = barcode.boundingBox ?: return@filter false
                            Rect.intersects(bounds, cropRect)
                        }
                        .mapNotNull { it.rawValue }
                    onRawValuesDetected(acceptedRawValues)
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
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        ScannerFrameOverlay(showFrame = !hasBarcodeInFrame)
    }
}


private fun buildCropRect(imageProxy: ImageProxy): Rect {
    val imageWidth = imageProxy.width
    val imageHeight = imageProxy.height
    val left = (imageWidth * SCAN_WINDOW_LEFT).toInt()
    val top = (imageHeight * SCAN_WINDOW_TOP).toInt()
    val right = (imageWidth * SCAN_WINDOW_RIGHT).toInt().coerceAtMost(imageWidth)
    val bottom = (imageHeight * SCAN_WINDOW_BOTTOM).toInt().coerceAtMost(imageHeight)
    return Rect(left, top, right, bottom)
}
