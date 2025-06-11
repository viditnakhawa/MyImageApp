package com.viditnakhawa.myimageapp.data

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

interface AppLifecycleProvider {
    val isAppInForeground: Boolean
}

class GalleryLifecycleProvider : AppLifecycleProvider, DefaultLifecycleObserver {
    private var _isAppInForeground = false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override val isAppInForeground: Boolean
        get() = _isAppInForeground

    override fun onResume(owner: LifecycleOwner) {
        _isAppInForeground = true
    }

    override fun onPause(owner: LifecycleOwner) {
        _isAppInForeground = false
    }
}