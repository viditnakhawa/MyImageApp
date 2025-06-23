package com.viditnakhawa.myimageapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.google.android.datatransport.BuildConfig
import com.viditnakhawa.myimageapp.data.AppContainer
import com.viditnakhawa.myimageapp.data.DefaultAppContainer

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_app_preferences")

class MyApplication : Application(),  ImageLoaderFactory{
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Initialize the container and ML Kit
        container = DefaultAppContainer(this, dataStore)
        MLKitImgDescProcessor.initialize(applicationContext)
    }

    @SuppressLint("ConstantConditionIf") //BuildConfig.debug()
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    // Set the memory cache to 25% of the app's available memory
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    // Set a fixed size of 250 MB for the disk cache
                    .maxSizeBytes(250 * 1024 * 1024)
                    .build()
            }
            //Cut memory usage in half for images without transparency.
            .bitmapConfig(Bitmap.Config.RGB_565)

            //Add a logger for debugging, but only in debug builds.
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }

            //Add a default placeholder for errors.
            // Note: You will need to create the 'ic_image_placeholder' drawable yourself.
            .error(R.drawable.gemma_color) // Using a default as an example

            //Skip unnecessary checks for local files.
            .respectCacheHeaders(false)

            .crossfade(true)
            .build()
    }
}
