package com.viditnakhawa.myimageapp

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

fun processImageWithOCR(
    context: Context,
    uri: Uri,
    onResult: (String) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                onResult(resultText)
            }
            .addOnFailureListener { e ->
                onResult("Error: ${e.message}")
                Log.e("MLKitOCR", "Text recognition failed", e)
            }
    } catch (e: IOException) {
        e.printStackTrace()
        onResult("Failed to load image: ${e.message}")
    }
}
