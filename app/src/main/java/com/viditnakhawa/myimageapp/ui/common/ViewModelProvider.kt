package com.viditnakhawa.myimageapp.ui.common

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viditnakhawa.myimageapp.MyApplication
import com.viditnakhawa.myimageapp.ui.ImageViewModel
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel

object ViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ModelManagerViewModel (This part is correct)
        initializer {
            val application = this.myApplication()
            ModelManagerViewModel(
                downloadRepository = application.container.downloadRepository,
                dataStoreRepository = application.container.dataStoreRepository,
                context = application.container.context,
            )
        }

        initializer {
            val application = this.myApplication()
            ImageViewModel(
                // The constructor only needs the repository, which we get from the container.
                repository = application.container.imageRepository
            )
        }
    }
}

/**
 * Extension function to get the MyApplication instance from CreationExtras.
 */
fun CreationExtras.myApplication(): MyApplication {
    return (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
}