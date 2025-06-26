package com.viditnakhawa.myimageapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import com.viditnakhawa.myimageapp.data.AppDatabase
import com.viditnakhawa.myimageapp.data.AppLifecycleProvider
import com.viditnakhawa.myimageapp.data.DataStoreRepository
import com.viditnakhawa.myimageapp.data.DefaultDataStoreRepository
import com.viditnakhawa.myimageapp.data.DefaultDownloadRepository
import com.viditnakhawa.myimageapp.data.DownloadRepository
import com.viditnakhawa.myimageapp.data.GalleryLifecycleProvider
import com.viditnakhawa.myimageapp.data.ImageDao
import com.viditnakhawa.myimageapp.data.ImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

// Define the DataStore instance at the top level for the delegate to work
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    // Correctly provides the GalleryLifecycleProvider as the implementation for AppLifecycleProvider
    @Provides
    @Singleton
    fun provideAppLifecycleProvider(): AppLifecycleProvider {
        return GalleryLifecycleProvider()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideImageDao(appDatabase: AppDatabase): ImageDao {
        return appDatabase.imageDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // Binds the DataStoreRepository interface to the DefaultDataStoreRepository implementation
    @Provides
    @Singleton
    fun provideDataStoreRepository(dataStore: DataStore<Preferences>): DataStoreRepository {
        return DefaultDataStoreRepository(dataStore)
    }

    // Binds the DownloadRepository interface to the DefaultDownloadRepository implementation,
    // now with the correct AppLifecycleProvider dependency.
    @Provides
    @Singleton
    fun provideDownloadRepository(
        @ApplicationContext context: Context,
        lifecycleProvider: AppLifecycleProvider
    ): DownloadRepository {
        return DefaultDownloadRepository(context, lifecycleProvider)
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        imageDao: ImageDao,
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): ImageRepository {
        return ImageRepository(imageDao, context, scope)
    }
}
