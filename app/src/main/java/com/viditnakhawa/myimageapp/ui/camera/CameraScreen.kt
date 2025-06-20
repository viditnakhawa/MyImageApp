package com.viditnakhawa.myimageapp.ui.camera

import android.net.Uri
import androidx.compose.runtime.Composable
import com.viditnakhawa.myimageapp.ComposeCameraScreen // Assuming this composable exists from your old MainActivity

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    // This is a simple wrapper around the camera composable you already have
    ComposeCameraScreen(
        onImageCaptured = onImageCaptured,
        onClose = onClose
    )
}