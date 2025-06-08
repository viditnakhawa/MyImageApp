package com.viditnakhawa.myimageapp

import ScreenshotGalleryScreenWithFAB
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.ai.edge.aicore.GenerationConfig
import com.google.ai.edge.aicore.GenerativeModel
import com.viditnakhawa.myimageapp.ui.theme.MyImageAppTheme
import kotlinx.coroutines.launch

// Import the missing composable and function to resolve the errors.
import com.viditnakhawa.myimageapp.AnalysisScreen
import com.viditnakhawa.myimageapp.processImageWithOCR


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyImageAppTheme {
                // STATE MANAGEMENT
                val images = remember { mutableStateListOf<Uri>() }
                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                var analysisDetails by remember { mutableStateOf<PostDetails?>(null) }
                val scope = rememberCoroutineScope()

                var isLoading by remember { mutableStateOf(true) } // Start loading initially
                var isModelReady by remember { mutableStateOf(false) }
                var showCamera by remember { mutableStateOf(false) } // State to control camera visibility


                // LAUNCHERS
                val pickImageLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { images.add(0, it) } // Add new images to the top
                }


                // This LaunchedEffect runs once to initialize our on-device model.
                LaunchedEffect(Unit) {
                    try {
                        val generationConfig = GenerationConfig.Builder().build()
                        val generativeModel = GenerativeModel(generationConfig)
                        GeminiNanoIntegration.initialize(generativeModel)
                        isModelReady = true // Mark the model as ready
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, "On-device model not available on this device.", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false // Stop the initial loading indicator
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // UI NAVIGATION LOGIC
                    when {
                        // If showCamera is true, display the new Composable camera
                        showCamera -> {
                            ComposeCameraScreen(
                                onImageCaptured = { uri ->
                                    images.add(0, uri) // Add to the top of the list
                                    showCamera = false // Go back to the gallery
                                },
                                onClose = {
                                    showCamera = false // Go back to the gallery
                                }
                            )
                        }

                        // If we have analysis results, show the AnalysisScreen
                        analysisDetails != null && selectedImageUri != null -> {
                            AnalysisScreen(
                                imageUri = selectedImageUri!!,
                                details = analysisDetails!!,
                                onClose = {
                                    analysisDetails = null
                                    selectedImageUri = null
                                }
                            )
                        }

                        // Default view: Show the gallery.
                        else -> {
                            ScreenshotGalleryScreenWithFAB(
                                images = images,
                                onAddCollectionClick = { /* TODO */ },
                                onCapturePhotoClick = { showCamera = true }, // Set state to true
                                onPickFromGalleryClick = { pickImageLauncher.launch("image/*") }, // CORRECTED: Use the existing launcher
                                // AUTO-PROCESS LOGIC IS NOW HERE
                                onImageClick = { uri ->
                                    // Prevent analysis if the model isn't ready yet.
                                    if (!isModelReady) {
                                        Toast.makeText(applicationContext, "AI model is initializing, please wait...", Toast.LENGTH_SHORT).show()
                                        return@ScreenshotGalleryScreenWithFAB
                                    }

                                    isLoading = true // Show loading indicator for this analysis
                                    selectedImageUri = uri

                                    // 1. Get raw text from the image
                                    processImageWithOCR(applicationContext, uri) { rawText ->
                                        if (rawText.isBlank() || rawText.startsWith("Error:")) {
                                            Toast.makeText(applicationContext, "Could not find text.", Toast.LENGTH_SHORT).show()
                                            selectedImageUri = null
                                            isLoading = false // Hide loading indicator
                                        } else {
                                            // 2. Launch a coroutine to analyze the text.
                                            scope.launch {
                                                val details = GeminiNanoIntegration.analyzeText(rawText)
                                                analysisDetails = details
                                                isLoading = false // Hide loading indicator
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Loading Overlay: Show a progress indicator when isLoading is true.
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
