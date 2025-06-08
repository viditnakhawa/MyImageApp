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

                // State to track if the AI model is ready and if we are currently processing.
                var isLoading by remember { mutableStateOf(true) } // Start loading initially
                var isModelReady by remember { mutableStateOf(false) }

                // LAUNCHERS - These were missing. Adding them back fixes the error.
                val takePictureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        result.data?.data?.let { images.add(it) }
                    }
                }

                val pickImageLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let { images.add(it) }
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
                                onCapturePhotoClick = {
                                    val intent = Intent(this@MainActivity, CameraCapture::class.java)
                                    takePictureLauncher.launch(intent)
                                },
                                onPickFromGalleryClick = { pickImageLauncher.launch("image/*") },
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
