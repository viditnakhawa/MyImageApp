package com.viditnakhawa.myimageapp.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts the dominant color from an image URI. Runs on the IO dispatcher.
 * @param context The application context.
 * @param imageUri The URI of the image to analyze.
 * @return The dominant Color, or a fallback gray if extraction fails.
 */
suspend fun extractDominantColor(context: Context, imageUri: Uri): Color {
    return withContext(Dispatchers.IO) {
        try {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            val hardwareBitmap = ImageDecoder.decodeBitmap(source)
            val softwareBitmap = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, false)
            val palette = Palette.from(softwareBitmap).generate()
            val dominant = palette.getDominantColor(0xFFCCCCCC.toInt())
            Color(dominant)
        } catch (e: Exception) {
            e.printStackTrace()
            Color(0xFFCCCCCC) // Fallback gray
        }
    }
}

/**
 * Adjusts the brightness of a given color by a multiplication factor.
 * @param color The original color.
 * @param factor A float factor to adjust brightness (e.g., 0.8f to darken, 1.2f to lighten).
 * @return The adjusted Color.
 */
fun adjustColorBrightness(color: Color, factor: Float): Color {
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}
