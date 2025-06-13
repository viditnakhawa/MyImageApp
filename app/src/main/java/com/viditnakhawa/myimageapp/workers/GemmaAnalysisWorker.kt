package com.viditnakhawa.myimageapp.workers

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viditnakhawa.myimageapp.GemmaIntegration
import com.viditnakhawa.myimageapp.MyApplication
import com.viditnakhawa.myimageapp.processImageWithOCR

class GemmaAnalysisWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Get the image URI passed to the worker
        val imageUriString = inputData.getString("IMAGE_URI") ?: return Result.failure()
        val imageUri = Uri.parse(imageUriString)

        val repository = (applicationContext as MyApplication).container.imageRepository

        return try {
            // 1. Perform OCR
            val rawText = processImageWithOCR(applicationContext, imageUri)
            if (rawText.isBlank() || rawText.contains("Error:", ignoreCase = true)) {
                return Result.failure()
            }

            // 2. Perform Gemma Analysis
            val gemmaResponse = GemmaIntegration.analyzeText(rawText)

            // 3. Update the database with the new summary
            repository.updateImageSummary(imageUriString, gemmaResponse)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}