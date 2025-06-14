package com.viditnakhawa.myimageapp.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.PostDetails

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnalysisScreen(
    imageUri: Uri,
    details: PostDetails?, // Parameter is correctly nullable
    isLoading: Boolean,    // Parameter to control loading state
    onClose: () -> Unit,
    onImageClick: (Uri) -> Unit,
    onAddToCollection: () -> Unit,
    onShare: (Uri) -> Unit,
    onDelete: (Uri) -> Unit,
    onRecognizeText: (Uri) -> Unit,
    onAnalyzeWithGemma: (Uri) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Details") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddToCollection) { /* ... */ }
                    IconButton(onClick = { onShare(imageUri) }) { /* ... */ }
                    IconButton(onClick = { onDelete(imageUri) }) { /* ... */ }
                }
            )
        }
    ) { innerPadding ->
        // This outer column now handles the main loading state
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                // Show a loading spinner if isLoading is true
                Spacer(modifier = Modifier.height(64.dp))
                CircularProgressIndicator()
            } else if (details != null) {
                // Only show the content if not loading AND details are available
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(32.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(imageUri).crossfade(true).build(),
                            contentDescription = "Analyzed Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clickable { onImageClick(imageUri) }
                        )
                        Column(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            TooltipFab(icon = Icons.Default.TextSnippet, label = "OCR", onClick = { onRecognizeText(imageUri) })
                            TooltipFab(icon = Icons.Default.AutoAwesome, label = "Gemma", onClick = { onAnalyzeWithGemma(imageUri) })
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    if (details.isFallback) {
                        Card(
                            modifier = Modifier.padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Upgrade",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Get richer titles, tags, and summaries. Go to 'Manage Model' to download and initialize Gemma.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (details.sourceApp != null && details.sourceApp != "Unknown") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = "Source App",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = details.sourceApp,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(text = details.title, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (!details.tags.isNullOrEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    details.tags.forEach { tag ->
                                        SuggestionChip(onClick = { /* TODO */ }, label = { Text(tag) })
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Text(
                                text = details.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun TooltipFab(
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
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
            )
        ) {
            Text(text = label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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

