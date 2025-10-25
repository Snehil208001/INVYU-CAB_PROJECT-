package com.example.invyucab_project.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CabPrimaryGreen,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = CabPrimaryGreen,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun INVYUCAB_PROJECTTheme(
    // darkTheme: Boolean = isSystemInDarkTheme(), // REMOVED: No longer check system theme
    dynamicColor: Boolean = true, // Keep dynamic color option if desired
    content: @Composable () -> Unit
) {
    // ✅ Always use the LightColorScheme logic
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // Always request dynamic *light* scheme
            dynamicLightColorScheme(context)
        }
        // darkTheme -> DarkColorScheme // REMOVED: Dark scheme branch
        else -> LightColorScheme // Default to LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}