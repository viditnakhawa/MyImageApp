package com.viditnakhawa.myimageapp.ui.common.modelitem

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
    modelManagerViewModel: ModelManagerViewModel,
    onModelClicked: (Model) -> Unit,
    modifier: Modifier = Modifier,
) {
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
}

