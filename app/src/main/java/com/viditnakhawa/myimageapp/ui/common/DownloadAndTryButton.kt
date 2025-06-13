package com.viditnakhawa.myimageapp.ui.common


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viditnakhawa.myimageapp.data.Model
import com.viditnakhawa.myimageapp.data.Task
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.ui.modelmanager.TokenRequestResultType
import com.viditnakhawa.myimageapp.ui.modelmanager.TokenStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

@Composable
fun DownloadAndTryButton(
    task: Task,
    model: Model,
    enabled: Boolean,
    needToDownloadFirst: Boolean,
    modelManagerViewModel: ModelManagerViewModel,
    onClicked: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var checkingToken by remember { mutableStateOf(false) }
    var showAgreementAckSheet by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        modelManagerViewModel.downloadModel(task = task, model = model)
    }

    val startDownload: (accessToken: String?) -> Unit = { accessToken ->
        model.accessToken = accessToken
        onClicked()
        checkNotificationPermissionAndStartDownload(
            context = context,
            launcher = permissionLauncher,
            modelManagerViewModel = modelManagerViewModel,
            task = task,
            model = model
        )
        checkingToken = false
    }


    val authResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        modelManagerViewModel.handleAuthResult(result) { tokenRequestResult ->
            if (tokenRequestResult.status == TokenRequestResultType.SUCCEEDED) {
                model.accessToken = modelManagerViewModel.curAccessToken
                checkNotificationPermissionAndStartDownload(context, permissionLauncher, modelManagerViewModel, task, model)
            }
            checkingToken = false
        }
    }

    Button(
        onClick = {
            if (!enabled || checkingToken) return@Button

            if (!needToDownloadFirst) {
                onClicked()
                return@Button
            }

            scope.launch(Dispatchers.IO) {
                if (model.url.startsWith("https://huggingface.co")) {
                    checkingToken = true
                    val firstResponseCode = modelManagerViewModel.getModelUrlResponse(model = model)
                    if (firstResponseCode == HttpURLConnection.HTTP_OK) {
                        withContext(Dispatchers.Main) {
                            checkNotificationPermissionAndStartDownload(context, permissionLauncher, modelManagerViewModel, task, model)
                            checkingToken = false
                        }
                        return@launch
                    }

                    val tokenStatusAndData = modelManagerViewModel.getTokenStatusAndData()
                    if (tokenStatusAndData.status == TokenStatus.NOT_STORED || tokenStatusAndData.status == TokenStatus.EXPIRED) {
                        withContext(Dispatchers.Main) {
                            // CORRECTED LOGIC HERE
                            val authRequest = modelManagerViewModel.getAuthorizationRequest()
                            val authIntent = modelManagerViewModel.authService.getAuthorizationRequestIntent(authRequest)
                            authResultLauncher.launch(authIntent)
                        }
                    } else {
                        val responseCode = modelManagerViewModel.getModelUrlResponse(model, tokenStatusAndData.data!!.accessToken)
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            withContext(Dispatchers.Main) {
                                checkNotificationPermissionAndStartDownload(context, permissionLauncher, modelManagerViewModel, task, model)
                                checkingToken = false
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                // CORRECTED LOGIC HERE
                                val authRequest = modelManagerViewModel.getAuthorizationRequest()
                                val authIntent = modelManagerViewModel.authService.getAuthorizationRequestIntent(authRequest)
                                authResultLauncher.launch(authIntent)
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        checkNotificationPermissionAndStartDownload(context, permissionLauncher, modelManagerViewModel, task, model)
                    }
                }
            }
        },
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = "",
            modifier = Modifier.padding(end = 4.dp)
        )
        if (checkingToken) {
            Text("Checking access...")
        } else if (needToDownloadFirst) {
            Text("Download & Try")
        } else {
            Text("Try it")
        }
    }
}