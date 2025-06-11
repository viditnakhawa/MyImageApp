package com.viditnakhawa.myimageapp.data

import android.content.Context
import androidx.work.*
import java.util.UUID

interface DownloadRepository {
    fun downloadModel(model: Model, onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit)
    // Other methods can be added later
}

class DefaultDownloadRepository(
    private val context: Context,
    private val lifecycleProvider: AppLifecycleProvider
) : DownloadRepository {
    private val workManager = WorkManager.getInstance(context)

    override fun downloadModel(model: Model, onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit) {
        val inputData = Data.Builder()
            .putString(KEY_MODEL_URL, model.url)
            .putString(KEY_MODEL_NAME, model.name)
            .putString(KEY_MODEL_VERSION, model.version)
            .putString(KEY_MODEL_DOWNLOAD_MODEL_DIR, model.normalizedName)
            .putString(KEY_MODEL_DOWNLOAD_FILE_NAME, model.downloadFileName)
            .putLong(KEY_MODEL_TOTAL_BYTES, model.totalBytes)
            .putString(KEY_MODEL_DOWNLOAD_ACCESS_TOKEN, model.accessToken)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<com.viditnakhawa.myimageapp.worker.DownloadWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(model.name, ExistingWorkPolicy.REPLACE, downloadWorkRequest)
        observerWorkerProgress(downloadWorkRequest.id, model, onStatusUpdated)
    }

    private fun observerWorkerProgress(
        workerId: UUID,
        model: Model,
        onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit
    ) {
        workManager.getWorkInfoByIdLiveData(workerId).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val receivedBytes = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_RECEIVED_BYTES, 0L)
                        onStatusUpdated(model, ModelDownloadStatus(
                            status = ModelDownloadStatusType.IN_PROGRESS,
                            totalBytes = model.totalBytes,
                            receivedBytes = receivedBytes
                        ))
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        onStatusUpdated(model, ModelDownloadStatus(status = ModelDownloadStatusType.SUCCEEDED))
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        val errorMessage = workInfo.outputData.getString(KEY_MODEL_DOWNLOAD_ERROR_MESSAGE) ?: ""
                        onStatusUpdated(model, ModelDownloadStatus(status = ModelDownloadStatusType.FAILED, errorMessage = errorMessage))
                    }
                    else -> {}
                }
            }
        }
    }
}

// These are placeholder keys. You need to define them in a file like `Consts.kt`.
const val KEY_MODEL_URL = "KEY_MODEL_URL"
const val KEY_MODEL_NAME = "KEY_MODEL_NAME"
const val KEY_MODEL_VERSION = "KEY_MODEL_VERSION"
const val KEY_MODEL_DOWNLOAD_MODEL_DIR = "KEY_MODEL_DOWNLOAD_MODEL_DIR"
const val KEY_MODEL_DOWNLOAD_FILE_NAME = "KEY_MODEL_DOWNLOAD_FILE_NAME"
const val KEY_MODEL_TOTAL_BYTES = "KEY_MODEL_TOTAL_BYTES"
const val KEY_MODEL_DOWNLOAD_RECEIVED_BYTES = "KEY_MODEL_DOWNLOAD_RECEIVED_BYTES"
const val KEY_MODEL_DOWNLOAD_ERROR_MESSAGE = "KEY_MODEL_DOWNLOAD_ERROR_MESSAGE"
const val KEY_MODEL_DOWNLOAD_ACCESS_TOKEN = "KEY_MODEL_DOWNLOAD_ACCESS_TOKEN"
const val DEFAULT_BUFFER_SIZE = 4096