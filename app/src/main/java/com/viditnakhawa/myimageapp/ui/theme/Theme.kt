package com.viditnakhawa.myimageapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Theme definition ---
private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    secondaryContainer = secondaryContainerLight,
    background = backgroundLight,
    surface = surfaceLight,
    surfaceVariant = surfaceVariantLight,
    outline = outlineLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    secondaryContainer = secondaryContainerDark,
    background = backgroundDark,
    surface = surfaceDark,
    surfaceVariant = surfaceVariantDark,
    outline = outlineDark,
)

@Immutable
data class CustomColors(
    val taskBgColors: List<Color> = listOf(),
    // THIS LINE WAS MISSING
    val taskIconColors: List<Color> = listOf()
)

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }

val lightCustomColors = CustomColors(
    taskBgColors = listOf(
        Color(0xFFE1F6DE), // green
        Color(0xFFEDF0FF), // blue
        Color(0xFFFFEFC9), // yellow
        Color(0xFFFFEDE6), // red
    ),
    // THIS INITIALIZATION WAS CAUSING THE ERROR
    taskIconColors = listOf(
        Color(0xFF34A853),
        Color(0xFF1967D2),
        Color(0xFFE37400),
        Color(0xFFD93025),
    )
)

val darkCustomColors = CustomColors(
    taskBgColors = listOf(
        Color(0xFF2E312D), // green
        Color(0xFF303033), // blue
        Color(0xFF33302A), // yellow
        Color(0xFF362F2D), // red
    ),
    taskIconColors = listOf(
        Color(0xFF6DD58C),
        Color(0xFFAAC7FF),
        Color(0xFFFFB955),
        Color(0xFFFFB4AB),
    )
)

val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColors.current

@Composable
fun GemmaDownloaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }
    val customColorsPalette = if (darkTheme) darkCustomColors else lightCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    CompositionLocalProvider(LocalCustomColors provides customColorsPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography, // Assumes a Typography.kt file exists
            content = content
        )
    }
}
