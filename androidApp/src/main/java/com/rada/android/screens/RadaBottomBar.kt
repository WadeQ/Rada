package com.rada.android.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Size
import com.rada.android.theme.RadaColors
import com.rada.android.theme.RadaPalette

enum class RadaTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Drive("Drive", Icons.Filled.Navigation),
    Report("Report", Icons.Filled.Report),
    Help("Help", Icons.Filled.Shield),
    Settings("Settings", Icons.Filled.Settings)
}

private val BarHeight = 64.dp

/** Space to leave beneath bottom-most content so the active circle, which rises above the bar, never covers it. */
val BOTTOM_BAR_CLEARANCE = 34.dp
private val Overshoot = 30.dp        // how far the active circle rises above the bar (overlaps content; takes no layout space)
private val NotchDepth = 30.dp
private val CircleSize = 52.dp
private val SidePadding = 28.dp      // keeps the notch clear of the bar's rounded ends
private val CornerRadius = 22.dp
private val NotchHalfWidth = 38.dp

/**
 * Dark floating bar with a concave notch under the active tab; the active icon sits in a
 * lime circle that slides between tabs. One instance lives under the NavHost so the circle
 * can animate across destinations instead of being redrawn per screen.
 */
@Composable
fun RadaBottomBar(
    selected: RadaTab,
    onSelect: (RadaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val barColor = if (dark) RadaPalette.DarkSurfaceVariant else Color(0xFF14181D)
    val tabs = RadaTab.entries

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp)
            .height(BarHeight)
    ) {
        val slotWidth = (maxWidth - SidePadding * 2) / tabs.size
        val targetCenter = SidePadding + slotWidth * (selected.ordinal + 0.5f)
        val center by animateDpAsState(
            targetValue = targetCenter,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
            label = "navCenter"
        )
        val density = LocalDensity.current

        // Notched bar
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BarHeight)
                .background(barColor, NotchedBarShape(with(density) { center.toPx() }, density))
        )

        // Tab slots: icon for inactive tabs, label for the active one
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BarHeight)
                .padding(horizontal = SidePadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { if (tab != selected) onSelect(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    if (tab == selected) {
                        Column(
                            Modifier.fillMaxSize().padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(tab.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Icon(tab.icon, tab.label, tint = Color.White.copy(alpha = 0.72f), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        // Active circle
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = center - CircleSize / 2, y = -(Overshoot - 3.dp))
                .size(CircleSize)
                .background(RadaColors.Accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = selected, label = "navIcon") { tab ->
                Icon(tab.icon, tab.label, tint = RadaColors.AccentInk, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/** Rounded rectangle with a smooth U-shaped dip in the top edge centred on [centerX]. */
private class NotchedBarShape(
    private val centerX: Float,
    density: Density
) : Shape {
    private val corner = with(density) { CornerRadius.toPx() }
    private val halfWidth = with(density) { NotchHalfWidth.toPx() }
    private val depth = with(density) { NotchDepth.toPx() }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val start = (centerX - halfWidth).coerceAtLeast(corner)
        val end = (centerX + halfWidth).coerceAtMost(w - corner)
        val path = Path().apply {
            moveTo(0f, corner)
            quadraticBezierTo(0f, 0f, corner, 0f)
            lineTo(start, 0f)
            cubicTo(centerX - halfWidth * 0.55f, 0f, centerX - halfWidth * 0.62f, depth, centerX, depth)
            cubicTo(centerX + halfWidth * 0.62f, depth, centerX + halfWidth * 0.55f, 0f, end, 0f)
            lineTo(w - corner, 0f)
            quadraticBezierTo(w, 0f, w, corner)
            lineTo(w, h - corner)
            quadraticBezierTo(w, h, w - corner, h)
            lineTo(corner, h)
            quadraticBezierTo(0f, h, 0f, h - corner)
            close()
        }
        return Outline.Generic(path)
    }
}
