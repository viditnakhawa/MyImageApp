package com.viditnakhawa.myimageapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viditnakhawa.myimageapp.data.ASK_IMAGE_TASK
import com.viditnakhawa.myimageapp.data.GEMMA_E2B_MODEL
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.common.ViewModelProvider
import com.viditnakhawa.myimageapp.ui.common.modelitem.ModelItem
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.ui.modelmanager.TokenRequestResultType
import com.viditnakhawa.myimageapp.ui.modelmanager.TokenStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(onClose: () -> Unit) {
    val modelManagerViewModel: ModelManagerViewModel = viewModel(factory = ViewModelProvider.Factory)
    val uiState by modelManagerViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val task = ASK_IMAGE_TASK
    val model = GEMMA_E2B_MODEL

    val downloadStatus by remember { derivedStateOf { uiState.modelDownloadStatus[model.name] } }
    val initializationStatus by remember { derivedStateOf { uiState.modelInitializationStatus[model.name] } }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            modelManagerViewModel.downloadModel(task, model)
        } else {
            Toast.makeText(context, "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    val authResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        modelManagerViewModel.handleAuthResult(result) { tokenRequestResult ->
            if (tokenRequestResult.status == TokenRequestResultType.SUCCEEDED) {
                model.accessToken = modelManagerViewModel.curAccessToken
                checkNotificationPermissionAndStartDownload(context, permissionLauncher, modelManagerViewModel, task, model)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemma Model Manager") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // The ModelItem is now always visible and handles its own state.
            // This removes the jarring UI change.
            ModelItem(
                model = model,
                task = task,
                downloadStatus = downloadStatus,
                initializationStatus = initializationStatus, // Pass the initialization state
                onCancelClicked = { modelManagerViewModel.cancelDownload(model) },
                onDeleteClicked = { modelManagerViewModel.deleteDownload(model) },
                onInitializeClicked = { modelManagerViewModel.initializeModel(context, model) },
                onTryItClicked = {
                    onClose()
                    Toast.makeText(context, "${model.name} is ready to use!", Toast.LENGTH_SHORT).show()
                },
                onDownloadClicked = {
                    scope.launch(Dispatchers.IO) {
                        val tokenStatusAndData = modelManagerViewModel.getTokenStatusAndData()
                        if (tokenStatusAndData.status == TokenStatus.NOT_STORED || tokenStatusAndData.status == TokenStatus.EXPIRED) {
                            withContext(Dispatchers.Main) {
                                val authRequest = modelManagerViewModel.getAuthorizationRequest()
                                val authIntent = modelManagerViewModel.authService.getAuthorizationRequestIntent(authRequest)
                                authResultLauncher.launch(authIntent)
                            }
                            return@launch
                        }
                        val accessToken = tokenStatusAndData.data!!.accessToken
                        val responseCode = modelManagerViewModel.getModelUrlResponse(model, accessToken)
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            withContext(Dispatchers.Main) {
                                model.accessToken = accessToken
                                checkNotificationPermissionAndStartDownload(context, permissionLauncher, modelManagerViewModel, task, model)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                val authRequest = modelManagerViewModel.getAuthorizationRequest()
                                val authIntent = modelManagerViewModel.authService.getAuthorizationRequestIntent(authRequest)
                                authResultLauncher.launch(authIntent)
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun checkNotificationPermissionAndStartDownload(
    context: Context,
    launcher: ActivityResultLauncher<String>,
    modelManagerViewModel: ModelManagerViewModel,
    task: Task,
    model: Model
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            modelManagerViewModel.downloadModel(task, model)
        } else {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else {
        modelManagerViewModel.downloadModel(task, model)
    }
}