package com.viditnakhawa.myimageapp

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//SUSPEND FUNCTION for easier use in coroutines.
suspend fun processImageWithOCR(context: Context, uri: Uri): String = suspendCoroutine {
    continuation ->
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text)
            }
            .addOnFailureListener { e ->
                Log.e("MLKitOCR", "Text recognition failed", e)
                continuation.resume("Error: ${e.message}")
            }
    } catch (e: IOException) {
        e.printStackTrace()
        continuation.resume("Failed to load image: ${e.message}")
    }
}
