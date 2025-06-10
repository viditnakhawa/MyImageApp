package com.viditnakhawa.myimageapp
/*
import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.viditnakhawa.myimageapp.data.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object GemmaIntegration {

    private var llmInference: LlmInference? = null

    fun isInitialized(): Boolean = llmInference != null

    /**
     * Tries to create an LlmInference instance from the model file stored in the Downloads folder.
     * This should only be called after the model is confirmed to be downloaded.
     */
    fun initializeFromDownloads(context: Context, modelInfo: Model): Boolean {
        if (llmInference != null) {
            Log.d("GemmaIntegration", "Already initialized.")
            return true
        }

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val modelFile = File(downloadsDir, modelInfo.localFileName)

            if (!modelFile.exists()) {
                Log.e("GemmaIntegration", "Model file not found in Downloads folder!")
                return false
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            Log.d("GemmaIntegration", "Gemma initialized successfully from Downloads folder.")
            true
        } catch (e: Exception) {
            Log.e("GemmaIntegration", "Failed to initialize Gemma from Downloads folder", e)
            false
        }
    }

    suspend fun analyzeText(rawText: String): PostDetails {
        if (llmInference == null) {
            return PostDetails(content = "Error: Gemma model is not ready. Please download the model first.", rawText = rawText)
        }

        val prompt = """
        You are an expert content analyzer for a screenshot app.
        Given raw, messy text extracted from a screenshot, follow these steps:

        Detect Content Type:
        Identify if the text is from a social media post, news article, chat, receipt, or another source.

        Remove UI Noise:
        Exclude any irrelevant device or app interface text.

        Structure the Output:
        Extract and label key information (e.g., Author, Headline, Summary).

        Summarize:
        Provide a concise summary of the main content.
        
        ---
        Here is the text to analyze:
        "$rawText"
        """.trimIndent()

        return withContext(Dispatchers.IO) {
            try {
                val response = llmInference!!.generateResponse(prompt)
                parseGemmaResponse(response, rawText)
            } catch (e: Exception) {
                Log.e("GemmaIntegration", "Error during Gemma inference", e)
                PostDetails(rawText = rawText, content = "Error during analysis: ${e.message}")
            }
        }
    }

    private fun parseGemmaResponse(responseText: String, rawText: String): PostDetails {
        val lines = responseText.lines().filter { it.isNotBlank() }
        val title = lines.firstOrNull { it.contains(":", ignoreCase = true) }
            ?.substringBefore(':')
            ?.trim()
            ?: "Analysis"
        val content = lines.joinToString("\n")

        return PostDetails(
            title = title,
            content = content,
            rawText = rawText
        )
    }
}*/
