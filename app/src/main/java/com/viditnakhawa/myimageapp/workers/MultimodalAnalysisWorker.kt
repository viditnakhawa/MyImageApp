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
        You are a highly intelligent screenshot analysis engine. Analyze the provided image and generate a structured JSON object with the fields: "title", "summary", "sourceApp", "tags".

        - From the image, determine the source application ("Twitter", "Gmail", "Unknown", etc.).
        - Write a concise title (under 10 words).
        - Write a concise summary of the screenshot's content and purpose.
        - Extract 3-5 relevant keyword tags.
        - Perform any necessary text recognition from the image internally to inform your analysis.

        JSON_OUTPUT:
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