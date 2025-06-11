package com.viditnakhawa.myimageapp.data


import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.vector.ImageVector

/** Type of task. */
enum class TaskType(val label: String, val id: String) {
    LLM_ASK_IMAGE(label = "Ask Image", id = "llm_ask_image"),
    // You can add other types here if you expand your app later
}

/** Data class for a task listed in home screen. */
data class Task(
    val type: TaskType,
    val icon: ImageVector? = null,
    val iconVectorResourceId: Int? = null,
    val models: MutableList<Model>,
    val description: String,
    val docUrl: String = "",
    val sourceCodeUrl: String = "",
    @StringRes val agentNameRes: Int = 0,
    @StringRes val textInputPlaceHolderRes: Int = 0,
    var index: Int = -1,
    val updateTrigger: MutableState<Long> = mutableLongStateOf(0)
)