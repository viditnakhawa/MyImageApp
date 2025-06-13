package com.viditnakhawa.myimageapp.ui.common.modelitem

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.common.DownloadAndTryButton
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel

/*@Composable
fun ModelItemActionButton(
    context: Context,
    model: Model,
    task: Task,
    modelManagerViewModel: ModelManagerViewModel,
    downloadStatus: ModelDownloadStatus?,
    onDownloadClicked: (Model) -> Unit,
) {
    when (downloadStatus?.status) {
        ModelDownloadStatusType.NOT_DOWNLOADED, ModelDownloadStatusType.FAILED ->
            DownloadAndTryButton(
                task = task,
                model = model,
                enabled = true,
                needToDownloadFirst = true,
                modelManagerViewModel = modelManagerViewModel,
                onClicked = { onDownloadClicked(model) }
            )

        ModelDownloadStatusType.IN_PROGRESS -> IconButton(onClick = {
            // TODO: Implement cancel logic in ViewModel and Repository
            modelManagerViewModel.cancelDownload(model)
        }) {
            Icon(Icons.Rounded.Cancel, contentDescription = "Cancel")
        }
        else -> {}
    }
}*/

@Composable
fun ModelItemActionButton(
    downloadStatus: ModelDownloadStatus?,
    onCancelClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    // This composable now ONLY shows the cancel button when a download is in progress.
    if (downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS) {
        IconButton(onClick = onCancelClicked) {
            Icon(Icons.Rounded.Pause, contentDescription = "Pause")
        }
        IconButton(onClick = onDeleteClicked) {
            Icon(Icons.Rounded.Cancel, contentDescription = "Delete")
        }
    }
}