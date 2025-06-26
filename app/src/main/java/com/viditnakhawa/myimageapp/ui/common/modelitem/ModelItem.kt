package com.viditnakhawa.myimageapp.ui.common.modelitem

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.common.TaskIcon
import com.viditnakhawa.myimageapp.ui.viewmodels.ModelInitializationStatus
import com.viditnakhawa.myimageapp.ui.viewmodels.ModelInitializationStatusType

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

                ModelNameAndStatus(model = model, downloadStatus = downloadStatus, modifier = Modifier.weight(1f))

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, model.learnMoreUrl.toUri())
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
