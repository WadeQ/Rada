package com.rada.android.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalContext
import com.rada.android.data.ThemeMode
import com.rada.android.data.ThemePreferences

private val RadaDarkScheme = darkColorScheme(
    background = RadaPalette.DarkBackground,
    surface = RadaPalette.DarkSurface,
    surfaceVariant = RadaPalette.DarkSurfaceVariant,
    primary = RadaPalette.Lime,
    onPrimary = RadaPalette.LimeInk,
    secondary = RadaPalette.Amber,
    onSecondary = RadaPalette.AmberInk,
    tertiary = RadaPalette.Orange,
    error = RadaPalette.Red,
    onError = RadaPalette.RedInk,
    onBackground = RadaPalette.DarkOnBackground,
    onSurface = RadaPalette.DarkOnBackground,
    onSurfaceVariant = RadaPalette.DarkOnSurfaceVariant
)

private val RadaLightScheme = lightColorScheme(
    background = RadaPalette.LightBackground,
    surface = RadaPalette.LightSurface,
    surfaceVariant = RadaPalette.LightSurfaceVariant,
    primary = RadaPalette.LimeDeep,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = RadaPalette.Amber,
    onSecondary = RadaPalette.AmberInk,
    tertiary = RadaPalette.Orange,
    error = RadaPalette.Red,
    onError = androidx.compose.ui.graphics.Color.White,
    onBackground = RadaPalette.LightOnBackground,
    onSurface = RadaPalette.LightOnBackground,
    onSurfaceVariant = RadaPalette.LightOnSurfaceVariant
)

@Composable
fun RadaTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val scheme = if (darkTheme) RadaDarkScheme else RadaLightScheme
    MaterialTheme(colorScheme = scheme, content = content)
}
