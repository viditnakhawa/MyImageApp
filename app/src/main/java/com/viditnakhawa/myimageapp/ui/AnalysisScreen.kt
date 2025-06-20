package com.viditnakhawa.myimageapp.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.PostDetails

private val AppBackground = Color(0xFFF2F2F2)
private val CardColor = Color(0xFFCBCBCB)
private val UpgradeCardColor = Color(0xFF174D38)
private val TagBackground = Color(0xFF4D1717)
private val TagText = Color(0xFFF2F2F2)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnalysisScreen(
    imageUri: Uri,
    details: PostDetails?,
    isLoading: Boolean,
    isOcrRunning: Boolean,
    onClose: () -> Unit,
    onImageClick: (Uri) -> Unit,
    onAddToCollection: () -> Unit,
    onShare: (Uri) -> Unit,
    onDelete: (Uri) -> Unit,
    onRecognizeText: (Uri) -> Unit,
    onAnalyzeWithGemma: (Uri) -> Unit,
    rawOcrText: String?
) {
    var noteText by remember { mutableStateOf("") }
    var isNoteEditable by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = onAddToCollection,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add to Collection",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(
                            onClick = { onShare(imageUri) },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(
                            onClick = { onDelete(imageUri) },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(AppBackground)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardColor,
                        contentColor = Color.Black
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyzing...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else if (details != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Analyzed Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(imageUri) }
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        TooltipFab(
                            icon = Icons.AutoMirrored.Filled.TextSnippet,
                            label = "OCR",
                            onClick = { onRecognizeText(imageUri) })
                        TooltipFab(
                            icon = Icons.Default.AutoAwesome,
                            label = "Gemma",
                            onClick = { onAnalyzeWithGemma(imageUri) })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                if (details.isFallback) {
                    UpgradeCard()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardColor,
                        contentColor = Color.Black
                    )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        if (!details.sourceApp.isNullOrBlank() && details.sourceApp != "Unknown") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = "Source App"
                                )
                                Text(
                                    text = details.sourceApp,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            text = details.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        if (!details.tags.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                details.tags.forEach { tag ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(tag) },
                                        shape = RoundedCornerShape(50),
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = TagBackground,
                                            labelColor = TagText
                                        )
                                    )
                                }
                            }
                        }

                        if (details.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            SelectionContainer {
                                Text(
                                    text = details.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // This card will appear automatically once the polishedOcr field is saved to the database.
                if (!details?.polishedOcr.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Polished Text (Gemma)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SelectionContainer {
                                Text(
                                    text = details!!.polishedOcr!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                if (rawOcrText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Raw Text (ML Kit)", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            SelectionContainer {
                                Text(rawOcrText)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                //NOTE CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { isNoteEditable = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (isNoteEditable) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    placeholder = { Text("Write a note...") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { isNoteEditable = false },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save Note",
                                        tint = Color.White
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = if (noteText.isNotBlank()) noteText else "Note",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun UpgradeCard() {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = UpgradeCardColor,
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.WorkspacePremium, contentDescription = "Upgrade")
            Text(
                text = "Get richer titles, tags, and summaries. Go to 'Manage Model' to download and initialize Gemma.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
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
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Text(text = label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = TagBackground
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
    }
}
