package com.viditnakhawa.myimageapp.ui.collections

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectScreenshotsScreen(
    allImages: List<Uri>,
    collectionId: Long,
    onDone: (List<Uri>) -> Unit,
    onClose: () -> Unit
) {
    var selectedUris by remember { mutableStateOf<Set<Uri>>(emptySet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select screenshots") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onDone(selectedUris.toList()) },
                        enabled = selectedUris.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = allImages, key = { it.toString() }) { uri ->
                val isSelected = selectedUris.contains(uri)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedUris = if (isSelected) {
                                selectedUris - uri
                            } else {
                                selectedUris + uri
                            }
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(uri).size(256).build(),
                        contentDescription = "Screenshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}