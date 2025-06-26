package com.viditnakhawa.myimageapp.ui.common.modelitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType
import com.viditnakhawa.myimageapp.ui.common.formatToHourMinSecond
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
                val totalSize = downloadStatus.totalBytes
                val received = downloadStatus.receivedBytes
                val remainingTime = downloadStatus.remainingMs.formatToHourMinSecond()

                statusLabel = "${received.humanReadableSize()} / ${totalSize.humanReadableSize()}"

                if (remainingTime.isNotBlank() && downloadStatus.bytesPerSecond > 0) {
                    statusLabel += " - $remainingTime remaining"
                }
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
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1.0f),
        )
    }
}