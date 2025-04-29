package com.example.blabapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BlabPurple,
    secondary = BlabLightPurple,
    tertiary = BlabDarkPurple,
    background = BlabDark,
    surface = BlabLight,
    outline = BlabDarkGreen,
    onErrorContainer = BlabRed,
    outlineVariant = BlabDarkRed
)

private val LightColorScheme = lightColorScheme(
    primary = BlabBlue,
    secondary = BlabDarkBlue,
    tertiary = BlabLightBlue,
    background = BlabLight,
    surface = BlabDark,
    outline = BlabLightGreen,
    onErrorContainer = BlabRed,
    outlineVariant = BlabDarkRed
)



@Composable
fun BlabAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    val activity = view.context as Activity

    SideEffect {
        val window = activity.window
        // Change the system nav bar color to your theme's primary color
        window.navigationBarColor = colorScheme.primary.toArgb()

        // Make nav bar icons dark or light depending on theme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}