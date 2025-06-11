package com.viditnakhawa.myimageapp.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.viditnakhawa.myimageapp.R

@Composable
fun ScreenshotsGalleryScreenWithFAB(
    images: List<Uri>,
    onAddCollectionClick: () -> Unit,
    onCapturePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onImageClick: (Uri) -> Unit,
    onManageModelClick: () -> Unit
) {
    var fabMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            // Use a Column to stack the FABs vertically
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. "Manage Model" Squircle Floating Action Button
                FloatingActionButton(
                    onClick = onManageModelClick,
                    shape = RoundedCornerShape(16.dp), // This creates the "squircle" shape
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(),
                    modifier = Modifier.size(58.dp) // A smaller size for a secondary FAB
                ) {
                    // 2. Use painterResource to load your PNG icon.
                    // in your app/src/main/res/drawable folder.
                    Icon(
                        painter = painterResource(id = R.drawable.gemma_color),
                        contentDescription = "Manage Model",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                // 3. Original FAB with dropdown menu
                Box {
                    FloatingActionButton(
                        onClick = { fabMenuExpanded = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = fabMenuExpanded,
                        onDismissRequest = { fabMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Capture Photo") },
                            onClick = {
                                fabMenuExpanded = false
                                onCapturePhotoClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pick from Gallery") },
                            onClick = {
                                fabMenuExpanded = false
                                onPickFromGalleryClick()
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // This Column now fills the entire available space provided by the Scaffold
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Screenshots",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )

                AssistChip(
                    onClick = onAddCollectionClick,
                    label = { Text("Create Collection") }
                )
            }

            // Image Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                // By using .weight(1f), the grid takes up all the remaining space
                // in the Column after the Row is measured, which prevents the crash.
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { imageUri ->
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Screenshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(imageUri) }
                    )
                }
            }
        }
    }
}
