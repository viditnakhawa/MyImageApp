package com.viditnakhawa.myimageapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Color Schemes using your custom colors ---
private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    secondaryContainer = secondaryContainerLight,
    background = backgroundLight,
    surface = surfaceLight,
    surfaceVariant = surfaceVariantLight,
    outline = outlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    secondaryContainer = secondaryContainerDark,
    background = backgroundDark,
    surface = surfaceDark,
    surfaceVariant = surfaceVariantDark,
    outline = outlineDark,
)

// --- Custom Colors for specific UI elements ---
@Immutable
data class CustomColors(
    val taskBgColors: List<Color> = listOf(),
    val taskIconColors: List<Color> = listOf()
)

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }

private val lightCustomColors = CustomColors(
    taskBgColors = listOf(
        Color(0xFFE1F6DE), // green
        Color(0xFFEDF0FF), // blue
        Color(0xFFFFEFC9), // yellow
        Color(0xFFFFEDE6), // red
    ),
    taskIconColors = listOf(
        Color(0xFF34A853),
        Color(0xFF1967D2),
        Color(0xFFE37400),
        Color(0xFFD93025),
    )
)

private val darkCustomColors = CustomColors(
    taskBgColors = listOf(
        Color(0xFF2E312D),
        Color(0xFF303033),
        Color(0xFF33302A),
        Color(0xFF362F2D),
    ),
    taskIconColors = listOf(
        Color(0xFF6DD58C),
        Color(0xFFAAC7FF),
        Color(0xFFFFB955),
        Color(0xFFFFB4AB),
    )
)

// Extension property to easily access custom colors
val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColors.current

// --- The Main App Theme ---
@Composable
fun MyImageAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // allow opt-in for Material You
    content: @Composable () -> Unit
) {
    val context = LocalView.current.context
    val colorScheme = when {
        dynamicColor && true -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColorsPalette = if (darkTheme) darkCustomColors else lightCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalCustomColors provides customColorsPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
