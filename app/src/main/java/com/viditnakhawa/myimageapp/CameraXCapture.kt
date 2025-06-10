package com.viditnakhawa.myimageapp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.AspectRatio
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ComposeCameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomLevel by remember { mutableStateOf(1f) }

    if (cameraPermissionState.status.isGranted) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // This Box acts as the top black bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = onClose, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            }

            // This is the camera viewfinder area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f) // Set to 9:16 aspect ratio
            ) {
                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
                val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
                val previewView = remember { PreviewView(context) }
                val imageCapture = remember {
                    ImageCapture.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9) // Set use case to 16:9
                        .build()
                }
                val cameraSelector = remember(lensFacing) {
                    CameraSelector.Builder().requireLensFacing(lensFacing).build()
                }
                var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

                LaunchedEffect(lensFacing) {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9) // Set use case to 16:9
                            .build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            },
                        imageCapture
                    )
                    cameraControl = camera.cameraControl
                    // Apply the current zoom level when the camera is bound
                    cameraControl?.setZoomRatio(zoomLevel)
                }

                // Camera Preview
                AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

                // Controls are now overlaid on the preview, at the bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ZoomControls(
                        currentZoom = zoomLevel,
                        onZoomChange = { newZoom ->
                            zoomLevel = newZoom
                            cameraControl?.setZoomRatio(newZoom)
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Spacer to center the shutter button
                        Spacer(modifier = Modifier.size(64.dp))

                        // Shutter Button
                        IconButton(
                            modifier = Modifier
                                .size(80.dp)
                                .border(4.dp, Color.White, CircleShape),
                            onClick = {
                                captureImage(context, imageCapture, onImageCaptured)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        }

                        // Flip Camera Button
                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            }
                        ) {
                            Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip camera", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            // This Box acts as the bottom black bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request Camera Permission")
            }
        }
    }
}

@Composable
private fun ZoomControls(currentZoom: Float, onZoomChange: (Float) -> Unit) {
    val zoomLevels = listOf(1f, 2f, 5f)
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            zoomLevels.forEach { zoom ->
                TextButton(
                    onClick = { onZoomChange(zoom) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (currentZoom == zoom) MaterialTheme.colorScheme.primary else Color.White
                    )
                ) {
                    Text(
                        text = "${zoom.toInt()}x",
                        fontWeight = if (currentZoom == zoom) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyImageApp")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri ?: return
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("CameraCapture", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}
