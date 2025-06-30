package com.viditnakhawa.myimageapp.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.viditnakhawa.myimageapp.data.ImageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "AutoAnalysisWorker"

@HiltWorker
class AutoAnalysisWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ImageRepository,
    private val workManager: WorkManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting automatic analysis of un-processed images.")
        return try {
            // This method already exists in your ImageRepository
            val unanalyzedImages = repository.getUnanalyzedImages()

            if (unanalyzedImages.isEmpty()) {
                Log.d(TAG, "No new images to analyze.")
                return Result.success()
            }

            Log.d(TAG, "Found ${unanalyzedImages.size} images to analyze.")
            for (image in unanalyzedImages) {
                // For each unanalyzed image, enqueue a specific MultimodalAnalysisWorker job.
                // This reuses your existing analysis logic.
                val analysisWorkRequest = OneTimeWorkRequestBuilder<MultimodalAnalysisWorker>()
                    .setInputData(workDataOf("IMAGE_URI" to image.imageUri))
                    .build()

                // Enqueue each job with a unique name to prevent duplicates
                workManager.enqueueUniqueWork(
                    "MultimodalAnalysis_${image.imageUri}",
                    ExistingWorkPolicy.KEEP, // KEEP: if a job for this image already exists, do nothing
                    analysisWorkRequest
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "AutoAnalysisWorker failed", e)
            Result.failure()
        }
    }
}