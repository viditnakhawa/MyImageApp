package com.viditnakhawa.myimageapp.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.viditnakhawa.myimageapp.GemmaIntegration
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor
import com.viditnakhawa.myimageapp.MyApplication
import com.viditnakhawa.myimageapp.processImageWithOCR
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class BatchAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as MyApplication).container.imageRepository
        val unanalyzedImages = repository.getUnanalyzedImages()

        if (unanalyzedImages.isEmpty()) {
            Log.d("BatchAnalysisWorker", "No images to analyze.")
            return Result.success()
        }

        Log.d("BatchAnalysisWorker", "Starting batch analysis for ${unanalyzedImages.size} images.")

        // Process images in chunks of 5 to be efficient
        unanalyzedImages.chunked(5).forEach { batch ->
            coroutineScope {
                batch.forEach { imageEntity ->
                    // Launch analysis for each image in the batch concurrently
                    async {
                        try {
                            analyzeSingleImage(applicationContext, imageEntity.imageUri)
                        } catch (e: Exception) {
                            Log.e("BatchAnalysisWorker", "Failed to analyze image: ${imageEntity.imageUri}", e)
                        }
                    }
                }
            }
            // Wait for a short period between batches to avoid overloading the system
            Log.d("BatchAnalysisWorker", "Finished a batch. Waiting for 1 minute.")
            delay(60_000) // 1 minute delay
        }

        Log.d("BatchAnalysisWorker", "Batch analysis complete.")
        return Result.success()
    }

    // This logic is duplicated from SmartAnalysisWorker.
    // It can be extracted into a shared helper/utility class for better architecture.
    private suspend fun analyzeSingleImage(context: Context, imageUriString: String) {
        val imageUri = Uri.parse(imageUriString)
        val repository = (context as MyApplication).container.imageRepository

        val (visualDesc, ocrText) = coroutineScope {
            val visualDescDeferred = async { MLKitImgDescProcessor.describeImage(context, imageUri) }
            val ocrTextDeferred = async { processImageWithOCR(context, imageUri) }
            (visualDescDeferred.await().content to ocrTextDeferred.await())
        }

        val prompt = createMasterPrompt(
            "VISUAL DESCRIPTION:\n'$visualDesc'\n\nEXTRACTED TEXT (OCR):\n'$ocrText'"
        )

        val gemmaResponse = GemmaIntegration.analyzeText(prompt)
        val analysis = parseGemmaResponse(gemmaResponse) ?: return

        val imageEntity = repository.getImageDetails(imageUri) ?: return
        val updatedEntity = imageEntity.copy(
            title = analysis.title,
            content = analysis.summary,
            sourceApp = analysis.sourceApp,
            tags = analysis.tags
        )
        repository.updateImageDetails(updatedEntity)
    }

    private fun createMasterPrompt(context: String): String {
        return """
        You are a highly intelligent screenshot analysis engine. Based on the following visual description and extracted text, provide a structured JSON object with the following fields: "title", "summary", "sourceApp", and "tags".

        - title: A concise, descriptive title no longer than 10 words.
        - summary: A detailed one-paragraph summary of the screenshot's main content.
        - sourceApp: The name of the application the screenshot was taken from (e.g., "Twitter", "Gmail", "Unknown").
        - tags: A JSON array of 3 to 5 relevant string keywords.

        CONTEXT:
        $context

        JSON_OUTPUT:
        """.trimIndent()
    }

    private fun parseGemmaResponse(response: String): StructuredAnalysis? {
        // Find the JSON part of the response, removing markdown backticks if present
        val jsonString = response.substringAfter("```json").substringBeforeLast("```").trim()
        return try {
            Gson().fromJson(jsonString, StructuredAnalysis::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}
