package com.viditnakhawa.myimageapp.ui.common


import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viditnakhawa.myimageapp.ui.modelmanager.ModelManagerViewModel
import com.viditnakhawa.myimageapp.MyApplication

object ViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ModelManagerViewModel.
        initializer {
            val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
            ModelManagerViewModel(
                downloadRepository = application.container.downloadRepository,
                dataStoreRepository = application.container.dataStoreRepository,
                context = application.container.context,
            )
        }
        // You can add initializers for other ViewModels here in the future
    }
}