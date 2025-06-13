/**package com.viditnakhawa.myimageapp.ui.common.modelitem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.common.TaskIcon
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.ui.common.getTaskBgColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType
import com.viditnakhawa.myimageapp.ui.common.checkNotificationPermissionAndStartDownload
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.ui.common.humanReadableSize
import com.viditnakhawa.myimageapp.ui.theme.MyImageAppTheme


@Composable
fun ModelItem(
    model: Model,
    task: Task,
    downloadStatus: ModelDownloadStatus?,
    onDownloadClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onTryItClicked: () -> Unit,
    modelManagerViewModel: ModelManagerViewModel, //OLD
    onModelClicked: (Model) -> Unit, //OLD
    modifier: Modifier = Modifier,
) {
    val isDownloaded = downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
    val context = LocalContext.current
    
    val uiState by modelManagerViewModel.uiState.collectAsState()
    val downloadStatus by remember {
        derivedStateOf { uiState.modelDownloadStatus[model.name] }
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        modelManagerViewModel.downloadModel(task = task, model = model)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 24.dp))
            .background(getTaskBgColor(task))
            .clickable(
                onClick = {
                    if (downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED) {
                        onModelClicked(model)
                    }
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TaskIcon(task = task)
            Column(Modifier.weight(1f)) {
                ModelNameAndStatus(model = model, downloadStatus = downloadStatus)
            }
            ModelItemActionButton(
                context = context,
                model = model,
                task = task,
                modelManagerViewModel = modelManagerViewModel,
                downloadStatus = downloadStatus,
                onDownloadClicked = {
                    checkNotificationPermissionAndStartDownload(
                        context, launcher, modelManagerViewModel, task, it
                    )
                }
            )
        }
    }
}*/

package com.viditnakhawa.myimageapp.ui.common.modelitem

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
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

@Composable
fun ModelItem(
    model: Model,
    task: Task,
    downloadStatus: ModelDownloadStatus?,
    onDownloadClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onTryItClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDownloaded = downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        // Use a standard Material You color for the background
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- Top Section: Icon, Name, and Cancel Button ---
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

            // --- Middle Section: Description Text ---
            Text(
                text = "Gemma is a family of lightweight, state-of-the-art open models from Google, " +
                        "built from the same research and technology used to create the Gemini models.",
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Progress Indicator Section ---
            if (downloadStatus?.status != ModelDownloadStatusType.NOT_DOWNLOADED) {
                ModelProgress(downloadStatus = downloadStatus, model = model)
            }

            // --- Bottom Section: Action Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // "Learn More" button now opens a URL
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(model.learnMoreUrl))
                    context.startActivity(intent)
                }) {
                    Text("Learn More")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (isDownloaded) {
                            onTryItClicked()
                        } else {
                            onDownloadClicked()
                        }
                    },
                    enabled = isDownloaded || downloadStatus?.status == ModelDownloadStatusType.NOT_DOWNLOADED || downloadStatus?.status == ModelDownloadStatusType.FAILED
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(if (isDownloaded) "Try it" else "Download")
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


