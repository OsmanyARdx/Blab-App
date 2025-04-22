package com.example.blabapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BlabPurple,
    secondary = Blablight,
    tertiary = BlabDarkBlue,
    background = BlabPurple,
    onTertiary = Color.White,
    surface = BlabDark


)

private val LightColorScheme = lightColorScheme(
    primary = BlabPurple,
    secondary = BlabPurple,
    tertiary = BlabBlue,
    background = BlabPurple,
    onTertiary = Color.Black,
    surface = Blablight

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