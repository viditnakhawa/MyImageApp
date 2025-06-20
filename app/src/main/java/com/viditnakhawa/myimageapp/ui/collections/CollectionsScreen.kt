package com.viditnakhawa.myimageapp.ui.collections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.data.CollectionWithImages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    collections: List<CollectionWithImages>,
    onNavigateBack: () -> Unit,
    // onCollectionClick: (Long) -> Unit // To view a collection's details later
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(collections) { collection ->
                CollectionCard(collection)
            }
        }
    }
}

@Composable
private fun CollectionCard(collectionWithImages: CollectionWithImages) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = { /* TODO: Navigate to collection detail view */ }
    ) {
        Column {
            // Row of preview images
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                collectionWithImages.images.take(3).forEach { image ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(image.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                // Fill remaining space if fewer than 3 images
                repeat(3 - collectionWithImages.images.size) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }
            // Collection name and count
            Row(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = collectionWithImages.collection.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${collectionWithImages.images.size} screenshots",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}