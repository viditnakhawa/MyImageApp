package com.viditnakhawa.myimageapp

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.viditnakhawa.myimageapp.data.AppContainer
import com.viditnakhawa.myimageapp.data.DefaultAppContainer

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_app_preferences")

class MyApplication : Application() {
    // This container will hold all our dependencies (repositories)
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Initialize the container and ML Kit
        container = DefaultAppContainer(this, dataStore)
        MLKitImgDescProcessor.initialize(applicationContext)
    }
}