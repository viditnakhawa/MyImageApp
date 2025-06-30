package com.viditnakhawa.myimageapp.ui.collections

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.ui.gallery.GalleryImageCard
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes
import com.viditnakhawa.myimageapp.ui.viewmodels.ImageViewModel
import com.viditnakhawa.myimageapp.ui.viewmodels.ModelManagerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    navController: NavController,
    imageViewModel: ImageViewModel,
    modelManagerViewModel: ModelManagerViewModel,
    collectionId: Long
) {
    val collectionWithImages by imageViewModel.getCollectionById(collectionId)
        .collectAsStateWithLifecycle(null)
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableIntStateOf(2) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedImageUris by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember(collectionWithImages) {
        mutableStateOf(collectionWithImages?.collection?.name ?: "")
    }

    LaunchedEffect(isSelectionMode) {
        if (!isSelectionMode) {
            selectedImageUris = emptySet()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    collectionWithImages?.collection?.name?.let {
                        Text(it, maxLines = 1)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        Text(
                            "${selectedImageUris.size} selected",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = {
                            imageViewModel.removeImagesFromCollection(
                                selectedImageUris.map { it.toString() },
                                collectionId
                            )
                            isSelectionMode = false
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove Selected Images")
                        }
                        IconButton(onClick = { isSelectionMode = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit Selection Mode")
                        }
                    } else {
                        IconButton(onClick = { gridColumns = if (gridColumns == 2) 3 else 2 }) {
                            Icon(
                                if (gridColumns == 2) Icons.Default.GridView else Icons.Default.ViewAgenda,
                                contentDescription = "Toggle Grid Layout"
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Title") },
                                    onClick = {
                                        newCollectionName = collectionWithImages?.collection?.name ?: ""
                                        showEditTitleDialog = true
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add More Images") },
                                    onClick = {
                                        navController.navigate(AppRoutes.selectScreenshotsScreen(collectionId))
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove Images") },
                                    onClick = {
                                        isSelectionMode = true
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Collection") },
                                    onClick = {
                                        showDeleteDialog = true
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        collectionWithImages?.let {
            if (it.images.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("This collection is empty.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(it.images, key = { image -> image.imageUri }) { image ->
                        val currentImageUri = image.imageUri.toUri()
                        val isSelected = selectedImageUris.contains(image.imageUri.toUri())

                        val onItemClickAction = {
                            if (isSelectionMode) {
                                selectedImageUris = if (isSelected) {
                                    selectedImageUris - currentImageUri
                                } else {
                                    selectedImageUris + currentImageUri
                                }
                            } else {
                                val galleryIndex = imageViewModel.searchedImages.value.indexOf(image)
                                if (galleryIndex != -1) {
                                    imageViewModel.prepareForAnalysis(currentImageUri, modelManagerViewModel.isGemmaInitialized())
                                    navController.navigate(AppRoutes.analysisScreen(galleryIndex))
                                }
                            }
                        }

                        if (gridColumns == 2 && !isSelectionMode) {
                            GalleryImageCard(
                                image = image,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable { onItemClickAction() }
                            )
                        } else {
                            GridItem(
                                image = image,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onItemClick = { onItemClickAction() }
                            )
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Collection") },
            text = { Text("Are you sure you want to permanently delete this collection? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        imageViewModel.deleteCollection(collectionId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditTitleDialog) {
        AlertDialog(
            onDismissRequest = { showEditTitleDialog = false },
            title = { Text("Edit Title") },
            text = {
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    label = { Text("Collection Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        imageViewModel.updateCollectionName(collectionId, newCollectionName)
                        showEditTitleDialog = false
                    },
                    enabled = newCollectionName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitleDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GridItem(
    image: ImageEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onItemClick: (ImageEntity) -> Unit
) {
    var showTitleOverlay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitleOverlay) 1f else 0f,
        animationSpec = tween(300),
        label = "TitleAlpha"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(image.imageUri) {
                detectTapGestures(
                    onTap = { onItemClick(image) },
                    onDoubleTap = {
                        if (image.title != null) {
                            scope.launch {
                                showTitleOverlay = true
                                delay(3000L) // Show for 3 seconds
                                showTitleOverlay = false
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.imageUri.toUri())
                .crossfade(true)
                .build(),
            contentDescription = "Collection Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Selection overlay logic
        AnimatedVisibility(visible = isSelectionMode, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else Color.Black.copy(alpha = 0.3f)
                    )
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Title overlay logic (from double-tap)
        if (titleAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(titleAlpha)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = image.title.orEmpty(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}