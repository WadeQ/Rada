package com.rada.android.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rada.android.theme.RadaColors

/**
 * Self-contained Compose shimmer — no external library dependency, matching this project's
 * existing preference for hand-built Compose-primitive components over pulling in a library
 * for a fairly small visual effect (same reasoning as SlideToConfirm/NavItem). Reusable
 * anywhere a "this is loading" placeholder is needed, not tied to Drive specifically.
 */
@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val shimmerColors = listOf(
        RadaColors.Ink3.copy(alpha = 0.6f),
        RadaColors.Ink3.copy(alpha = 0.25f),
        RadaColors.Ink3.copy(alpha = 0.6f)
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

/**
 * A single rounded-rect shimmer placeholder — size it via the modifier (width/height), same
 * as any other Box. Use this in place of a value's Text while its real value isn't known yet,
 * keeping static labels/captions around it as real text rather than shimmering everything.
 */
@Composable
fun ShimmerBlock(modifier: Modifier = Modifier, cornerRadius: Dp = 6.dp) {
    val brush = rememberShimmerBrush()
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(brush))
}