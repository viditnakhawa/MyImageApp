package com.viditnakhawa.myimageapp.ui.common.modelitem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType


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
    }
    if (downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS ||
        downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
    ) {
        IconButton(onClick = onDeleteClicked) {
            Icon(Icons.Rounded.Close, contentDescription = "Delete")
        }
    }
}