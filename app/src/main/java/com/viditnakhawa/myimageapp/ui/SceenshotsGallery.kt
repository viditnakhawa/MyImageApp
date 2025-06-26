package com.viditnakhawa.myimageapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.ui.gallery.GalleryImageItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsGalleryScreenWithFAB(
    images: List<ImageEntity>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onViewCollectionsClick: () -> Unit,
    onCreateCollectionClick: () -> Unit,
    onCapturePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onImageClick: (ImageEntity) -> Unit,
    onSettingsClick: () -> Unit
) {
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var isTwoColumnGrid by remember { mutableStateOf(false) }
    val isAtLeastApi31 = true
    val gridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val blurRadius by remember {
        derivedStateOf {
            (scrollBehavior.state.collapsedFraction * 12).dp
        }
    }

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
            containerColor = MaterialTheme.colorScheme.background,
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
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.blur(radius = blurRadius)
                )
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyVerticalGrid(
                    columns = if (isTwoColumnGrid) GridCells.Fixed(2) else GridCells.Adaptive(minSize = 128.dp),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 96.dp // Increased padding for the bottom bar
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Collection",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(top = 12.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Screenshots",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            IconButton(onClick = { isTwoColumnGrid = !isTwoColumnGrid }) {
                                Icon(
                                    imageVector = if (isTwoColumnGrid) Icons.Default.GridView else Icons.Default.CalendarViewMonth,
                                    contentDescription = "Toggle Grid Layout",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    items(items = images, key = { it.imageUri }) { imageEntity ->
                        GalleryImageItem(
                            image = imageEntity,
                            onImageClick = onImageClick,
                            isTwoColumnLayout = isTwoColumnGrid
                        )
                    }
                }

                // This Row contains the search bar and the FAB, aligned to the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    // This Box creates the frosted glass search bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.82f) // fix width, no weight
                            .height(54.dp)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .blur(16.dp)
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Search...") },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent.copy(alpha = 0.7f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedIndicatorColor = Color.Transparent.copy(alpha = 0.7f),
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        )
                    }

                    // This is the FAB and its expandable menu
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        AnimatedFabMenu(
                            expanded = fabMenuExpanded,
                            onCapturePhotoClick = {
                                fabMenuExpanded = false
                                onCapturePhotoClick()
                            },
                            onPickFromGalleryClick = {
                                fabMenuExpanded = false
                                onPickFromGalleryClick()
                            }
                        )
                        FloatingActionButton(
                            onClick = { fabMenuExpanded = !fabMenuExpanded },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                imageVector = if (!fabMenuExpanded) Icons.Default.Add else Icons.Default.Close,
                                contentDescription = "Add",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun TooltipFab(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    labelVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        if (labelVisible) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}

@Composable
private fun AnimatedFabMenu(
    expanded: Boolean,
    onCapturePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit
) {
    var areLabelsVisible by remember { mutableStateOf(false) }
    val menuAnimationProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(expanded) {
        if (expanded) {
            areLabelsVisible = true
            menuAnimationProgress.animateTo(1f, tween(durationMillis = 250))
        } else {
            scope.launch {
                menuAnimationProgress.animateTo(0f, tween(durationMillis = 200))
                areLabelsVisible = false
            }
        }
    }

    val alpha = menuAnimationProgress.value
    val offsetY = (1 - menuAnimationProgress.value) * 20f

    if (alpha > 0f) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .alpha(alpha)
                .offset(y = offsetY.dp)
        ) {
            TooltipFab(
                icon = Icons.Default.Camera,
                label = "Camera",
                onClick = onCapturePhotoClick,
                labelVisible = areLabelsVisible
            )

            TooltipFab(
                icon = Icons.Default.PhotoLibrary,
                label = "Gallery",
                onClick = onPickFromGalleryClick,
                labelVisible = areLabelsVisible
            )
        }
    }
}
