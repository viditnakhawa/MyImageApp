package com.viditnakhawa.myimageapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.viditnakhawa.myimageapp.data.GalleryLifecycleProvider


/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val context: Context
    val dataStoreRepository: DataStoreRepository
    val downloadRepository: DownloadRepository
}

/**
 * Default implementation of the AppContainer interface.
 */
class DefaultAppContainer(private val ctx: Context, dataStore: DataStore<Preferences>) : AppContainer {
    override val context = ctx
    override val dataStoreRepository by lazy {
        DefaultDataStoreRepository(dataStore)
    }
    override val downloadRepository by lazy {
        // We need a lifecycle provider, which we can create here
        DefaultDownloadRepository(ctx, GalleryLifecycleProvider())
    }
}

