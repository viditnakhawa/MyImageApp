package com.viditnakhawa.myimageapp.ui.common.modelitem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType
import com.viditnakhawa.myimageapp.ui.common.humanReadableSize

@Composable
fun ModelNameAndStatus(
    model: Model,
    downloadStatus: ModelDownloadStatus?,
    modifier: Modifier = Modifier
) {
    val inProgress = downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS

    Column {
        Text(
            model.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )

        if (downloadStatus?.status == ModelDownloadStatusType.FAILED) {
            Text(
                downloadStatus.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        } else {
            var statusLabel = model.totalBytes.humanReadableSize()
            if (inProgress) {
                val totalSize = downloadStatus?.totalBytes ?: model.totalBytes
                val received = downloadStatus?.receivedBytes ?: 0
                statusLabel = "${received.humanReadableSize()} / ${totalSize.humanReadableSize()}"
            }
            Text(
                statusLabel,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        val safeProgress = when {
            downloadStatus == null || downloadStatus.totalBytes == 0L -> 0f
            else -> (downloadStatus.receivedBytes.toFloat() / downloadStatus.totalBytes.toFloat()).coerceIn(0f, 1f)
        }

        LinearProgressIndicator(
            progress = { safeProgress },
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.primary, // The color of the progress bar itself
            trackColor = MaterialTheme.colorScheme.surfaceVariant, // The color of the bar's background
        )
    }
}