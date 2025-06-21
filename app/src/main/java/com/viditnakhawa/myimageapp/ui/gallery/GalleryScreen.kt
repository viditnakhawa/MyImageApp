package com.viditnakhawa.myimageapp.ui.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viditnakhawa.myimageapp.ui.ImageViewModel
import com.viditnakhawa.myimageapp.ui.ScreenshotsGalleryScreenWithFAB
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel

@Composable
fun GalleryScreen(
    imageViewModel: ImageViewModel,
    modelManagerViewModel: ModelManagerViewModel,
    onNavigateToAnalysis: (Uri) -> Unit,
    onNavigateToCollections: () -> Unit,
    onCreateCollection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCamera: () -> Unit,
) {
    val imageEntities by imageViewModel.allImages.collectAsStateWithLifecycle(initialValue = emptyList())
    //val imageList by imageViewModel.images.collectAsStateWithLifecycle()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                imageViewModel.addImages(uris)
            }
        }
    )

    ScreenshotsGalleryScreenWithFAB(
        images = imageEntities,
        onViewCollectionsClick = onNavigateToCollections,
        onCreateCollectionClick = onCreateCollection,
        onImageClick = { imageEntity ->
            val uri = imageEntity.imageUri.toUri()
            imageViewModel.prepareForAnalysis(uri, modelManagerViewModel.isGemmaInitialized())
            onNavigateToAnalysis(uri)
        },
        onCapturePhotoClick = onNavigateToCamera,
        onPickFromGalleryClick = { pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onSettingsClick = onNavigateToSettings
    )
}