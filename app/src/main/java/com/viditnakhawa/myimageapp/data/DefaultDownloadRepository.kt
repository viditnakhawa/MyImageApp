package com.viditnakhawa.myimageapp.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.viditnakhawa.myimageapp.R
import com.viditnakhawa.myimageapp.workers.DownloadWorker
import java.util.UUID

interface DownloadRepository {
    fun downloadModel(model: Model, onStatusUpdated: (model: Model, status: ModelDownloadStatus) -> Unit)
    // Other methods can be added later
    fun cancelDownload(model: Model)
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

        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(model.name, ExistingWorkPolicy.REPLACE, downloadWorkRequest)
        observerWorkerProgress(downloadWorkRequest.id, model, onStatusUpdated)
    }

    override fun cancelDownload(model: Model) {
        // Use the same unique name to cancel the specific work.
        workManager.cancelUniqueWork(model.name)
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
                        val downloadRate = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_RATE, 0L)
                        val remainingMs = workInfo.progress.getLong(KEY_MODEL_DOWNLOAD_REMAINING_MS, 0L)

                        if (receivedBytes != 0L) {
                            onStatusUpdated(model, ModelDownloadStatus(
                                status = ModelDownloadStatusType.IN_PROGRESS,
                                totalBytes = model.totalBytes,
                                receivedBytes = receivedBytes,
                                bytesPerSecond = downloadRate,
                                remainingMs = remainingMs,
                            ))
                        }
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        onStatusUpdated(model, ModelDownloadStatus(status = ModelDownloadStatusType.SUCCEEDED))
                        sendNotification(
                            title = context.getString(R.string.notification_title_success),
                            text = context.getString(R.string.notification_content_success).format(model.name)
                        )
                    }
                    WorkInfo.State.FAILED -> {
                        val errorMessage = workInfo.outputData.getString(KEY_MODEL_DOWNLOAD_ERROR_MESSAGE) ?: ""
                        onStatusUpdated(model, ModelDownloadStatus(status = ModelDownloadStatusType.FAILED, errorMessage = errorMessage))
                        sendNotification(
                            title = context.getString(R.string.notification_title_fail),
                            text = context.getString(R.string.notification_content_fail).format(model.name)
                        )
                    }
                    WorkInfo.State.CANCELLED -> {
                        onStatusUpdated(model, ModelDownloadStatus(status = ModelDownloadStatusType.NOT_DOWNLOADED))
                    }
                    else -> {}
                }
            }
        }
    }

    private fun sendNotification(title: String, text: String) {
        if (lifecycleProvider.isAppInForeground) return

        val channelId = "download_notification_channel"
        val channelName = "Model Downloads"
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}