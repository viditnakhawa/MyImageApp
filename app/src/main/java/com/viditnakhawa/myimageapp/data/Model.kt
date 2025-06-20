package com.viditnakhawa.myimageapp.data

import android.content.Context
import com.viditnakhawa.myimageapp.ui.common.PromptTemplate
import java.io.File

data class ModelDataFile(
    val name: String,
    val url: String,
    val downloadFileName: String,
    val sizeInBytes: Long,
)

enum class Accelerator(val label: String) {
    CPU(label = "CPU"), GPU(label = "GPU")
}

const val IMPORTS_DIR = "__imports"
private val NORMALIZE_NAME_REGEX = Regex("[^a-zA-Z0-9]")

/** A model for a task */
data class Model(
    val name: String,
    val version: String = "_",
    val downloadFileName: String,
    val url: String,
    val sizeInBytes: Long,
    val extraDataFiles: List<ModelDataFile> = listOf(),
    val info: String = "",
    val learnMoreUrl: String = "",
    val configs: List<Config> = listOf(),
    val showRunAgainButton: Boolean = true,
    val showBenchmarkButton: Boolean = true,
    val isZip: Boolean = false,
    val unzipDir: String = "",
    val llmPromptTemplates: List<PromptTemplate> = listOf(),
    val llmSupportImage: Boolean = false,
    val imported: Boolean = false,
    var normalizedName: String = "",
    var instance: Any? = null,
    var initializing: Boolean = false,
    var cleanUpAfterInit: Boolean = false,
    var configValues: Map<String, Any> = mapOf(),
    var totalBytes: Long = 0L,
    var accessToken: String? = null,
    val description: String,
) {
    init {
        normalizedName = NORMALIZE_NAME_REGEX.replace(name, "_")
    }

    fun preProcess() {
        val configValues: MutableMap<String, Any> = mutableMapOf()
        for (config in this.configs) {
            configValues[config.key.label] = config.defaultValue
        }
        this.configValues = configValues
        this.totalBytes = this.sizeInBytes + this.extraDataFiles.sumOf { it.sizeInBytes }
    }

    fun getPath(context: Context, fileName: String = downloadFileName): String {
        if (imported) {
            return listOf(context.getExternalFilesDir(null)?.absolutePath ?: "", fileName).joinToString(
                File.separator
            )
        }

        val baseDir =
            listOf(
                context.getExternalFilesDir(null)?.absolutePath ?: "",
                normalizedName,
                version
            ).joinToString(File.separator)
        return if (this.isZip && this.unzipDir.isNotEmpty()) {
            "$baseDir/${this.unzipDir}"
        } else {
            "$baseDir/$fileName"
        }
    }

    // Helper functions to get typed configuration values
    fun getIntConfigValue(key: ConfigKey, defaultValue: Int): Int {
        return (this.configValues[key.label] as? Number)?.toInt() ?: defaultValue
    }

    fun getFloatConfigValue(key: ConfigKey, defaultValue: Float): Float {
        return (this.configValues[key.label] as? Number)?.toFloat() ?: defaultValue
    }

    fun getStringConfigValue(key: ConfigKey, defaultValue: String): String {
        return this.configValues[key.label] as? String ?: defaultValue
    }
}

/** Data for a imported local model. */
data class ImportedModelInfo(
    val fileName: String,
    val fileSize: Long,
    val defaultValues: Map<String, Any>
)

enum class ModelDownloadStatusType {
    NOT_DOWNLOADED, PARTIALLY_DOWNLOADED, IN_PROGRESS, UNZIPPING, SUCCEEDED, FAILED,
}

data class ModelDownloadStatus(
    val status: ModelDownloadStatusType,
    val totalBytes: Long = 0,
    val receivedBytes: Long = 0,
    val errorMessage: String = "",
    val bytesPerSecond: Long = 0,
    val remainingMs: Long = 0,
)

enum class ConfigKey(val label: String) {
    MAX_TOKENS("Max tokens"),
    TOPK("TopK"),
    TOPP("TopP"),
    TEMPERATURE("Temperature"),
    DEFAULT_MAX_TOKENS("Default max tokens"),
    DEFAULT_TOPK("Default TopK"),
    DEFAULT_TOPP("Default TopP"),
    DEFAULT_TEMPERATURE("Default temperature"),
    SUPPORT_IMAGE("Support image"),
    MAX_RESULT_COUNT("Max result count"),
    USE_GPU("Use GPU"),
    ACCELERATOR("Choose accelerator"),
    COMPATIBLE_ACCELERATORS("Compatible accelerators"),
    WARM_UP_ITERATIONS("Warm up iterations"),
    BENCHMARK_ITERATIONS("Benchmark iterations"),
    ITERATIONS("Iterations"),
    THEME("Theme"),
    NAME("Name"),
    MODEL_TYPE("Model type")
}

