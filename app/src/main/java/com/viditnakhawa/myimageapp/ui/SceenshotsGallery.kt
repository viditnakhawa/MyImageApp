package com.viditnakhawa.myimageapp.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsGalleryScreenWithFAB(
    images: List<Uri>,
    onViewCollectionsClick: () -> Unit,
    onCreateCollectionClick: () -> Unit,
    onCapturePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onImageClick: (Uri) -> Unit,
    onSettingsClick: () -> Unit
) {
    var fabMenuExpanded by remember { mutableStateOf(false) }
    val isAtLeastApi31 = true
    val gridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Box(modifier = Modifier.fillMaxSize()) {
        if (fabMenuExpanded) {
            val blurModifier = if (isAtLeastApi31) Modifier.blur(12.dp) else Modifier
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .then(blurModifier)
                    .clickable { fabMenuExpanded = false }
            )
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "SnapSuite",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animated visibility for the expanded FAB options
                    AnimatedVisibility(
                        visible = fabMenuExpanded,
                        enter = fadeIn() + expandIn(expandFrom = Alignment.BottomEnd),
                        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomEnd)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TooltipFab(
                                icon = Icons.Default.Camera,
                                label = "Capture Photo",
                                onClick = {
                                    fabMenuExpanded = false
                                    onCapturePhotoClick()
                                }
                            )
                            TooltipFab(
                                icon = Icons.Default.PhotoLibrary,
                                label = "Pick from Gallery",
                                onClick = {
                                    fabMenuExpanded = false
                                    onPickFromGalleryClick()
                                }
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = { fabMenuExpanded = !fabMenuExpanded },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                state = gridState,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 80.dp // Padding for the FAB
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // The large title is now part of the TopAppBar and will collapse on scroll.
                // We no longer need a separate header item for it here.

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(28.dp))
                                .clickable { onViewCollectionsClick() }
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "My Collections",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onCreateCollectionClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Collection",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Screenshots",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 8.dp)
                    )
                }

                items(items = images, key = { it.toString() }) { imageUri ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .size(256)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Screenshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .clickable { onImageClick(imageUri) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TooltipFab(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            )
        ) {
            Text(text = label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun ScreenshotsGalleryScreenPreview() {
//    MaterialTheme {
//        ScreenshotsGalleryScreenWithFAB(
//            images = listOf(),
//            onViewCollectionsClick = {},
//            onCreateCollectionClick = {},
//            onCapturePhotoClick = {},
//            onPickFromGalleryClick = {},
//            onImageClick = {},
//            onSettingsClick = {}
//        )
//    }
//}
