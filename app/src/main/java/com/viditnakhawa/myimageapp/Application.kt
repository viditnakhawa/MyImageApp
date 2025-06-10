package com.viditnakhawa.myimageapp

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the ML Kit Image Describer
        MLKitImgDescProcessor.initialize(applicationContext)

        // Initialize our new Gemma integration
        //GemmaIntegration.initialize(applicationContext) THE USER CURRENTLY SIDE-LOADS THIS MODEL
    }
}