package com.viditnakhawa.myimageapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.imagedescription.*
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

data class PostDetails(
    val title: String = "Text Analysis",
    val author: String = "",
    val content: String = "",
    val stats: String = "",
    val replies: String = "",
    val rawText: String = "",
    val sourceApp: String? = null,
    val tags: List<String>? = null,
    val isFallback: Boolean = false,
    val polishedOcr: String? = null
)

object MLKitImgDescProcessor {

    private var imageDescriber: ImageDescriber? = null
    // Define a target size for the bitmaps to keep memory usage low.
    private const val TARGET_IMAGE_SIZE = 1024

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

            // Load the bitmap using our new, optimized function.
            val bitmap = uriToBitmap(context, imageUri)
                ?: return PostDetails(content = "Could not load image.")

            // --- FIX FOR RECYCLED BITMAP ---
            // Create a mutable copy to pass to the library. This ensures that even if
            // the library recycles its copy, our original bitmap remains safe.
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val request = ImageDescriptionRequest.builder(mutableBitmap).build()
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

    private suspend fun downloadModel(describer: ImageDescriber) {
        suspendCancellableCoroutine<Unit> { cont ->
            describer.downloadFeature(object : DownloadCallback {
                override fun onDownloadStarted(bytesToDownload: Long) {
                    Log.d("ImageDescriber", "Download started: $bytesToDownload bytes")
                }
                override fun onDownloadProgress(totalBytesDownloaded: Long) { }
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

    internal fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            // First, decode with inJustDecodeBounds=true to check dimensions without allocating memory
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}