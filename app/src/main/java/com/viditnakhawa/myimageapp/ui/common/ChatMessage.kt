package com.viditnakhawa.myimageapp.ui.common


/** This is just too resolve the import statement issues caused in Model.kt
     ChatMessage.kt doesn't has any real usages in this app*/

// In app/src/main/java/com/your/app/ui/common/chat/ChatMessage.kt

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import com.viditnakhawa.myimageapp.data.*

enum class ChatMessageType {
    INFO,
    WARNING,
    TEXT,
    IMAGE,
    IMAGE_WITH_HISTORY,
    LOADING,
    CLASSIFICATION,
    CONFIG_VALUES_CHANGE,
    BENCHMARK_RESULT,
    BENCHMARK_LLM_RESULT,
    PROMPT_TEMPLATES
}

enum class ChatSide {
    USER, AGENT, SYSTEM
}

data class Classification(val label: String, val score: Float, val color: Color)

/** Base class for a chat message. */
open class ChatMessage(
    open val type: ChatMessageType,
    open val side: ChatSide,
    open val latencyMs: Float = -1f,
    open val accelerator: String = "",
) {
    open fun clone(): ChatMessage {
        return ChatMessage(type = type, side = side, latencyMs = latencyMs)
    }
}

/** Chat message for showing loading status. */
class ChatMessageLoading(override val accelerator: String = "") :
    ChatMessage(type = ChatMessageType.LOADING, side = ChatSide.AGENT, accelerator = accelerator)

/** Chat message for info (help). */
class ChatMessageInfo(val content: String) :
    ChatMessage(type = ChatMessageType.INFO, side = ChatSide.SYSTEM)

/** Chat message for info (help). */
class ChatMessageWarning(val content: String) :
    ChatMessage(type = ChatMessageType.WARNING, side = ChatSide.SYSTEM)

/** Chat message for config values change. */
class ChatMessageConfigValuesChange(
    val model: Model,
    val oldValues: Map<String, Any>,
    val newValues: Map<String, Any>
) : ChatMessage(type = ChatMessageType.CONFIG_VALUES_CHANGE, side = ChatSide.SYSTEM)

/** Chat message for plain text. */
open class ChatMessageText(
    val content: String,
    override val side: ChatSide,
    override val latencyMs: Float = 0f,
    val isMarkdown: Boolean = true,
    var llmBenchmarkResult: ChatMessageBenchmarkLlmResult? = null,
    override val accelerator: String = "",
) : ChatMessage(
    type = ChatMessageType.TEXT,
    side = side,
    latencyMs = latencyMs,
    accelerator = accelerator
) {
    override fun clone(): ChatMessageText {
        return ChatMessageText(
            content = content,
            side = side,
            latencyMs = latencyMs,
            accelerator = accelerator,
            isMarkdown = isMarkdown,
            llmBenchmarkResult = llmBenchmarkResult,
        )
    }
}

/** Chat message for images. */
class ChatMessageImage(
    val bitmap: Bitmap,
    val imageBitMap: ImageBitmap,
    override val side: ChatSide,
    override val latencyMs: Float = 0f
) :
    ChatMessage(type = ChatMessageType.IMAGE, side = side, latencyMs = latencyMs) {
    override fun clone(): ChatMessageImage {
        return ChatMessageImage(
            bitmap = bitmap,
            imageBitMap = imageBitMap,
            side = side,
            latencyMs = latencyMs
        )
    }
}

/** Chat message for images with history. */
class ChatMessageImageWithHistory(
    val bitmaps: List<Bitmap>,
    val imageBitMaps: List<ImageBitmap>,
    val totalIterations: Int,
    override val side: ChatSide,
    override val latencyMs: Float = 0f,
    var curIteration: Int = 0, // 0-based
) :
    ChatMessage(type = ChatMessageType.IMAGE_WITH_HISTORY, side = side, latencyMs = latencyMs) {
    fun isRunning(): Boolean {
        return curIteration < totalIterations - 1
    }
}

/** Chat message for showing classification result. */
class ChatMessageClassification(
    val classifications: List<Classification>,
    override val latencyMs: Float = 0f,
    val maxBarWidth: Dp? = null,
) : ChatMessage(type = ChatMessageType.CLASSIFICATION, side = ChatSide.AGENT, latencyMs = latencyMs)

/** A stat used in benchmark result. */
data class Stat(val id: String, val label: String, val unit: String)

/** Chat message for showing benchmark result. */
class ChatMessageBenchmarkResult(
    val orderedStats: List<Stat>,
    val statValues: MutableMap<String, Float>,
    val values: List<Float>,
    val histogram: Histogram,
    val warmupCurrent: Int,
    val warmupTotal: Int,
    val iterationCurrent: Int,
    val iterationTotal: Int,
    override val latencyMs: Float = 0f,
    val highlightStat: String = "",
) :
    ChatMessage(
        type = ChatMessageType.BENCHMARK_RESULT,
        side = ChatSide.AGENT,
        latencyMs = latencyMs
    ) {
    fun isWarmingUp(): Boolean {
        return warmupCurrent < warmupTotal
    }

    fun isRunning(): Boolean {
        return iterationCurrent < iterationTotal
    }
}

/** Chat message for showing LLM benchmark result. */
class ChatMessageBenchmarkLlmResult(
    val orderedStats: List<Stat>,
    val statValues: MutableMap<String, Float>,
    val running: Boolean,
    override val latencyMs: Float = 0f,
    override val accelerator: String = "",
) : ChatMessage(
    type = ChatMessageType.BENCHMARK_LLM_RESULT,
    side = ChatSide.AGENT,
    latencyMs = latencyMs,
    accelerator = accelerator,
)

data class Histogram(
    val buckets: List<Int>,
    val maxCount: Int,
    val highlightBucketIndex: Int = -1
)

/** Chat message for showing prompt templates. */
class ChatMessagePromptTemplates(
    val templates: List<PromptTemplate>,
    val showMakeYourOwn: Boolean = true,
) : ChatMessage(type = ChatMessageType.PROMPT_TEMPLATES, side = ChatSide.SYSTEM)

// This is the class definition that was causing the error
data class PromptTemplate(val title: String, val description: String, val prompt: String)