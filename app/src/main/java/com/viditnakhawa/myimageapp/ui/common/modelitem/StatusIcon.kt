package com.viditnakhawa.myimageapp.ui.common.modelitem

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.ModelDownloadStatus
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType

@Composable
fun StatusIcon(downloadStatus: ModelDownloadStatus?, modifier: Modifier = Modifier) {
    when (downloadStatus?.status) {
        ModelDownloadStatusType.NOT_DOWNLOADED -> Icon(
            Icons.AutoMirrored.Outlined.HelpOutline,
            tint = Color.Gray,
            contentDescription = "Not Downloaded",
            modifier = modifier.size(18.dp)
        )
        ModelDownloadStatusType.SUCCEEDED -> {
            Icon(
                Icons.Filled.DownloadForOffline,
                tint = MaterialTheme.colorScheme.primary, // Or a success color
                contentDescription = "Downloaded",
                modifier = modifier.size(18.dp)
            )
        }
        ModelDownloadStatusType.FAILED -> Icon(
            Icons.Rounded.Error,
            tint = MaterialTheme.colorScheme.error,
            contentDescription = "Failed",
            modifier = modifier.size(18.dp)
        )
        ModelDownloadStatusType.IN_PROGRESS -> Icon(
            Icons.Rounded.Downloading,
            contentDescription = "Downloading",
            modifier = modifier.size(18.dp)
        )
        else -> {}
    }
}