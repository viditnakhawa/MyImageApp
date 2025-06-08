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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
        Box(modifier = Modifier.fillMaxSize()) {
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
            val previewView = remember { PreviewView(context) }
            val imageCapture = remember {
                ImageCapture.Builder()
                    .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
                    .build()
            }
            val cameraSelector = remember(lensFacing) {
                CameraSelector.Builder().requireLensFacing(lensFacing).build()
            }
            var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

            LaunchedEffect(lensFacing, zoomLevel) {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    Preview.Builder()
                        .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_16_9)
                        .build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        },
                    imageCapture
                )
                cameraControl = camera.cameraControl
                cameraControl?.setZoomRatio(zoomLevel)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black),
                    contentAlignment = Alignment.TopStart
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        ZoomLevelSelector(
                            cameraControl = cameraControl,
                            availableZoomRatios = listOf(1f, 2f),
                            currentZoom = zoomLevel,
                            onZoomChanged = { newZoom -> zoomLevel = newZoom }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(64.dp))

                        IconButton(
                            modifier = Modifier
                                .size(80.dp)
                                .border(4.dp, Color.White, CircleShape),
                            onClick = {
                                captureImage(context, imageCapture, onImageCaptured)
                            },
                            content = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                        )

                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Text(text = "⟲", color = Color.White, fontSize = 24.sp)
                        }
                    }
                }
            }
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
fun ZoomLevelSelector(
    cameraControl: androidx.camera.core.CameraControl?,
    availableZoomRatios: List<Float>,
    currentZoom: Float,
    onZoomChanged: (Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableZoomRatios.forEach { zoomRatio ->
                TextButton(
                    onClick = {
                        cameraControl?.setZoomRatio(zoomRatio)
                        onZoomChanged(zoomRatio)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (currentZoom == zoomRatio) Color.White else Color.Gray
                    ),
                    modifier = Modifier
                        .background(
                            if (currentZoom == zoomRatio) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "${zoomRatio.toInt()}x",
                        fontWeight = if (currentZoom == zoomRatio) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
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
