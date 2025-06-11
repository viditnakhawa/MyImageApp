package com.viditnakhawa.myimageapp.ui
/*
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.Model

@Composable
fun DownloadGemmaButton(
    model: Model,
    viewModel: ModelManagerViewModel
) {
    val context = LocalContext.current
    val status by viewModel.downloadStatus.observeAsState()
    var triedRedirect by remember { mutableStateOf(false) }

    Button(
        onClick = {
            val accessible = DownloadManager.checkIfAccessible(model)
            if (accessible || triedRedirect) {
                viewModel.downloadModel(model)
            } else {
                triedRedirect = true
                DownloadManager.openHuggingFaceLicensePage(context, model)
            }
        },
        enabled = status?.inProgress != true,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            when {
                status?.success == true -> "Downloaded"
                status?.inProgress == true -> "Downloading..."
                status?.failed == true -> "Retry Download"
                else -> "Download Gemma"
            }
        )
    }

    LaunchedEffect(status) {
        if (status?.success == true) {
            Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
        } else if (status?.failed == true) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }
}*/
