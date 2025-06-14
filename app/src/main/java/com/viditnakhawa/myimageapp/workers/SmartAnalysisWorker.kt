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
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.processImageWithOCR
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

// A data class to easily parse Gemma's structured JSON output
data class StructuredAnalysis(
    val title: String,
    val summary: String,
    val sourceApp: String,
    val tags: List<String>
)

class SmartAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val imageUriString = inputData.getString("IMAGE_URI") ?: return Result.failure()
        val imageUri = Uri.parse(imageUriString)
        val repository = (applicationContext as MyApplication).container.imageRepository

        try {
            // Step 1: Run both visual and text analysis in parallel
            val (visualDesc, ocrText) = coroutineScope {
                val visualDescDeferred = async { MLKitImgDescProcessor.describeImage(applicationContext, imageUri) }
                val ocrTextDeferred = async { processImageWithOCR(applicationContext, imageUri) }
                (visualDescDeferred.await().content to ocrTextDeferred.await())
            }

            // Step 2: Create the master prompt for Gemma
            val combinedInput = """
                VISUAL DESCRIPTION:
                '$visualDesc'

                EXTRACTED TEXT (OCR):
                '$ocrText'
            """.trimIndent()

            val prompt = createMasterPrompt(combinedInput)

            // Step 3: Get the structured analysis from Gemma
            val gemmaResponse = GemmaIntegration.analyzeText(prompt)

            // Step 4: Parse the JSON response
            val analysis = parseGemmaResponse(gemmaResponse)

            if (analysis == null) {
                Log.e("SmartAnalysisWorker", "Failed to parse Gemma's JSON response.")
                return Result.failure()
            }

            // Step 5: Save the complete, structured analysis to the database
            val imageEntity = repository.getImageDetails(imageUri) ?: ImageEntity(imageUri = imageUriString)
            val updatedEntity = imageEntity.copy(
                title = analysis.title,
                content = analysis.summary,
                sourceApp = analysis.sourceApp,
                tags = analysis.tags
            )
            repository.updateImageDetails(updatedEntity)

            return Result.success()

        } catch (e: Exception) {
            Log.e("SmartAnalysisWorker", "Error in SmartAnalysisWorker", e)
            return Result.failure()
        }
    }

    private fun createMasterPrompt(context: String): String {
        return """
        You are a highly intelligent screenshot analysis engine. Based on the following visual description and extracted text, provide a structured JSON object with the following fields: "title", "summary", "sourceApp", "tags", and "formattedOcr".

        - title: A concise, descriptive title (maximum 10 words).
        - summary: A clear, informative paragraph summarizing the main content and context of the screenshot, highlighting its purpose or what a user would find important.
        - sourceApp: The name of the application the screenshot was taken from (e.g., "Twitter", "Gmail", or "Unknown").
        - tags: A JSON array of 3 to 5 relevant string keywords.
        - formattedOcr: The extracted OCR text, preserved exactly as in the original, but formatted for readability (e.g., proper line breaks, removal of obvious artifacts, but no changes to actual words).
            
        Instructions:
        - Output only a valid JSON object.
        - Do not alter the wording of the OCR text in "formattedOcr"—only adjust spacing and line breaks for readability.
        - In the "summary", provide a clear, informative paragraph that explains what is important or interesting about the screenshot, referencing both visual and textual content.
        - If the source application cannot be confidently determined, set "sourceApp" to "Unknown".

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