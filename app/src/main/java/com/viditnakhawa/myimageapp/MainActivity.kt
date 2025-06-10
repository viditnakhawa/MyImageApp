package com.viditnakhawa.myimageapp

import ImageDetailScreen
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.viditnakhawa.myimageapp.ui.AnalysisScreen
import com.viditnakhawa.myimageapp.ui.ImageViewModel
import com.viditnakhawa.myimageapp.ui.ScreenshotsGalleryScreenWithFAB
import com.viditnakhawa.myimageapp.ui.theme.MyImageAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MLKitImgDescProcessor.initialize(applicationContext) // Initialize ML Kit
        setContent {
            MyImageAppTheme {
                MyImageApp()
            }
        }
    }

    @Composable
    fun MyImageApp() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Gallery) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var analysisResult by remember { mutableStateOf<PostDetails?>(null) }

        val imageList by viewModel.images.collectAsState()

        val pickMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                viewModel.addImage(uri)
            }
        }

        when (currentScreen) {
            is Screen.Gallery -> {
                ScreenshotsGalleryScreenWithFAB(
                    images = imageList,
                    // FIX: Added the missing onAddCollectionClick parameter
                    onAddCollectionClick = { /* TODO: Implement create collection logic */ },
                    onCapturePhotoClick = { currentScreen = Screen.Camera },
                    onPickFromGalleryClick = {
                        pickMediaLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onImageClick = { uri: Uri ->
                        selectedImageUri = uri
                        currentScreen = Screen.Detail
                    }
                )
            }
            is Screen.Camera -> {
                // This function is missing from your provided code, assuming it exists elsewhere
                // ComposeCameraScreen( ... )
            }
            is Screen.Detail -> {
                selectedImageUri?.let { uri ->
                    ImageDetailScreen(
                        imageUri = uri,
                        onClose = { currentScreen = Screen.Gallery },
                        onShare = { /* Implement share */ },
                        onEdit = { /* Implement edit */ },
                        onDelete = {
                            viewModel.removeImage(it)
                            currentScreen = Screen.Gallery
                        },
                        onRecognizeText = { imageUri ->
                            lifecycleScope.launch {
                                // This function is missing, assuming it exists elsewhere
                                val text = processImageWithOCR(applicationContext, imageUri)
                                //val text = "OCR processing..." // Placeholder
                                analysisResult = PostDetails(title = "Text Recognition (OCR)", content = text)
                                currentScreen = Screen.Analysis
                            }
                        },
                        onDescribeImage = { imageUri ->
                            lifecycleScope.launch {
                                // This function is missing, assuming it exists elsewhere
                                val result = MLKitImgDescProcessor.describeImage(applicationContext, imageUri)
                                //val result = PostDetails(title="Image Description", content = "Description processing...") // Placeholder
                                analysisResult = result
                                currentScreen = Screen.Analysis
                            }
                        }
                    )
                }
            }
            is Screen.Analysis -> {
                if (selectedImageUri != null && analysisResult != null) {
                    AnalysisScreen(
                        imageUri = selectedImageUri!!,
                        details = analysisResult!!,
                        onClose = { currentScreen = Screen.Detail }
                    )
                }
            }
        }
    }

    sealed class Screen {
        object Gallery : Screen()
        object Camera : Screen()
        object Detail : Screen()
        object Analysis : Screen()
    }
}