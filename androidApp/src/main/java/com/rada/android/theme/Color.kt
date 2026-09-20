package com.rada.android.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object RadaPalette {
    val Blue = Color(0xFF6FA1F0)
    val BlueDark = Color(0xFF2F5FC4)
    val BlueInk = Color(0xFF0B2A52)
    val Amber = Color(0xFFF2A93B)
    val AmberInk = Color(0xFF3A2600)
    val Orange = Color(0xFFD97706)
    val Red = Color(0xFFE5484D)
    val Lime = Color(0xFFCCFF00)
    val LimeInk = Color(0xFF10130A)
    val LimeDeep = Color(0xFF557300)

    val Burgundy = Color(0xFF7A1F3D)
    val BurgundyLight = Color(0xFF9A2C50)
    val RedInk = Color(0xFF3A0709)

    val DarkBackground = Color(0xFF12161B)
    val DarkSurface = Color(0xFF1C222A)
    val DarkSurfaceVariant = Color(0xFF262E38)
    val DarkOnBackground = Color(0xFFF3F1EC)
    val DarkOnSurfaceVariant = Color(0xFFA9B0BC)
    val DarkOnSurfaceMuted = Color(0xFF6E7684)

    val LightBackground = Color(0xFFF7F6F3)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFEDEBE6)
    val LightOnBackground = Color(0xFF1B1E22)
    val LightOnSurfaceVariant = Color(0xFF565C64)
    val LightOnSurfaceMuted = Color(0xFF8A9099)
}

object RadaColors {
    val Ink @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
    val Ink2 @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
    val Ink3 @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant
    val Blue @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
    val Burgundy @Composable @ReadOnlyComposable get() =
        if (MaterialTheme.colorScheme.background.luminance() > 0.5f) RadaPalette.BurgundyLight else RadaPalette.Burgundy
    val Accent = RadaPalette.Lime
    val AccentInk = RadaPalette.LimeInk
    val Amber = RadaPalette.Amber
    val AmberInk = RadaPalette.AmberInk
    val Orange = RadaPalette.Orange
    val Red = RadaPalette.Red
    val RedInk = RadaPalette.RedInk
    val TextPrimary @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onBackground
    val TextSecondary @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val TextMuted @Composable @ReadOnlyComposable get() =
        if (MaterialTheme.colorScheme.background.luminance() > 0.5f) RadaPalette.LightOnSurfaceMuted
        else RadaPalette.DarkOnSurfaceMuted
}
