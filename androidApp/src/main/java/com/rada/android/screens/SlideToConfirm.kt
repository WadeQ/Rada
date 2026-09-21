package com.rada.android.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rada.android.theme.RadaColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToConfirm(
    label: String,
    doneLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onConfirm: () -> Unit
) {
    val handleSizeDp = 44.dp
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val handleOffset = remember { Animatable(0f) }
    var confirmed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val handleSizePx = with(density) { handleSizeDp.toPx() }

    LaunchedEffect(enabled, confirmed) {
        if (!confirmed) return@LaunchedEffect
        if (enabled) delay(RESET_AFTER_MS)
        if (enabled) {
            confirmed = false
            handleOffset.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(RadaColors.Ink2)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
    ) {
        val maxOffset = (trackWidthPx - handleSizePx - 8f).coerceAtLeast(0f)
        val disabledAlpha = if (enabled) 1f else 0.4f

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (confirmed) RadaColors.Accent else RadaColors.Accent.copy(
                        alpha = (if (maxOffset > 0f) (handleOffset.value / maxOffset).coerceIn(0f, 1f) else 0f) * disabledAlpha
                    )
                )
        )

        Text(
            text = if (confirmed) doneLabel else label,
            color = (if (confirmed) RadaColors.AccentInk else RadaColors.TextSecondary).copy(alpha = disabledAlpha),
            modifier = Modifier.align(Alignment.Center)
        )

        Box(
            Modifier
                .padding(4.dp)
                .offset { IntOffset(handleOffset.value.roundToInt(), 0) }
                .size(handleSizeDp)
                .clip(CircleShape)
                .background(RadaColors.Accent.copy(alpha = disabledAlpha))
                .pointerInput(maxOffset, enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (!confirmed) {
                                scope.launch {
                                    val next = (handleOffset.value + dragAmount.x).coerceIn(0f, maxOffset)
                                    handleOffset.snapTo(next)
                                }
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (handleOffset.value >= maxOffset * 0.8f) {
                                    handleOffset.animateTo(maxOffset)
                                    confirmed = true
                                    onConfirm()
                                } else {
                                    handleOffset.animateTo(0f)
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Slide to confirm",
                tint = RadaColors.AccentInk.copy(alpha = disabledAlpha)
            )
        }
    }
}

private const val RESET_AFTER_MS = 800L
