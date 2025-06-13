package com.viditnakhawa.myimageapp

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.viditnakhawa.myimageapp.data.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.viditnakhawa.myimageapp.data.GEMMA_E2B_MODEL


/**
 * A singleton object to handle the initialization and execution of the Gemma LlmInference engine.
 */
object GemmaIntegration {

    private var llmInference: LlmInference? = null

    /**
     * Checks if the Gemma inference engine has been successfully initialized.
     */
    fun isInitialized(): Boolean = llmInference != null

    /**
     * Initializes the LlmInference engine from the downloaded model file.
     * This should be called after the model is confirmed to be downloaded.
     * @param context The application context.
     * @return An empty string on success, or an error message on failure.
     */
    suspend fun initialize(context: Context): String {
        return withContext(Dispatchers.IO) {
            if (llmInference != null) {
                Log.d("GemmaIntegration", "Gemma is already initialized.")
                return@withContext ""
            }
            try {
                val model = GEMMA_E2B_MODEL // Your model definition
                val modelFile = File(model.getPath(context))

                if (!modelFile.exists()) {
                    val errorMsg = "Model file does not exist at path: ${modelFile.absolutePath}"
                    Log.e("GemmaIntegration", errorMsg)
                    return@withContext errorMsg
                }

                // These are the options for the inference engine.
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .build()

                llmInference = LlmInference.createFromOptions(context, options)
                Log.d("GemmaIntegration", "Gemma initialized successfully.")
                "" // Return empty string on success
            } catch (e: Exception) {
                val errorMsg = "Failed to initialize Gemma: ${e.message}"
                Log.e("GemmaIntegration", errorMsg, e)
                errorMsg
            }
        }
    }

        /**
         * Generates a response from the Gemma model for a given text prompt.
         * @param prompt The input text to analyze.
         * @return The model's generated response string.
         */
        suspend fun analyzeText(prompt: String): String {
            if (!isInitialized()) {
                val errorMsg = "Error: Gemma model is not initialized."
                Log.e("GemmaIntegration", errorMsg)
                return errorMsg
            }

            return try {
                llmInference!!.generateResponse(prompt)
            } catch (e: Exception) {
                val errorMsg = "Error during Gemma inference: ${e.message}"
                Log.e("GemmaIntegration", errorMsg, e)
                errorMsg
            }
        }

        /**
         * Creates a structured prompt for analyzing the raw text extracted from a screenshot.
         * @param rawText The unstructured text from OCR.
         * @return A formatted prompt string to guide the Gemma model.
         */
        fun createAnalysisPrompt(rawText: String): String {
            return """
        Act as a precise text cleaner for OCR output. Your task:
            - Remove ALL UI elements: Buttons (Like, Share, Reply), icons (🔍, ⚙️), navigation bars, system timestamps, and app chrome
            - Preserve EXACT original text order and wording
            - Keep numbers/dates intact
            - Maintain original line breaks for paragraphs
            - Never add summaries or interpretations
            OCR Input:
            "$rawText"
        """.trimIndent()
        }
    }

