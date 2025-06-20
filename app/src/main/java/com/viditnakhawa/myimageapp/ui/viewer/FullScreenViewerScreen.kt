package com.viditnakhawa.myimageapp.ui.viewer

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun FullScreenViewerScreen(imageUri: Uri, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imageUri).build(),
            contentDescription = "Full screen image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 52.dp, start = 20.dp)
                .background(Color.Black.copy(alpha = 0.7f), shape = CircleShape)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}