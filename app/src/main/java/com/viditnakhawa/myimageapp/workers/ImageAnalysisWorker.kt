package com.viditnakhawa.myimageapp.workers

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viditnakhawa.myimageapp.MLKitImgDescProcessor
import com.viditnakhawa.myimageapp.data.ImageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class ImageAnalysisWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ImageRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val imageUriString = inputData.getString("IMAGE_URI") ?: return Result.failure()

        return try {
            val result = MLKitImgDescProcessor.describeImage(
                applicationContext,
                imageUriString.toUri()
            )
            repository.updateImageSummary(imageUriString, result.content)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
