package com.viditnakhawa.myimageapp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.viditnakhawa.myimageapp.data.Accelerator
import com.viditnakhawa.myimageapp.data.ConfigKey
import com.viditnakhawa.myimageapp.data.MAX_IMAGE_COUNT
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.ui.common.DEFAULT_MAX_TOKEN
import com.viditnakhawa.myimageapp.ui.common.DEFAULT_TEMPERATURE
import com.viditnakhawa.myimageapp.ui.common.DEFAULT_TOPK
import com.viditnakhawa.myimageapp.ui.common.DEFAULT_TOPP
import com.viditnakhawa.myimageapp.ui.common.cleanUpMediapipeTaskErrorMessage

private const val TAG = "Gemma_Model_E2B"

typealias ResultListener = (partialResult: String, done: Boolean) -> Unit

typealias CleanUpListener = () -> Unit

data class LlmModelInstance(val engine: LlmInference, var session: LlmInferenceSession)

object LlmChatModelHelper {
    // Indexed by model name.
    private val cleanUpListeners: MutableMap<String, CleanUpListener> = mutableMapOf()
    private val lock = Any()

    fun isModelInitialized(model: Model): Boolean {
        return model.instance != null
    }

    fun initialize(context: Context, model: Model, onDone: (String) -> Unit) {
        synchronized(lock) {
            // Prepare options.
            val maxTokens =
                model.getIntConfigValue(
                    key = ConfigKey.MAX_TOKENS,
                    defaultValue = DEFAULT_MAX_TOKEN
                )
            val topK = model.getIntConfigValue(key = ConfigKey.TOPK, defaultValue = DEFAULT_TOPK)
            val topP = model.getFloatConfigValue(key = ConfigKey.TOPP, defaultValue = DEFAULT_TOPP)
            val temperature =
                model.getFloatConfigValue(
                    key = ConfigKey.TEMPERATURE,
                    defaultValue = DEFAULT_TEMPERATURE
                )
            val accelerator =
                model.getStringConfigValue(
                    key = ConfigKey.ACCELERATOR,
                    defaultValue = Accelerator.GPU.label
                )
            Log.d(TAG, "Initializing...")
            val preferredBackend =
                when (accelerator) {
                    Accelerator.CPU.label -> LlmInference.Backend.CPU
                    Accelerator.GPU.label -> LlmInference.Backend.GPU
                    else -> LlmInference.Backend.GPU
                }
            val options =
                LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(model.getPath(context = context))
                    .setMaxTokens(maxTokens)
                    .setPreferredBackend(preferredBackend)
                    .setMaxNumImages(if (model.llmSupportImage) MAX_IMAGE_COUNT else 0)
                    .build()

            // Create an instance of the LLM Inference task and session.
            try {
                val llmInference = LlmInference.createFromOptions(context, options)

                val session =
                    LlmInferenceSession.createFromOptions(
                        llmInference,
                        LlmInferenceSession.LlmInferenceSessionOptions.builder()
                            .setTopK(topK)
                            .setTopP(topP)
                            .setTemperature(temperature)
                            .setGraphOptions(
                                GraphOptions.builder()
                                    .setEnableVisionModality(model.llmSupportImage).build()
                            )
                            .build(),
                    )
                model.instance = LlmModelInstance(engine = llmInference, session = session)
            } catch (e: Exception) {
                onDone(cleanUpMediapipeTaskErrorMessage(e.message ?: "Unknown error"))
                return
            }
            onDone("")
        }
    }

    fun resetSession(model: Model) {
        synchronized(lock) {
            try {
                Log.d(TAG, "Resetting session for model '${model.name}'")

                val instance = model.instance as LlmModelInstance? ?: return
                val session = instance.session
                session.close()

                val inference = instance.engine
                val topK =
                    model.getIntConfigValue(key = ConfigKey.TOPK, defaultValue = DEFAULT_TOPK)
                val topP =
                    model.getFloatConfigValue(key = ConfigKey.TOPP, defaultValue = DEFAULT_TOPP)
                val temperature =
                    model.getFloatConfigValue(
                        key = ConfigKey.TEMPERATURE,
                        defaultValue = DEFAULT_TEMPERATURE
                    )
                val newSession =
                    LlmInferenceSession.createFromOptions(
                        inference,
                        LlmInferenceSession.LlmInferenceSessionOptions.builder()
                            .setTopK(topK)
                            .setTopP(topP)
                            .setTemperature(temperature)
                            .setGraphOptions(
                                GraphOptions.builder()
                                    .setEnableVisionModality(model.llmSupportImage).build()
                            )
                            .build(),
                    )
                instance.session = newSession
                Log.d(TAG, "Resetting done")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to reset session", e)
            }
        }
    }

    fun cleanUp(model: Model) {
        synchronized(lock) {
            if (model.instance == null) {
                return
            }

            val instance = model.instance as LlmModelInstance

            try {
                instance.session.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close the LLM Inference session: ${e.message}")
            }

            try {
                instance.engine.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close the LLM Inference engine: ${e.message}")
            }

            val onCleanUp = cleanUpListeners.remove(model.name)
            if (onCleanUp != null) {
                onCleanUp()
            }
            model.instance = null
            Log.d(TAG, "Clean up done.")
        }
    }
    
    @Synchronized
    fun runInference(
        model: Model,
        input: String,
        resultListener: ResultListener,
        cleanUpListener: CleanUpListener,
        images: List<Bitmap> = listOf(),
    ) {
        val instance = model.instance as LlmModelInstance

        if (false) {
            Log.e(TAG, "Cannot run inference, model instance is null.")
            resultListener("Error: Model is not ready.", true)
            return
        }

        // Set listener.
        if (!cleanUpListeners.containsKey(model.name)) {
            cleanUpListeners[model.name] = cleanUpListener
        }

        val session = instance.session
        session.addQueryChunk(input)
        for (image in images) {
            session.addImage(BitmapImageBuilder(image).build())
        }
        session.generateResponseAsync(resultListener)
    }
}