package com.rada.android.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Both a light and dark ramp for every role. The previous version of this file only had dark
 * values (RadaColors.Ink etc.) and screens referenced them directly — that's what made "dark
 * mode support" impossible without touching every screen. Screens should now read colors from
 * MaterialTheme.colorScheme (see Theme.kt), not from this object directly, in any newly
 * converted screen. Un-migrated screens (Dashboard/Drive/Report/Help/Paywall as of this pass)
 * still reference RadaColors below until they're converted in a follow-up pass.
 */
object RadaPalette {
    // Brand — same across both modes, this is the app's identity, not a surface color.
    val Blue = Color(0xFF6FA1F0)
    val BlueDark = Color(0xFF2F5FC4) // "on light surfaces" variant where the pale Blue lacks contrast
    val BlueInk = Color(0xFF0B2A52)
    val Amber = Color(0xFFF2A93B)
    val AmberInk = Color(0xFF3A2600)
    val Orange = Color(0xFFD97706)
    val Red = Color(0xFFE5484D)
    // App accent. Bright lime reads well on dark surfaces; on light surfaces it has almost no
    // contrast as text/icons, so light mode uses a deeper lime (LimeDeep) with white on top.
    val Lime = Color(0xFFCCFF00)
    val LimeInk = Color(0xFF10130A)
    val LimeDeep = Color(0xFF557300)
    val RedInk = Color(0xFF3A0709)

    // Dark mode surfaces (the app's original/primary look — night driving, glare reduction)
    val DarkBackground = Color(0xFF12161B)
    val DarkSurface = Color(0xFF1C222A)
    val DarkSurfaceVariant = Color(0xFF262E38)
    val DarkOnBackground = Color(0xFFF3F1EC)
    val DarkOnSurfaceVariant = Color(0xFFA9B0BC)
    val DarkOnSurfaceMuted = Color(0xFF6E7684)

    // Light mode surfaces
    val LightBackground = Color(0xFFF7F6F3)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFEDEBE6)
    val LightOnBackground = Color(0xFF1B1E22)
    val LightOnSurfaceVariant = Color(0xFF565C64)
    val LightOnSurfaceMuted = Color(0xFF8A9099)
}

/**
 * Theme-aware accessors for screens that still reference RadaColors. Each getter reads the active
 * MaterialTheme scheme, so these follow light/dark automatically. Must be read from composable
 * scope. Prefer MaterialTheme.colorScheme directly in new code.
 */
object RadaColors {
    val Ink @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
    val Ink2 @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
    val Ink3 @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant
    /** Accent for text/icons on surfaces: lime in dark, a deeper lime in light so it stays legible. */
    val Blue @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
    /** Bright lime for filled shapes (buttons, avatars, progress) — always paired with AccentInk. */
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
