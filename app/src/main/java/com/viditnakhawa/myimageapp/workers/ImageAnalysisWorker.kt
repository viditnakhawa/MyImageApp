package com.viditnakhawa.myimageapp.workers

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor
import com.viditnakhawa.myimageapp.MyApplication
import com.viditnakhawa.myimageapp.data.ImageRepository

class ImageAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val imageUriString = inputData.getString("IMAGE_URI") ?: return Result.failure()
        val repository = (applicationContext as MyApplication).container.imageRepository

        return try {
            val result = MLKitImgDescProcessor.describeImage(applicationContext, Uri.parse(imageUriString))
            repository.updateImageSummary(imageUriString, result.content)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
