package com.viditnakhawa.myimageapp.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viditnakhawa.myimageapp.data.CollectionEntity
import com.viditnakhawa.myimageapp.data.CollectionWithImages
import com.viditnakhawa.myimageapp.data.ImageEntity
import com.viditnakhawa.myimageapp.data.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor
import com.viditnakhawa.myimageapp.processImageWithOCR
import com.viditnakhawa.myimageapp.workers.MultimodalAnalysisWorker

class ImageViewModel(
    private val repository: ImageRepository,
    private val workManager: WorkManager,
    private val applicationContext: Context
) : ViewModel() {

    val images: StateFlow<List<Uri>> = repository.allImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allImages: Flow<List<ImageEntity>> = repository.allImageEntities


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

    fun performOcr(uri: Uri, isGemmaInitialized: Boolean, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val rawText = processImageWithOCR(applicationContext, uri)
            if (isGemmaInitialized) {
                // Polishing logic will be handled by the ModelManagerViewModel
                // For now, we'll assume it's there.
                // In a further refactoring, we'd inject one ViewModel into the other.
                // For now, we update the DB directly.
                val currentDetails = getImageDetails(uri) ?: ImageEntity(uri.toString())
                // val polishedText = modelManagerViewModel.polishTextWithGemma(rawText)
                // For now, let's just save the raw text to a "polished" field to show it works.
                val updatedDetails = currentDetails.copy(polishedOcr = "[Polished] $rawText")
                updateImageDetails(updatedDetails)
                onResult(null) // Signal that the result is in the database
            } else {
                onResult(rawText) // Send raw text back to UI for temporary display
            }
        }
    }
}
