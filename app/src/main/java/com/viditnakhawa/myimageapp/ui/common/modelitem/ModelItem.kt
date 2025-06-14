package com.viditnakhawa.myimageapp.ui.common.modelitem

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.common.TaskIcon
import com.viditnakhawa.myimageapp.ui.common.humanReadableSize
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelInitializationStatus
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelInitializationStatusType

@Composable
fun ModelItem(
    model: Model,
    task: Task,
    downloadStatus: ModelDownloadStatus?,
    initializationStatus: ModelInitializationStatus?,
    onDownloadClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onInitializeClicked: () -> Unit,
    onTryItClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDownloaded = downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
    val isInitializing = initializationStatus?.status == ModelInitializationStatusType.INITIALIZING
    val isInitialized = initializationStatus?.status == ModelInitializationStatusType.INITIALIZED
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TaskIcon(task = task, modifier = Modifier.size(40.dp))
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                ModelItemActionButton(
                    downloadStatus = downloadStatus,
                    onCancelClicked = onCancelClicked,
                    onDeleteClicked = onDeleteClicked
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gemma is a family of lightweight, state-of-the-art open models from Google, " +
                        "built from the same research and technology used to create the Gemini models.",
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (downloadStatus?.status != ModelDownloadStatusType.NOT_DOWNLOADED) {
                ModelProgress(downloadStatus = downloadStatus, model = model)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(model.learnMoreUrl))
                    context.startActivity(intent)
                }) {
                    Text("Learn More")
                }
                Spacer(modifier = Modifier.width(8.dp))
                when {
                    isInitializing -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Initializing...")
                    }
                    isInitialized -> {
                        Button(onClick = onTryItClicked, enabled = true) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Ready")
                        }
                    }
                    isDownloaded -> {
                        Button(onClick = onInitializeClicked) {
                            Text("Initialize")
                        }
                    }
                    else -> {
                        Button(
                            onClick = onDownloadClicked,
                            enabled = downloadStatus?.status == ModelDownloadStatusType.NOT_DOWNLOADED || downloadStatus?.status == ModelDownloadStatusType.FAILED
                        ) {
                            Text("Download")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelProgress(downloadStatus: ModelDownloadStatus?, model: Model) {
    val inProgress = downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS
    Column {
        if (downloadStatus?.status == ModelDownloadStatusType.FAILED) {
            Text(
                downloadStatus.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
            )
        } else {
            val statusLabel = if (inProgress) {
                val totalSize = downloadStatus?.totalBytes ?: model.totalBytes
                val received = downloadStatus?.receivedBytes ?: 0
                "${received.humanReadableSize()} / ${totalSize.humanReadableSize()}"
            } else {
                model.totalBytes.humanReadableSize()
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
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1.0f),
        )
    }
}