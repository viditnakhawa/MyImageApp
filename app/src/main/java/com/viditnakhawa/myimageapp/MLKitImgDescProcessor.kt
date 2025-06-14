package com.viditnakhawa.myimageapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.imagedescription.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class PostDetails(
    val title: String = "Text Analysis",
    val author: String = "",
    val content: String = "",
    val stats: String = "",
    val replies: String = "",
    val rawText: String = "",
    val sourceApp: String? = null,
    val tags: List<String>? = null,
    val isFallback: Boolean = false
)

object MLKitImgDescProcessor {

    private var imageDescriber: ImageDescriber? = null

    fun initialize(context: Context) {
        val options = ImageDescriberOptions.builder(context).build()
        imageDescriber = ImageDescription.getClient(options)
        Log.d("ImageDescriber", "ML Kit ImageDescriber initialized.")
    }

    suspend fun describeImage(context: Context, imageUri: Uri): PostDetails {
        val describer = imageDescriber
            ?: return PostDetails(content = "Error: Image Describer not initialized.")

        try {
            val status = describer.checkFeatureStatus().await()
            when (status) {
                FeatureStatus.AVAILABLE -> {
                    Log.d("ImageDescriber", "Model already available.")
                }

                FeatureStatus.DOWNLOADABLE -> {
                    Log.d("ImageDescriber", "Downloading model...")
                    downloadModel(describer)
                    Log.d("ImageDescriber", "Model downloaded successfully.")
                }

                FeatureStatus.DOWNLOADING -> {
                    Log.d("ImageDescriber", "Model is downloading.")
                    return PostDetails(
                        title = "Model Downloading",
                        content = "Model is downloading. Please try again shortly."
                    )
                }

                else -> {
                    Log.e("ImageDescriber", "Model unavailable: $status")
                    return PostDetails(content = "Model unavailable: $status")
                }
            }

            val bitmap = uriToBitmap(context, imageUri)
                ?: return PostDetails(content = "Could not load image.")

            val request = ImageDescriptionRequest.builder(bitmap).build()
            val result = describer.runInference(request).await()

            return PostDetails(
                title = "Image Description",
                content = result.description ?: "No description generated."
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return PostDetails(content = "Error: ${e.message}")
        }
    }

    // Coroutine wrapper for downloadFeature()
    private suspend fun downloadModel(describer: ImageDescriber) {
        suspendCancellableCoroutine<Unit> { cont ->
            describer.downloadFeature(object : DownloadCallback {
                override fun onDownloadStarted(bytesToDownload: Long) {
                    Log.d("ImageDescriber", "Download started: $bytesToDownload bytes")
                }

                override fun onDownloadProgress(totalBytesDownloaded: Long) {
                    // Optional: Log or ignore
                }

                override fun onDownloadCompleted() {
                    Log.d("ImageDescriber", "Download completed.")
                    cont.resume(Unit)
                }

                override fun onDownloadFailed(e: GenAiException) {
                    Log.e("ImageDescriber", "Download failed: ${e.message}")
                    cont.resumeWithException(e)
                }
            })
        }
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}