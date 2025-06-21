package com.viditnakhawa.myimageapp.ui.analysis

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.viditnakhawa.myimageapp.PostDetails
import com.viditnakhawa.myimageapp.ui.AnalysisScreen
import com.viditnakhawa.myimageapp.ui.ImageViewModel
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.ui.navigation.AppRoutes

@Composable
fun AnalysisContainerScreen(
    imageUri: Uri,
    imageViewModel: ImageViewModel,
    modelManagerViewModel: ModelManagerViewModel,
    navController: NavController,
    onAddToCollection: (Uri) -> Unit
) {

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var rawOcrResult by remember { mutableStateOf<String?>(null) }
    var analysisMessage by remember { mutableStateOf("Analyzing...") }
    val analysisResult by produceState<PostDetails?>(initialValue = null, key1 = imageUri) {
        isLoading = true
        imageViewModel.getImageDetailsFlow(imageUri).collect { entity ->
            value = entity?.let {
                PostDetails(
                    title = it.title ?: "Analyzing...",
                    content = it.content ?: "",
                    sourceApp = it.sourceApp,
                    tags = it.tags,
                    isFallback = (it.title != null && it.sourceApp == null),
                    polishedOcr = it.polishedOcr
                )
            }
            isLoading = false
        }
    }

    val isGemmaReady = modelManagerViewModel.isGemmaInitialized()

    AnalysisScreen(
        imageUri = imageUri,
        isLoading = isLoading || analysisResult == null,
        isOcrRunning = isLoading, // Re-use the same loading state for simplicity
        details = analysisResult,
        rawOcrText = rawOcrResult,
        analysisMessage = analysisMessage,
        isGemmaReady = isGemmaReady,
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
        onDelete = { uri ->
            imageViewModel.ignoreImage(uri)
            navController.popBackStack()
        },
        onRecognizeText = { uri ->
            val isGemmaReady = modelManagerViewModel.isGemmaInitialized()
            analysisMessage = if (isGemmaReady) "Polishing text with Gemma..." else "Extracting text with ML Kit..."
            isLoading = true
            imageViewModel.performOcr(uri, isGemmaReady) { rawText ->
                if (rawText != null) {
                    rawOcrResult = rawText
                }
                isLoading = false
            }
        },
        onAnalyzeWithGemma = { uri ->
            analysisMessage = "Analyzing with Gemma..."
            if (modelManagerViewModel.isGemmaInitialized()) {
                // val encodedUri = Uri.encode(uri.toString())
                // navController.navigate("${AppRoutes.CHAT}/$encodedUri")
                // --- CHAT FEATURE COMMENTED OUT AS REQUESTED ---
            } else {
                navController.navigate(AppRoutes.MODEL_MANAGER)
            }
        }
    )
}