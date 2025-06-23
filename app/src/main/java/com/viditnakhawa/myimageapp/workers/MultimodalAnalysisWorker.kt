package com.viditnakhawa.myimageapp.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.viditnakhawa.myimageapp.MyApplication
import com.viditnakhawa.myimageapp.data.GEMMA_E2B_MODEL
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.data.StructuredAnalysis
import com.viditnakhawa.myimageapp.LlmChatModelHelper
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor.uriToBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.core.net.toUri

private const val TAG = "MultimodalAnalysisWorker"

class MultimodalAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val imageUriString = inputData.getString("IMAGE_URI") ?: return Result.failure()
        val imageUri = imageUriString.toUri()
        val repository = (applicationContext as MyApplication).container.imageRepository

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