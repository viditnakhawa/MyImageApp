package com.viditnakhawa.myimageapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingWorkPolicy
import com.viditnakhawa.myimageapp.data.ImageEntity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.viditnakhawa.myimageapp.data.GEMMA_E2B_MODEL
import com.viditnakhawa.myimageapp.ui.AddToCollectionDialog
import com.viditnakhawa.myimageapp.ui.PermissionsScreen
import com.viditnakhawa.myimageapp.ui.collections.CreateCollectionDialog
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.workers.MultimodalAnalysisWorker
import com.viditnakhawa.myimageapp.ui.navigation.AppNavigation


/*class MainActivity : ComponentActivity() {


    private val viewModel: ImageViewModel by viewModels { ViewModelProvider.Factory }
    private val modelManagerViewModel: ModelManagerViewModel by viewModels { ViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MLKitImgDescProcessor.initialize(applicationContext) // Initialize ML Kit
        // --- AUTOMATIC IMPORT ---
        // On app start, refresh the image list from the device's screenshots folder
//        lifecycleScope.launch {
//            (application as MyApplication).container.imageRepository.refreshImagesFromDevice(applicationContext)
//            val isModelDownloaded = (application as MyApplication).container.imageRepository.isGemmaModelDownloaded(this@MainActivity)
//
//            if (isModelDownloaded && !modelManagerViewModel.isGemmaInitialized()) {
//                Log.d("MainActivity", "Model is downloaded but not initialized. Initializing in background...")
//                modelManagerViewModel.initializeModel(this@MainActivity, GEMMA_E2B_MODEL)
//            }
//        }
        setContent {
            MyImageAppTheme {
                // We will now decide whether to show the PermissionsScreen or the main app
                var allPermissionsGranted by remember {
                    mutableStateOf(checkAllPermissions(this))
                }

                if (allPermissionsGranted) {
                    // On app start, refresh the image list from the device's screenshots folder
                    LaunchedEffect(Unit) {
                        (application as MyApplication).container.imageRepository.refreshImagesFromDevice(applicationContext)
                        val modelManagerViewModel: ModelManagerViewModel by viewModels { ViewModelProvider.Factory }
                        // Check if the model is downloaded but not yet initialized
                        if (!modelManagerViewModel.isGemmaInitialized() &&
                            (application as MyApplication).container.imageRepository.isGemmaModelDownloaded(this@MainActivity)) {
                            // Initialize it in the background
                            modelManagerViewModel.initializeModel(this@MainActivity, GEMMA_E2B_MODEL)
                        }
                    }
                    MyImageApp()
                } else {
                    PermissionsScreen(
                        onPermissionsGranted = {
                            allPermissionsGranted = true
                        }
                    )
                }
            }
        }
    }

    private fun checkAllPermissions(context: Context): Boolean {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
        return permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MyImageApp() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Gallery) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var isLoadingAnalysis by remember { mutableStateOf(false) }
        val isOcrRunning = remember { mutableStateOf(false) }
        var rawOcrResult by remember { mutableStateOf<String?>(null) }
        val collections by viewModel.collections.collectAsState()
        var showCollectionDialog by remember { mutableStateOf(false) }
        val imageList by viewModel.images.collectAsState()
        val scope = rememberCoroutineScope()
        val modelManagerViewModel: ModelManagerViewModel = viewModel(factory = ViewModelProvider.Factory)
        var showCreateCollectionDialog by remember { mutableStateOf(false) }
        val collectionsWithImages by viewModel.collectionsWithImages.collectAsStateWithLifecycle()

        if (showCreateCollectionDialog) {
            CreateCollectionDialog(
                onDismissRequest = { showCreateCollectionDialog = false },
                onCreateClicked = { collectionName ->
                    scope.launch {
                        val newId = viewModel.createCollectionAndReturnId(collectionName)
                        currentScreen = Screen.SelectScreenshots(newId)
                        showCreateCollectionDialog = false
                    }
                }
            )
        }

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

        BackHandler(enabled = currentScreen != Screen.Gallery) {
            rawOcrResult = null // Clear temporary result on back press
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
                    onViewCollectionsClick = { currentScreen = Screen.CollectionsList },
                    onCreateCollectionClick = { showCreateCollectionDialog = true },
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
                            val needsFallbackAnalysis = !gemmaIsReady && imageDetails?.sourceApp == null

                            //val needsAnalysis = viewModel.getImageDetails(uri)?.title == null
                            if (needsSmartAnalysis) {
                                // Upgrade from fallback or analyze for the first time with Gemma
                                val workManager = WorkManager.getInstance(applicationContext)
                                // --- STRATEGY PART 1: PRIORITIZE CLICKED IMAGE ---
                                // Enqueue a high-priority worker for the specific image the user clicked.
                                val analysisWorkRequest = OneTimeWorkRequestBuilder<MultimodalAnalysisWorker>()
                                    .setInputData(workDataOf("IMAGE_URI" to uri.toString()))
                                    .build()
                                workManager.enqueueUniqueWork(
                                    "MultimodalAnalysis_${uri}", // A unique name for this specific image job
                                    ExistingWorkPolicy.KEEP,
                                    analysisWorkRequest
                                )

                                // --- STRATEGY PART 2: PROCESS OTHERS IN BACKGROUND ---
                                // Also enqueue the background batch worker to handle all other images.
//                                val batchWorkRequest = OneTimeWorkRequestBuilder<BatchAnalysisWorker>().build()
//                                workManager.enqueueUniqueWork(
//                                    "background_batch_analysis", // A generic name for the background task
//                                    ExistingWorkPolicy.KEEP, // Don't start a new one if it's already running
//                                    batchWorkRequest
//                                )
                            } else if (needsFallbackAnalysis) {
                                val fallbackAnalysis = MLKitImgDescProcessor.describeImage(applicationContext, uri)

                                // If the analysis fails, save it as a 'failed' state so the UI can update.
                                // Otherwise, save the successful analysis.
                                val entityToSave = if (fallbackAnalysis.content.startsWith("Error", ignoreCase = true)) {
                                    (imageDetails ?: ImageEntity(imageUri = uri.toString())).copy(
                                        title = "Analysis Failed",
                                        content = fallbackAnalysis.content
                                    )
                                } else {
                                    (imageDetails ?: ImageEntity(imageUri = uri.toString())).copy(
                                        title = fallbackAnalysis.title,
                                        content = fallbackAnalysis.content
                                    )
                                }
                                viewModel.updateImageDetails(entityToSave)
                            }
                            // If neither condition is met, do nothing. The existing cached data is sufficient.
                        }
                    },
                    onManageModelClick = { currentScreen = Screen.ModelManager }
                )
            }

            is Screen.CollectionsList -> {
                CollectionsScreen(
                    collections = collectionsWithImages,
                    onNavigateBack = { currentScreen = Screen.Gallery }
                )
            }
            is Screen.SelectScreenshots -> {
                SelectScreenshotsScreen(
                    allImages = imageList,
                    onClose = { currentScreen = Screen.Gallery },
                    onDone = { selectedUris ->
                        val uriStrings = selectedUris.map { it.toString() }
                        viewModel.addImagesToCollection(uriStrings, (currentScreen as Screen.SelectScreenshots).newCollectionId)
                        currentScreen = Screen.CollectionsList
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
                        // Show a loading state from `isLoadingAnalysis` OR if analysisResult is null
                        isLoading = isLoadingAnalysis || analysisResult == null,
                        isOcrRunning = isOcrRunning.value,
                        details = analysisResult, // Pass the nullable details
                        rawOcrText = rawOcrResult,
                        onClose = {
                            rawOcrResult = null
                            currentScreen = Screen.Gallery
                                  },
                        onAddToCollection = {showCollectionDialog = true},
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
                                isOcrRunning.value = true
                                rawOcrResult = null
                                // 1. Run the OCR process
                                val rawText = processImageWithOCR(applicationContext, imageUri)
                                if (modelManagerViewModel.isGemmaInitialized()) {
                                    // --- GEMMA PATH ---
                                    Log.d("MainActivity", "Gemma is initialized. Polishing OCR text.")
                                    val polishedText = modelManagerViewModel.polishTextWithGemma(rawText)
                                    val currentDetails = viewModel.getImageDetails(imageUri) ?: ImageEntity(imageUri.toString())
                                    val updatedDetails = currentDetails.copy(polishedOcr = polishedText)
                                    viewModel.updateImageDetails(updatedDetails)
                                    // The UI will update automatically from the database observer
                                } else {
                                    // --- FALLBACK PATH ---
                                    Log.d("MainActivity", "Gemma not ready. Showing raw OCR.")
                                    // Set the temporary state variable to show the raw text in its own card
                                    rawOcrResult = rawText
                                }
                                isOcrRunning.value = false
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
        }

        if (showCollectionDialog && selectedImageUri != null) {
            val imageUriToAdd = selectedImageUri!! // Ensure it's not null
            AddToCollectionDialog(
                collections = collections,
                onDismiss = { showCollectionDialog = false },
                onCollectionSelected = { collectionId ->
                    viewModel.addImageToCollection(imageUriToAdd.toString(), collectionId)
                    // Optionally, show a toast or confirmation
                },
                onCreateCollection = { collectionName ->
                    viewModel.createCollection(collectionName)
                }
            )
        }
    }

    sealed class Screen {
        object Gallery : Screen()
        object Camera : Screen()
        object Analysis : Screen()
        object FullScreenViewer : Screen()
        object ModelManager : Screen()
        data class Chat(val imageUri: Uri) : Screen()
        object CollectionsList : Screen()
        data class SelectScreenshots(val newCollectionId: Long) : Screen()
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
}*/

class MainActivity : ComponentActivity() {

    private val modelManagerViewModel: ModelManagerViewModel by viewModels { ViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MLKitImgDescProcessor.initialize(applicationContext)

        setContent {
            MyImageAppTheme {
                var permissionsGranted by remember { mutableStateOf(checkAllPermissions()) }

                if (permissionsGranted) {
                    LaunchedEffect(Unit) {
                        (application as MyApplication).container.imageRepository.refreshImagesFromDevice(applicationContext)
                        val isModelDownloaded = (application as MyApplication).container.imageRepository.isGemmaModelDownloaded(this@MainActivity)
                        if (isModelDownloaded && !modelManagerViewModel.isGemmaInitialized()) {
                            modelManagerViewModel.initializeModel(this@MainActivity, GEMMA_E2B_MODEL)
                        }
                    }
                    AppNavigation()
                } else {
                    PermissionsScreen(onPermissionsGranted = { permissionsGranted = true })
                }
            }
        }
    }

    private fun checkAllPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }
}
