package com.viditnakhawa.myimageapp

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize ML Kit services
        MLKitImgDescProcessor.initialize(applicationContext)

    }
}