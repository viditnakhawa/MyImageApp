package com.viditnakhawa.myimageapp

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.viditnakhawa.myimageapp.ui.AnalysisScreen
import com.viditnakhawa.myimageapp.ui.ImageViewModel
import com.viditnakhawa.myimageapp.ui.ModelManagerScreen
import com.viditnakhawa.myimageapp.ui.ScreenshotsGalleryScreenWithFAB
import com.viditnakhawa.myimageapp.ui.theme.MyImageAppTheme
import kotlinx.coroutines.launch
import com.viditnakhawa.myimageapp.ui.common.ViewModelProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip

class MainActivity : ComponentActivity() {

    private val viewModel: ImageViewModel by viewModels { ViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MLKitImgDescProcessor.initialize(applicationContext) // Initialize ML Kit
        // --- AUTOMATIC IMPORT ---
        // On app start, refresh the image list from the device's screenshots folder
        lifecycleScope.launch {
            (application as MyApplication).container.imageRepository.refreshImagesFromDevice(applicationContext)
        }
        setContent {
            MyImageAppTheme {
                //MyImageApp()
                PermissionWrapper()
            }
        }
    }
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun PermissionWrapper() {
        // Define the permission we need based on Android version
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val permissionState = rememberPermissionState(permission)

        if (permissionState.status.isGranted) {
            // If permission is granted, launch the scan ONCE
            LaunchedEffect(Unit) {
                (application as MyApplication).container.imageRepository.refreshImagesFromDevice(applicationContext)
            }
            // And show the main app
            MyImageApp()
        } else {
            // If permission is not granted, show a button to request it
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Permission needed to access screenshots.")
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MyImageApp() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Gallery) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var analysisResult by remember { mutableStateOf<PostDetails?>(null) }
        var isLoadingAnalysis by remember { mutableStateOf(false) } // To show a loading spinner

        val imageList by viewModel.images.collectAsState()

        val pickMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            // The result is now a list of URIs
            if (uris.isNotEmpty()) {
                // --- THE FIX IS HERE ---
                // Before adding the images to our database, we must take
                // persistent permission for each one.
                try {
                    val contentResolver = applicationContext.contentResolver
                    uris.forEach { uri ->
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                } catch (e: SecurityException) {
                    Log.e("MainActivity", "Failed to take persistent permission for URIs", e)
                    // Optionally, show a toast to the user
                    Toast.makeText(this, "Failed to get image permissions.", Toast.LENGTH_SHORT).show()
                }

                // Now, add the images to the ViewModel
                viewModel.addImages(uris)
            }
        }

        when (currentScreen) {
            is Screen.Gallery -> {
                ScreenshotsGalleryScreenWithFAB(
                    images = imageList,
                    onAddCollectionClick = { /* TODO: Implement create collection logic */ },
                    onCapturePhotoClick = { currentScreen = Screen.Camera },
                    onPickFromGalleryClick = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onImageClick = { uri ->
                        // When an image is clicked in the gallery:
                        // 1. Set the selected image URI.
                        selectedImageUri = uri
                        // 2. Set loading state to true.
                        isLoadingAnalysis = true
                        // 3. Immediately navigate to the Analysis screen.
                        currentScreen = Screen.Analysis
                        // 4. Launch a coroutine to fetch the analysis in the background.
                        lifecycleScope.launch {
                            // The analysis will be ready when the user sees the screen.
                            analysisResult = MLKitImgDescProcessor.describeImage(applicationContext, uri)
                            isLoadingAnalysis = false // Analysis is done, hide spinner.
                        }
                    },
                    onManageModelClick = {
                        currentScreen = Screen.ModelManager
                    }
                )
            }
            is Screen.Camera -> {
                ComposeCameraScreen(
                    onImageCaptured = { uri ->
                        viewModel.addImage(uri)
                        currentScreen = Screen.Gallery
                    },
                    onClose = {
                        currentScreen = Screen.Gallery
                    }
                )
            }
            is Screen.Analysis -> {
                // This is our new, polished detail screen.
                if (selectedImageUri != null && analysisResult != null) {
                    AnalysisScreen(
                        imageUri = selectedImageUri!!,
                        details = analysisResult!!,
                        onClose = { currentScreen = Screen.Gallery },
                        // When the image itself is tapped, go to the full-screen viewer
                        onAddToCollection = {/*TODO*/},
                        onShare = {/*TODO*/},
                        onDelete = {
                            viewModel.removeImage(it)
                            currentScreen = Screen.Gallery
                        },
                        onRecognizeText = { imageUri ->
                            lifecycleScope.launch {
                                val text = processImageWithOCR(applicationContext, imageUri)
                                analysisResult = PostDetails(title = "Text Recognition (OCR)", content = text)
                                currentScreen = Screen.Analysis
                            }
                        },
                        onAnalyzeWithGemma = { imageUri ->
                            if (GemmaIntegration.isInitialized()) {
                                // If it's ready, launch the coroutine to do the work.
                                // All suspend functions must be called inside this block.
                                lifecycleScope.launch {
                                    // 1. Run OCR to get the raw text from the image
                                    val rawText = processImageWithOCR(applicationContext, imageUri)
                                    if (rawText.contains("Error:", ignoreCase = true) || rawText.isBlank()) {
                                        analysisResult = PostDetails(
                                            title = "Gemma Analysis",
                                            content = "Could not extract text from the image."
                                        )
                                        currentScreen = Screen.Analysis
                                        return@launch // Exit the coroutine
                                    }

                                    // 2. Create the detailed prompt for the model
                                    val prompt = GemmaIntegration.createAnalysisPrompt(rawText)

                                    // 3. Run the inference (This suspend fun call is now correct)
                                    val gemmaResponse = GemmaIntegration.analyzeText(prompt)

                                    // 4. Display the result
                                    analysisResult = PostDetails(title = "Gemma Analysis", content = gemmaResponse)
                                    currentScreen = Screen.Analysis
                                }
                            } else {
                                // If it's not ready, just show the Toast message
                                Toast.makeText(
                                    this@MainActivity,
                                    "Please initialize Gemma from 'Manage Model' screen.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        onImageClick = { currentScreen = Screen.FullScreenViewer }
                    )
                }
            }

            is Screen.FullScreenViewer -> {
                selectedImageUri?.let { uri ->
                    FullScreenImageViewer(
                        imageUri = uri,
                        onClose = { currentScreen = Screen.Analysis }
                    )
                }
            }

            // This block handles displaying the new ModelManagerScreen
            is Screen.ModelManager -> {
                ModelManagerScreen(onClose = { currentScreen = Screen.Gallery })
            }

            Screen.FullScreenViewer -> TODO()
        }
    }

    sealed class Screen {
        object Gallery : Screen()
        object Camera : Screen()
        object Analysis : Screen()
        object FullScreenViewer : Screen()
        object ModelManager : Screen()
    }
}

@Composable
fun FullScreenImageViewer(imageUri: Uri, onClose: () -> Unit) {
    //The button for universal back gesture
    BackHandler(onBack = onClose)
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .build(),
            contentDescription = "Full screen image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 52.dp, start = 20.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.7f), // Adjust alpha for translucency
                    shape = CircleShape
                )
                .size(48.dp) // Standard touch target size
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}
