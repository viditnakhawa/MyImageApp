package com.viditnakhawa.myimageapp

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()
        MLKitImgDescProcessor.initialize(applicationContext)
        //scheduleAutoAnalysis()
    }

    /*private fun scheduleAutoAnalysis() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val autoAnalysisRequest = PeriodicWorkRequestBuilder<AutoAnalysisWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        // Enqueue the unique work
        workManager.enqueueUniquePeriodicWork(
            "AutoImageAnalysis",
            ExistingPeriodicWorkPolicy.KEEP,
            autoAnalysisRequest
        )
    }*/

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @SuppressLint("ConstantConditionIf")
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250 * 1024 * 1024)
                    .build()
            }
            // --- Add these dispatchers for more robust loading ---
            .dispatcher(Dispatchers.IO) // Use a dedicated IO dispatcher
            .fetcherDispatcher(Dispatchers.IO)
            .decoderDispatcher(Dispatchers.Default)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .apply {
                if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    logger(DebugLogger())
                }
            }
            .error(R.drawable.gemma_color)
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
