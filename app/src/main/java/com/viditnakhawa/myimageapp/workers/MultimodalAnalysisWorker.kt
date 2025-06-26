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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "MultimodalAnalysisWorker"

@HiltWorker
class MultimodalAnalysisWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ImageRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val imageUriString = inputData.getString("IMAGE_URI") ?: return Result.failure()
        val imageUri = imageUriString.toUri()

        try {
            LlmChatModelHelper.resetSession(GEMMA_E2B_MODEL)
            val bitmap = uriToBitmap(applicationContext, imageUri)
                ?: return Result.failure()

            // The prompt now directly asks Gemma to analyze the image content
            val prompt = createGemmaPrompt()

            val fullResponse = analyzeImageWithGemma(prompt, bitmap)
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
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in MultimodalAnalysisWorker for: $imageUriString", e)
            return Result.failure()
        }
    }

    private fun createGemmaPrompt(): String {
        // This prompt expects the model to do all the work from the image alone.
        return """
        SYSTEM_TASK:
        You are an expert-level screenshot data extraction agent. Your goal is to identify key entities and structured information from the provided image. Respond ONLY with a valid, structured JSON object based on the schema.

        INSTRUCTIONS:
        1.  **Identify Core Entity:** Determine the main subject (e.g., Social Media Post, Weather Forecast, News Article).
        2.  **Create Factual Title:** Write a short, factual title describing the subject.
        3.  **Extract Key Details:** Pull out all relevant data points. Format these as a bulleted list within a single string for the 'details' field. Do not write a narrative summary.
        4.  **Generate Keywords:** Create a list of 3-5 relevant tags.

        JSON_SCHEMA:
        {
          "title": "string",
          "sourceApp": "string",
          "details": "string",
          "tags": ["string"]
        }
        Analyze the provided image and generate the JSON response now.

        RESPONSE:
        """.trimIndent()
    }

    private fun parseGemmaResponse(response: String): StructuredAnalysis? {
        val jsonString = response.substringAfter("```json").substringBeforeLast("```").trim()
        return try {
            Gson().fromJson(jsonString, StructuredAnalysis::class.java)
        } catch (e: JsonSyntaxException) {
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
                    if (done) {
                        if (continuation.isActive) continuation.resume(fullResponse)
                    }
                },
                cleanUpListener = {
                    if (continuation.isActive) continuation.resume("")
                }
            )
        }
}

