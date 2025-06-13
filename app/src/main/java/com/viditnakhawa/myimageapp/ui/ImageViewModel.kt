package com.viditnakhawa.myimageapp.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viditnakhawa.myimageapp.data.ImageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ImageViewModel(private val repository: ImageRepository) : ViewModel() {

    val images: StateFlow<List<Uri>> = repository.allImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
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

    fun removeImage(uri: Uri) {
        viewModelScope.launch {
            repository.deleteImage(uri)
        }
    }
}