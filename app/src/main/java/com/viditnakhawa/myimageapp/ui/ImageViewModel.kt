package com.viditnakhawa.myimageapp.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageViewModel : ViewModel() {
    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images = _images.asStateFlow()

    fun addImage(uri: Uri) {
        _images.value = listOf(uri) + _images.value // Add new images to the top
    }

    fun removeImage(uri: Uri) {
        _images.value = _images.value - uri
    }
}