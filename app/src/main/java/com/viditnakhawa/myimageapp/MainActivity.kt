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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viditnakhawa.myimageapp.data.ImageEntity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.viditnakhawa.myimageapp.data.GEMMA_E2B_MODEL
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.workers.SmartAnalysisWorker

class MainActivity : ComponentActivity() {

    private val viewModel: ImageViewModel by viewModels { ViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MLKitImgDescProcessor.initialize(applicationContext) // Initialize ML Kit
        // --- AUTOMATIC IMPORT ---
        // On app start, refresh the image list from the device's screenshots folder
        lifecycleScope.launch {
            (application as MyApplication).container.imageRepository.refreshImagesFromDevice(applicationContext)
            val modelManagerViewModel: ModelManagerViewModel by viewModels { ViewModelProvider.Factory }
            // Check if the model is downloaded but not yet initialized
            if (!modelManagerViewModel.isGemmaInitialized() &&
                (application as MyApplication).container.imageRepository.isGemmaModelDownloaded(this@MainActivity)) {
                // Initialize it in the background
                modelManagerViewModel.initializeModel(this@MainActivity, GEMMA_E2B_MODEL)
            }
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
        var isLoadingAnalysis by remember { mutableStateOf(false) }

        val imageList by viewModel.images.collectAsState()
        val scope = rememberCoroutineScope()
        val modelManagerViewModel: ModelManagerViewModel = viewModel(factory = ViewModelProvider.Factory)

        // This produceState block is correct and will now be the ONLY source for analysisResult
        val analysisResult by produceState<PostDetails?>(initialValue = null, key1 = selectedImageUri) {
            // When selectedImageUri changes, this block runs
            selectedImageUri?.let { uri ->
                isLoadingAnalysis = true
                viewModel.getImageDetailsFlow(uri).collect { entity ->
                    // This will emit a new value whenever the database entry for this URI changes
                    value = if (entity?.title != null) {
                        PostDetails(
                            title = entity.title!!,
                            content = entity.content ?: "",
                            sourceApp = entity.sourceApp,
                            tags = entity.tags,
                            isFallback = (entity.title != null && entity.sourceApp == null)
                        )
                    } else {
                        PostDetails(title = "Analyzing...")
                    }
                    isLoadingAnalysis = false
                }
            }
        }

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

        // --- IMPROVED BACK NAVIGATION ---
        // This BackHandler will manage back presses for the entire app.
        BackHandler(enabled = currentScreen != Screen.Gallery) {
            when (currentScreen) {
                is Screen.Analysis -> currentScreen = Screen.Gallery
                is Screen.FullScreenViewer -> currentScreen = Screen.Analysis
                is Screen.ModelManager -> currentScreen = Screen.Gallery
                is Screen.Camera -> currentScreen = Screen.Gallery
                else -> { /* No action needed for Gallery */ }
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
                        // --- THIS IS THE SIMPLIFIED LOGIC ---
                        // 1. Set the URI. This will trigger the produceState block above.
                        selectedImageUri = uri
                        // 2. Navigate to the screen.
                        currentScreen = Screen.Analysis
                        // 3. Launch a check to see if a *new* analysis is needed.
                        scope.launch {
                            // Get the current state of the image from the database
                            val imageDetails = viewModel.getImageDetails(uri)
                            val gemmaIsReady = modelManagerViewModel.isGemmaInitialized()

                            // Decide if we need to run an analysis
                            val needsSmartAnalysis = gemmaIsReady && imageDetails?.sourceApp == null
                            val needsFallbackAnalysis = !gemmaIsReady && imageDetails == null

                            //val needsAnalysis = viewModel.getImageDetails(uri)?.title == null
                            if (needsSmartAnalysis) {
                                // Upgrade from fallback or analyze for the first time with Gemma
                                val workManager = WorkManager.getInstance(applicationContext)
                                val workRequest = OneTimeWorkRequestBuilder<SmartAnalysisWorker>()
                                    .setInputData(workDataOf("IMAGE_URI" to uri.toString()))
                                    .build()
                                workManager.enqueue(workRequest)
                            } else if (needsFallbackAnalysis) {
                                // First time analysis, but Gemma isn't ready
                                val fallbackAnalysis = MLKitImgDescProcessor.describeImage(applicationContext, uri)
                                val entityToSave = ImageEntity(
                                    imageUri = uri.toString(),
                                    title = fallbackAnalysis.title,
                                    content = fallbackAnalysis.content
                                    // sourceApp remains null, marking this as a fallback
                                )
                                viewModel.updateImageDetails(entityToSave)
                            }
                            // If neither condition is met, do nothing. The existing cached data is sufficient.
                        }
                    },
                    onManageModelClick = { currentScreen = Screen.ModelManager }
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
                        // Show a loading state from `isLoadingAnalysis` OR if analysisResult is null
                        isLoading = isLoadingAnalysis || analysisResult == null,
                        details = analysisResult, // Pass the nullable details
                        onClose = { currentScreen = Screen.Gallery },
                        onAddToCollection = { /*TODO*/ },
                        onShare = { uri ->
                            val shareIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "image/jpeg"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share Image"))
                        },
                        onDelete = {
                            viewModel.removeImage(it)
                            currentScreen = Screen.Gallery
                        },

                        // --- CORRECTED LOGIC FOR onRecognizeText ---
                        onRecognizeText = { imageUri ->
                            scope.launch {
                                // 1. Run the OCR process
                                val ocrText = processImageWithOCR(applicationContext, imageUri)
                                // 2. Get the current details from the database
                                val currentDetails = viewModel.getImageDetails(imageUri) ?: ImageEntity(imageUri.toString())
                                // 3. Update the entity and save it back to the database
                                val updatedDetails = currentDetails.copy(
                                    title = "Text Recognition (OCR)",
                                    content = ocrText
                                )
                                viewModel.updateImageDetails(updatedDetails)
                                // The UI will update automatically because produceState is listening for this change.
                            }
                        },

                        // --- CORRECTED LOGIC FOR onAnalyzeWithGemma ---
                        onAnalyzeWithGemma = {
                            if (modelManagerViewModel.isGemmaInitialized()) {
                                // This now correctly implements our plan to navigate to the chat screen (Phase 2)
                                // For now, it will seem like nothing happens, which is correct until we build the chat screen.
                                //currentScreen = Screen.Chat(analysisResult!!)
                            } else {
                                // This part is correct: prompt the user to initialize the model.
                                Toast.makeText(
                                    this@MainActivity,
                                    "Please initialize Gemma for a detailed chat.",
                                    Toast.LENGTH_LONG
                                ).show()
                                currentScreen = Screen.ModelManager
                            }
                        },
                        onImageClick = { currentScreen = Screen.FullScreenViewer }
                    )
                } else if (isLoadingAnalysis) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
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
