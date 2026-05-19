package com.splitmate.android.ui.expense

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.splitmate.android.domain.model.LineItem
import com.splitmate.android.util.ReceiptOcrExtractor

@Composable
fun OcrCameraScreen(
    onAmountExtracted: (Double, List<LineItem>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanning by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { proxy ->
                                if (!isScanning) {
                                    isScanning = true
                                    processImage(proxy) { text ->
                                        val amount = ReceiptOcrExtractor.extractAmount(text)
                                        val items = ReceiptOcrExtractor.extractLineItems(text)
                                        if (amount != null) {
                                            onAmountExtracted(amount, items)
                                        }
                                        isScanning = false
                                    }
                                }
                                proxy.close()
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview, imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Point at the total amount on the bill",
                color = Color.White, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImage(
    proxy: ImageProxy,
    onResult: (String) -> Unit
) {
    val mediaImage = proxy.image ?: return
    val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        .process(image)
        .addOnSuccessListener { result -> onResult(result.text) }
        .addOnFailureListener {
            // Handle error, proxy already closed in analyzer
        }
}