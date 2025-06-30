/*package com.viditnakhawa.myimageapp.ui.analysis

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.PostDetails
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes
import com.viditnakhawa.myimageapp.ui.viewmodels.ImageViewModel
import com.viditnakhawa.myimageapp.ui.viewmodels.ModelManagerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalysisContainerScreen(
    initialPage: Int,
    imageViewModel: ImageViewModel,
    modelManagerViewModel: ModelManagerViewModel,
    navController: NavController,
    onAddToCollection: (Uri) -> Unit
) {
    val context = LocalContext.current
    val images by imageViewModel.searchedImages.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = initialPage) { images.size }

    if (images.isNotEmpty()) {
        HorizontalPager(
            state = pagerState,
            key = { index -> images[index].imageUri }
        ) { pageIndex ->
            val imageEntity = images[pageIndex]
            val imageUri = Uri.parse(imageEntity.imageUri)

            // ---- State management from single screen mode ----
            var isLoading by remember(imageUri) { mutableStateOf(false) }
            var rawOcrResult by remember(imageUri) { mutableStateOf<String?>(null) }
            var analysisMessage by remember(imageUri) { mutableStateOf("Analyzing...") }
            var showDeleteDialog by remember(imageUri) { mutableStateOf(false) }

            val imageRequest = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(true)
                .interruptionHandling(false) // Add this line
                .build()

            val detailedEntity by imageViewModel.getImageDetailsFlow(imageUri).collectAsState(initial = null)
            val analysisResult by produceState<PostDetails?>(initialValue = null, key1 = imageUri) {
                isLoading = true
                imageViewModel.getImageDetailsFlow(imageUri).collect { entity ->
                    value = entity?.let {
                        PostDetails(
                            title = when {
                                it.title.isNullOrBlank() && it.content?.startsWith("Model unavailable") == true -> "Analysis Failed"
                                it.title.isNullOrBlank() && it.content?.startsWith("Could not prepare") == true -> "Analysis Failed"
                                it.title.isNullOrBlank() -> "Analysis in progress..."
                                else -> it.title!!
                            },
                            content = it.content ?: "",
                            sourceApp = it.sourceApp,
                            tags = it.tags,
                            isFallback = (it.title != null && it.sourceApp == null),
                            polishedOcr = it.polishedOcr
                        )
                    } ?: PostDetails(title = "Loading image...", content = "")
                    isLoading = false
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Screenshot") },
                    text = { Text("This will permanently delete the screenshot and all of its associated analysis from the app. This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                imageViewModel.deleteImage(imageUri)
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

            val isGemmaReady = modelManagerViewModel.isGemmaInitialized()

            AnalysisScreen(
                imageRequest = imageRequest,
                imageUri = imageUri,
                isLoading = isLoading || analysisResult == null,
                isOcrRunning = isLoading,
                details = analysisResult,
                note = detailedEntity?.note ?: "",
                rawOcrText = rawOcrResult,
                analysisMessage = analysisMessage,
                isGemmaReady = isGemmaReady,
                onSaveNote = { newNote -> imageViewModel.updateImageNote(imageUri, newNote) },
                onClose = { navController.popBackStack() },
                onImageClick = { uri -> navController.navigate(AppRoutes.viewerScreen(Uri.encode(uri.toString()))) },
                onAddToCollection = { onAddToCollection(imageUri) },
                onShare = { uri ->
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "image/jpeg"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
                },
                onDelete = { showDeleteDialog = true },
                onRecognizeText = { uri ->
                    analysisMessage = if (isGemmaReady) {
                        "Gemma is reading the image..."
                    } else {
                        "Extracting text with ML Kit..."
                    }
                    isLoading = true
                    imageViewModel.performOcrOnImage(uri, isGemmaReady, modelManagerViewModel)
                },
                onAnalyzeWithGemma = { uri ->
                    analysisMessage = "Analyzing with Gemma..."
                    if (isGemmaReady) {
                        // val encodedUri = Uri.encode(uri.toString())
                        // navController.navigate("${AppRoutes.CHAT}/$encodedUri")
                        // --- CHAT FEATURE COMMENTED OUT AS REQUESTED ---
                    } else {
                        navController.navigate(AppRoutes.MODEL_MANAGER)
                    }
                }
            )
        }
    }
}*/


package com.viditnakhawa.myimageapp.ui.analysis

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.viditnakhawa.myimageapp.PostDetails
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes
import com.viditnakhawa.myimageapp.ui.viewmodels.ImageViewModel
import com.viditnakhawa.myimageapp.ui.viewmodels.ModelManagerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalysisContainerScreen(
    initialPage: Int,
    imageViewModel: ImageViewModel,
    modelManagerViewModel: ModelManagerViewModel,
    navController: NavController,
    onAddToCollection: (Uri) -> Unit
) {
    val images by imageViewModel.searchedImages.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = initialPage) {
        images.size
    }

    val context = LocalContext.current
    val isGemmaReady = modelManagerViewModel.isGemmaInitialized()

    if (images.isNotEmpty()) {
        HorizontalPager(
            state = pagerState,
            key = { index -> images[index].imageUri }
        ) { pageIndex ->

            val imageEntity = images[pageIndex]
            val imageUri = imageEntity.imageUri.toUri()


            AnalysisScreen(
                imageUri = imageUri,
                details = imageEntity.toPostDetails(),
                isLoading = (imageEntity.title == null),
                isOcrRunning = false, // This can be enhanced later if needed.
                note = imageEntity.note ?: "",
                onSaveNote = { newNote ->
                    imageViewModel.updateImageNote(imageUri, newNote)
                },
                rawOcrText = imageEntity.polishedOcr,
                analysisMessage = "Analyzing...",
                isGemmaReady = isGemmaReady,
                onClose = { navController.popBackStack() },
                onImageClick = { uri -> navController.navigate(AppRoutes.viewerScreen(Uri.encode(uri.toString()))) },
                onAddToCollection = { onAddToCollection(imageUri) },
                onShare = { uri ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "image/jpeg"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
                },
                onDelete = { uri ->
                    imageViewModel.ignoreImage(uri)
                    navController.popBackStack()
                },
                onRecognizeText = { uri ->
                    imageViewModel.performOcrOnImage(uri, isGemmaReady, modelManagerViewModel)
                },
                onAnalyzeWithGemma = { uri ->
                    if (isGemmaReady) {
                        // Future chat feature can be implemented here.
                        imageViewModel.forceReanalyzeImage(uri)
                    } else {
                        navController.navigate(AppRoutes.MODEL_MANAGER)
                    }
                }
            )
        }
    }
}

private fun ImageEntity.toPostDetails(): PostDetails {
    return PostDetails(
        title = this.title ?: "Analysis in progress...",
        content = this.content ?: "",
        sourceApp = this.sourceApp,
        tags = this.tags,
        isFallback = (this.title != null && this.sourceApp == null),
        polishedOcr = this.polishedOcr
    )
}