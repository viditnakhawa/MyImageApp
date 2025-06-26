/*package com.viditnakhawa.myimageapp.ui.common

import androidx.work.WorkManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viditnakhawa.myimageapp.MyApplication
import com.viditnakhawa.myimageapp.ui.viewmodels.ImageViewModel
import com.viditnakhawa.myimageapp.ui.viewmodels.ModelManagerViewModel

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
            ModelManagerViewModel(
                downloadRepository = application.container.downloadRepository,
                dataStoreRepository = application.container.dataStoreRepository,
                context = application.applicationContext
            )
        }

        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
            ImageViewModel(
                repository = application.container.imageRepository,
                workManager = WorkManager.getInstance(application.applicationContext),
                applicationContext = application.applicationContext
            )
        }
    }
}*/
