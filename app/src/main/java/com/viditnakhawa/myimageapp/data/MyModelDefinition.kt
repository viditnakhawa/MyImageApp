package com.viditnakhawa.myimageapp.data


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mms
import com.viditnakhawa.myimageapp.ui.common.createLlmChatConfigs

// 1. Define the specific model using data from the original model_allowlist.json
val GEMMA_E2B_MODEL = Model(
    name = "Gemma-3n-E2B-it-int4",
    // These details are taken directly from the allowlist file
    version = "20250520",
    llmSupportImage = true,
    sizeInBytes = 3136226711,
    url = "https://huggingface.co/google/gemma-3n-E2B-it-litert-preview/resolve/main/gemma-3n-E2B-it-int4.task?download=true",
    downloadFileName = "gemma-3n-E2B-it-int4.task",
    learnMoreUrl = "https://huggingface.co/google/gemma-3n-E2B-it-litert-preview",
    description = "Preview version of Gemma 3n E2B ready for deployment on Android.",
    // These configs are essential for the model to run correctly
    configs = createLlmChatConfigs(
        defaultTopK = 64,
        defaultTopP = 0.95f,
        defaultTemperature = 1.0f,
        defaultMaxToken = 4096,
        accelerators = listOf(Accelerator.CPU, Accelerator.GPU)
    ),
    showBenchmarkButton = false, // Simplified for your use case
    showRunAgainButton = false   // Simplified for your use case
).apply {
    // This helper call initializes internal values like totalBytes
    preProcess()
}

// 2. Define a single task that holds only this model
val ASK_IMAGE_TASK = Task(
    type = TaskType.LLM_ASK_IMAGE,
    icon = Icons.Outlined.Mms,
    models = mutableListOf(GEMMA_E2B_MODEL),
    description = "Ask questions about images with Gemma E2B."
).apply {
    this.index = 0 // Manually set index since it's the only task
}