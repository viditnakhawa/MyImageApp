package com.viditnakhawa.myimageapp.workers

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.viditnakhawa.myimageapp.LlmChatModelHelper
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor.uriToBitmap
import com.viditnakhawa.myimageapp.data.GEMMA_E2B_MODEL
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.data.ImageRepository
import com.viditnakhawa.myimageapp.data.StructuredAnalysis
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "MultimodalAnalysisWorker"

@HiltWorker
class MultimodalAnalysisWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ImageRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val imageUriString = inputData.getString("IMAGE_URI") ?: return Result.failure()
        val imageUri = imageUriString.toUri()

        try {
            if (!LlmChatModelHelper.isModelInitialized(GEMMA_E2B_MODEL)) {
                Log.d(TAG, "Model is not initialized. Initializing now...")
                var initializationError = ""
                LlmChatModelHelper.initialize(appContext, GEMMA_E2B_MODEL) { error ->
                    if (error.isNotEmpty()) {
                        initializationError = error
                    }
                }
                // If initialization failed, stop the worker.
                if (initializationError.isNotEmpty()) {
                    Log.e(TAG, "Model initialization failed in worker: $initializationError")
                    return Result.failure()
                }
                Log.d(TAG, "Model initialized successfully in worker.")
            }

            LlmChatModelHelper.resetSession(GEMMA_E2B_MODEL)
            val bitmap = uriToBitmap(applicationContext, imageUri) ?: return Result.failure()

            // The prompt now directly asks Gemma to analyze the image content
            val prompt = createGemmaPrompt()
            val fullResponse = analyzeImageWithGemma(prompt, bitmap)
            delay(250)

            val analysis = parseGemmaResponse(fullResponse)
            if (analysis != null) {
                val imageEntity = repository.getImageDetails(imageUri) ?: ImageEntity(imageUri = imageUriString)
                val updatedEntity = imageEntity.copy(
                    title = analysis.title,
                    content = analysis.summary,
                    sourceApp = analysis.sourceApp,
                    tags = analysis.tags
                )
                repository.updateImageDetails(updatedEntity)
                Log.d(TAG, "Gemma analysis successful for: $imageUriString")
                return Result.success()
            } else {
                Log.e(TAG, "Failed to parse Gemma's JSON response for: $imageUriString")
                Log.e(TAG, "Raw model output was: $fullResponse")

                val failedEntity = repository.getImageDetails(imageUri) ?: ImageEntity(imageUri = imageUriString)
                val updatedFailedEntity = failedEntity.copy(
                    title = "Analysis Failed",
                    content = "The model returned an invalid response. Please try re-analyzing from the image details screen later."
                )
                repository.updateImageDetails(updatedFailedEntity)
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in MultimodalAnalysisWorker for: $imageUriString", e)

            val failedAnalysis = StructuredAnalysis(
                title = "Analysis Failed",
                summary = "Could not analyze this image. The model may be busy or an unexpected error occurred. Please try again later.",
                sourceApp = "System",
                tags = listOf("Error"),
                formattedOcr = null
            )

            val imageEntity = repository.getImageDetails(imageUri) ?: ImageEntity(imageUri = imageUriString)
            val updatedEntity = imageEntity.copy(
                title = failedAnalysis.title,
                content = failedAnalysis.summary,
                sourceApp = failedAnalysis.sourceApp,
                tags = failedAnalysis.tags
            )
            repository.updateImageDetails(updatedEntity)
            return Result.failure()
        }
    }

    private fun createGemmaPrompt(): String {
        // This prompt expects the model to do all the work from the image alone.
        return """
        SYSTEM_TASK:Add commentMore actions
        You are an expert-level screenshot classification and summarization agent. Your task is to analyze the provided image and respond ONLY with a valid, structured JSON object.

        INSTRUCTIONS:
        1.  **Analyze Image:** Examine the visual elements and any text in the screenshot.
        2.  **Determine Source App:** Identify the application where the screenshot was taken (e.g., "Twitter", "Gmail", "Instagram", "Unknown").
        3.  **Create Title:** Write a very short, descriptive title (max 10 words).
        4.  **Create Summary:** Write a brief, three or four-sentence summary of the main content.
        5.  **Extract Tags:** List 3 to 5 relevant keywords as an array of strings.
        
        JSON_SCHEMA:
        {
          "title": "string",
          "summary": "string",
          "sourceApp": "string",

          "tags": ["string"]
        }
        

        RESPONSE:
        """.trimIndent()
    }

    private fun parseGemmaResponse(response: String): StructuredAnalysis? {
        val jsonString = response.substringAfter("```json").substringBeforeLast("```").trim()
        return try {
            Gson().fromJson(jsonString, StructuredAnalysis::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parsing failed: ${e.localizedMessage}")
            null
        }
    }

    private suspend fun analyzeImageWithGemma(prompt: String, image: android.graphics.Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            var fullResponse = ""
            LlmChatModelHelper.runInference(
                model = GEMMA_E2B_MODEL,
                input = prompt,
                images = listOf(image),
                resultListener = { partialResult, done ->
                    fullResponse += partialResult
                    if (done && continuation.isActive) {
                        continuation.resume(fullResponse)
                    }
                },
                cleanUpListener = {
                    if (continuation.isActive) continuation.resume("")
                }
            )
        }
}

