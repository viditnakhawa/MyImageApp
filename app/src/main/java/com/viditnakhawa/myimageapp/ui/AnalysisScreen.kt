package com.viditnakhawa.myimageapp.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.PostDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    imageUri: Uri,
    details: PostDetails,
    onClose: () -> Unit,
    onImageClick: (Uri) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(details.title) },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Add horizontal padding
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            //REMEMBER THIS GIVES YOUR IMAGE ROUNDED APPEARANCE
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Analyzed Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)) // Apply rounded corners
                    .clickable { onImageClick(imageUri) } // Make the image clickable
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Display the AI Summary in its own section
            Column {
                Text(
                    text = details.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                // You can add more sophisticated parsing here for bullet points later
                Text(
                    text = details.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}