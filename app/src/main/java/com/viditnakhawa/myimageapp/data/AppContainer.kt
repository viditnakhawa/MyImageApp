/*package com.viditnakhawa.myimageapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val context: Context
    val dataStoreRepository: DataStoreRepository
    val downloadRepository: DownloadRepository
    val imageRepository: ImageRepository
}

/**
 * Default implementation of the AppContainer interface.
 */
class AppDataContainer(private val ctx: Context,
                       dataStore: DataStore<Preferences>,
                       private val scope: CoroutineScope) : AppContainer {
    override val context = ctx

    override val dataStoreRepository by lazy {
        DefaultDataStoreRepository(dataStore)
    }
    override val downloadRepository by lazy {
        // We need a lifecycle provider, which we can create here
        DefaultDownloadRepository(ctx, GalleryLifecycleProvider())
    }
    // Add the implementation that provides the ImageRepository
    override val imageRepository: ImageRepository by lazy {
        ImageRepository(
            AppDatabase.getDatabase(context).imageDao(),
            context, // Pass context
            scope // Pass scope
        )
    }
}*/

