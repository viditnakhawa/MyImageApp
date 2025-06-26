package com.viditnakhawa.myimageapp.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor.uriToBitmap
import com.viditnakhawa.myimageapp.data.CollectionEntity
import com.viditnakhawa.myimageapp.data.CollectionWithImages
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.data.ImageRepository
import com.viditnakhawa.myimageapp.processImageWithOCR
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.workers.MultimodalAnalysisWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ImageViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val allImages: Flow<List<ImageEntity>> = repository.allImageEntities
    private val allDatabaseImages = repository.allImageEntities

    init {
        viewModelScope.launch {
            repository.syncScreenshotsFromDevice()
        }
    }

    val images: StateFlow<List<Uri>> = repository.allImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )


    val collections: StateFlow<List<CollectionEntity>> = repository.collections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addImage(uri: Uri) {
        viewModelScope.launch {
            repository.addImage(uri)
        }
    }

    fun addImages(uris: List<Uri>) {
        viewModelScope.launch {
            uris.forEach { repository.addImage(it) }
        }
    }

    fun ignoreImage(uri: Uri) {
        viewModelScope.launch {
            repository.ignoreImage(uri)
        }
    }

    //PHASE 1 CODE
    suspend fun getImageDetails(uri: Uri): ImageEntity? {
        return repository.getImageDetails(uri)
    }

    //PHASE 1 CODE
    suspend fun getImageDetailsFlow(uri: Uri): Flow<ImageEntity?> {
        return repository.getImageDetailsFlow(uri)
    }

    //PHASE 1 CODE
    fun updateImageDetails(imageDetails: ImageEntity) {
        viewModelScope.launch {
            repository.updateImageDetails(imageDetails)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchedImages: StateFlow<List<ImageEntity>> = combine(
        allDatabaseImages,
        searchQuery.debounce(300) // Debounce adds a small delay for a better user experience
    ) { images, query ->
        if (query.isBlank()) {
            images
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            images.filter { image ->
                image.title?.lowercase()?.contains(lowerCaseQuery) == true ||
                        image.content?.lowercase()?.contains(lowerCaseQuery) == true ||
                        image.polishedOcr?.lowercase()?.contains(lowerCaseQuery) == true ||
                        image.tags?.any { tag -> tag.lowercase().contains(lowerCaseQuery) } == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun createCollection(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createCollection(name)
        }
    }

    fun addImageToCollection(imageUri: String, collectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addImageToCollection(imageUri, collectionId)
        }
    }

    // NEW: Expose collections with their images to the UI
    val collectionsWithImages: StateFlow<List<CollectionWithImages>> = repository.collectionsWithImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun createCollectionAndReturnId(name: String): Long {
        return withContext(Dispatchers.IO) {
            repository.createCollectionAndGetId(name)
        }
    }

    fun addImagesToCollection(imageUris: List<String>, collectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addImagesToCollection(imageUris, collectionId)
        }
    }

    fun getCollectionById(collectionId: Long): Flow<CollectionWithImages?> {
        return repository.getCollectionWithImagesById(collectionId)
    }

    fun removeImagesFromCollection(uris: List<String>, collectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeImagesFromCollection(uris, collectionId)
        }
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCollectionById(collectionId)
        }
    }

    fun updateCollectionName(collectionId: Long, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCollectionName(collectionId, newName)
        }
    }

    fun prepareForAnalysis(uri: Uri, isGemmaInitialized: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageDetails = repository.getImageDetails(uri)

            // Logic to decide if Gemma analysis is needed
            if (isGemmaInitialized && imageDetails?.sourceApp == null) {
                val analysisWorkRequest = OneTimeWorkRequestBuilder<MultimodalAnalysisWorker>()
                    .setInputData(workDataOf("IMAGE_URI" to uri.toString()))
                    .build()
                workManager.enqueueUniqueWork(
                    "MultimodalAnalysis_${uri}",
                    ExistingWorkPolicy.KEEP,
                    analysisWorkRequest
                )
            }
            // Logic to run fallback analysis if Gemma isn't ready
            else if (!isGemmaInitialized && imageDetails?.title == null) {
                val fallbackAnalysis = MLKitImgDescProcessor.describeImage(applicationContext, uri)
                val entityToSave = ImageEntity(
                    imageUri = uri.toString(),
                    title = fallbackAnalysis.title,
                    content = fallbackAnalysis.content
                )
                updateImageDetails(entityToSave)
            }
        }
    }

    fun performOcrOnImage(uri: Uri, isGemmaReady: Boolean, modelManagerViewModel: ModelManagerViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val extractedText: String
            if (isGemmaReady) {
                // Primary Path: Use Gemma for direct, high-quality OCR
                val bitmap = uriToBitmap(applicationContext, uri) ?: return@launch
                extractedText = modelManagerViewModel.extractTextFromImageWithGemma(bitmap)
            } else {
                // Fallback Path: Use ML Kit for reliable raw OCR
                extractedText = processImageWithOCR(applicationContext, uri)
            }
            val currentDetails = getImageDetails(uri) ?: ImageEntity(uri.toString())
            val updatedDetails = currentDetails.copy(polishedOcr = extractedText)
            updateImageDetails(updatedDetails)
        }
    }
}

