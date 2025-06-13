import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    imageUri: Uri,
    onClose: () -> Unit,
    onShare: (Uri) -> Unit,
    onEdit: (Uri) -> Unit,
    onDelete: (Uri) -> Unit,
    onRecognizeText: (Uri) -> Unit, // For OCR
    onDescribeImage: (Uri) -> Unit,  // For Image Description
    onAnalyzeWithGemma: (Uri) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Detail") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onRecognizeText(imageUri) }) {
                        Icon(Icons.Filled.TextSnippet, contentDescription = "Recognize Text")
                    }
                    IconButton(onClick = { onDescribeImage(imageUri) }) {
                        Icon(Icons.Filled.Description, contentDescription = "Describe Image")
                    }
                    IconButton(onClick = { onAnalyzeWithGemma(imageUri) }) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Analyze with Gemma")
                    }
                    IconButton(onClick = { onDelete(imageUri) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Image")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Full screen image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}