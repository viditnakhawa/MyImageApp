package com.viditnakhawa.myimageapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viditnakhawa.myimageapp.data.ASK_IMAGE_TASK
import com.viditnakhawa.myimageapp.data.GEMMA_E2B_MODEL
import com.viditnakhawa.myimageapp.data.ModelDownloadStatusType
import com.viditnakhawa.myimageapp.ui.common.ViewModelProvider
import com.viditnakhawa.myimageapp.ui.common.modelitem.ModelItem
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelInitializationStatusType
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(onClose: () -> Unit) {
    val modelManagerViewModel: ModelManagerViewModel = viewModel(factory = ViewModelProvider.Factory)
    val uiState by modelManagerViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // These values will be taken from your ModelDefinitions.kt file
    val task = ASK_IMAGE_TASK
    val model = GEMMA_E2B_MODEL

    val downloadStatus by remember {
        derivedStateOf { uiState.modelDownloadStatus[model.name] }
    }
    val initializationStatus by remember {
        derivedStateOf { uiState.modelInitializationStatus[model.name] }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemma 3n E2B Model") },
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
            if (downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED) {
                // After download, show initialization status and button
                when (initializationStatus?.status) {
                    ModelInitializationStatusType.INITIALIZED -> {
                        Text("Model '${model.name}' is downloaded and ready to use!")
                    }

                    ModelInitializationStatusType.INITIALIZING -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Initializing model...")
                    }

                    ModelInitializationStatusType.ERROR -> {
                        Text("Initialization failed: ${initializationStatus?.error}")
                    }

                    else -> { // NOT_INITIALIZED or null
                        Button(onClick = {
                            modelManagerViewModel.initializeModel(
                                context,
                                model
                            )
                        }) {
                            Text("Initialize Model")
                        }
                    }
                }
            }
            else {
                // This is the main UI component from your GemmaModel project
                ModelItem(
                    model = model,
                    task = task,
                    modelManagerViewModel = modelManagerViewModel,
                    onModelClicked = {
                        Toast.makeText(context, "${it.name} is ready!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}