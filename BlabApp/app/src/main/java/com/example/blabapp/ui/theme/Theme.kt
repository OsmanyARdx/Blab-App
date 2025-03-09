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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}