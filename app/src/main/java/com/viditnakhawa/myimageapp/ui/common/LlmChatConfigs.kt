package com.viditnakhawa.myimageapp.ui.common


import android.content.Context
import android.util.Log
import com.viditnakhawa.myimageapp.data.*

const val DEFAULT_MAX_TOKEN = 1024
const val DEFAULT_TOPK = 40
const val DEFAULT_TOPP = 0.9f
const val DEFAULT_TEMPERATURE = 1.0f
val DEFAULT_ACCELERATORS = listOf(Accelerator.GPU)

fun createLlmChatConfigs(
    defaultMaxToken: Int = DEFAULT_MAX_TOKEN,
    defaultTopK: Int = DEFAULT_TOPK,
    defaultTopP: Float = DEFAULT_TOPP,
    defaultTemperature: Float = DEFAULT_TEMPERATURE,
    accelerators: List<Accelerator> = DEFAULT_ACCELERATORS,
): List<Config> {
    return listOf(
        LabelConfig(
            key = ConfigKey.MAX_TOKENS,
            defaultValue = "$defaultMaxToken",
        ),
        NumberSliderConfig(
            key = ConfigKey.TOPK,
            sliderMin = 5f,
            sliderMax = 40f,
            defaultValue = defaultTopK.toFloat(),
            valueType = ValueType.INT
        ),
        NumberSliderConfig(
            key = ConfigKey.TOPP,
            sliderMin = 0.0f,
            sliderMax = 1.0f,
            defaultValue = defaultTopP,
            valueType = ValueType.FLOAT
        ),
        NumberSliderConfig(
            key = ConfigKey.TEMPERATURE,
            sliderMin = 0.0f,
            sliderMax = 2.0f,
            defaultValue = defaultTemperature,
            valueType = ValueType.FLOAT
        ),
        SegmentedButtonConfig(
            key = ConfigKey.ACCELERATOR,
            defaultValue = accelerators[0].label,
            options = accelerators.map { it.label }
        )
    )
}

object LlmChatModelHelper {
    fun initialize(context: Context, model: Model, onDone: (String) -> Unit) {
        // Placeholder for actual MediaPipe LlmInference initialization
        // In a real scenario, this would create the inference engine instance.
        Log.d("LlmChatModelHelper", "Simulating initialization for ${model.name}")
        model.instance = Any() // Simulate a successful instance creation
        onDone("")
    }
}