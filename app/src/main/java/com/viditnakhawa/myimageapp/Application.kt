package com.viditnakhawa.myimageapp

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.viditnakhawa.myimageapp.data.AppContainer
import com.viditnakhawa.myimageapp.data.DefaultAppContainer

// This line is from your GemmaModel project. It sets up DataStore.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_app_preferences")

class MyApplication : Application() {
    /** AppContainer instance from GemmaModel, used for dependency injection. */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        // --- Initialization from GemmaModel ---
        // Sets up the dependency container which provides repositories for downloading, etc.
        container = DefaultAppContainer(this, dataStore)

        // --- Original Initialization from MyImageApp ---
        // This initializes the ML Kit Image Description service.
        MLKitImgDescProcessor.initialize(applicationContext)
    }
}