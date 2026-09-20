package com.rada.android.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun OnboardingCameraAlertIllustration(modifier: Modifier = Modifier) {
    val carEnterX = remember { Animatable(-190f) }
    val popupAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        carEnterX.animateTo(8f, tween(800, easing = LinearEasing))
        carEnterX.animateTo(-4f, tween(150))
        carEnterX.animateTo(0f, tween(150))
        popupAlpha.animateTo(1f, tween(500))
    }

    val infinite = rememberInfiniteTransition(label = "ob-illustration-loop")
    val bob by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "bob"
    )
    val wheelAngle by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(500, easing = LinearEasing)), label = "wheel"
    )
    val arcPhase by infinite.animateFloat(
        0f, 40f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "arc"
    )
    val lensGlow by infinite.animateFloat(
        1f, 0.35f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse), label = "glow"
    )

    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(380f / 260f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        val scale = size.width / 380f
        fun x(v: Float) = v * scale
        fun y(v: Float) = v * scale

        drawRect(Color(0xFF151A21), size = androidx.compose.ui.geometry.Size(size.width, y(170f)))
        listOf(45f to 35f, 90f to 20f, 330f to 30f, 250f to 45f).forEach { (sx, sy) ->
            drawCircle(Color(0xFF3A4250), radius = 1.6f * scale, center = Offset(x(sx), y(sy)))
        }

        drawRect(
            Color(0xFF262B33),
            topLeft = Offset(0f, y(170f)),
            size = androidx.compose.ui.geometry.Size(size.width, y(60f))
        )
        listOf(20f, 70f, 270f, 320f).forEach { lx ->
            drawRoundRect(
                Color(0xFF4B5563),
                topLeft = Offset(x(lx), y(197f)),
                size = androidx.compose.ui.geometry.Size(x(26f), y(4f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(y(2f))
            )
        }

        drawRect(Color(0xFF4B5563), topLeft = Offset(x(298f), y(108f)), size = androidx.compose.ui.geometry.Size(x(6f), y(60f)))
        drawRoundRect(
            Color(0xFF333B46), topLeft = Offset(x(280f), y(92f)),
            size = androidx.compose.ui.geometry.Size(x(44f), y(30f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(x(6f))
        )
        drawCircle(
            Color(0xFFE5484D), radius = x(8f), center = Offset(x(302f), y(107f)),
            style = Stroke(width = x(2f)), alpha = lensGlow
        )
        drawCircle(Color(0xFFE5484D), radius = x(3f), center = Offset(x(302f), y(107f)))

        val dash = PathEffect.dashPathEffect(floatArrayOf(x(5f), x(5f)), phase = x(arcPhase))
        listOf(0.9f to 0f, 0.5f to 0.5f).forEachIndexed { i, (baseAlpha, _) ->
            val path = androidx.compose.ui.graphics.Path().apply {
                if (i == 0) {
                    moveTo(x(158f), y(168f))
                    quadraticBezierTo(x(220f), y(150f), x(278f), y(118f))
                } else {
                    moveTo(x(158f), y(178f))
                    quadraticBezierTo(x(215f), y(168f), x(272f), y(132f))
                }
            }
            drawPath(path, Color(0xFF6FA1F0), style = Stroke(width = x(2f), cap = StrokeCap.Round, pathEffect = dash), alpha = baseAlpha)
        }

        val carX = x(carEnterX.value)
        val carY = y((sin(bob * PI.toFloat()) * 2f))
        val carOffset = Offset(carX, carY)

        drawRoundRect(
            Color(0xFFF2A93B), topLeft = Offset(x(60f), y(188f)) + carOffset,
            size = androidx.compose.ui.geometry.Size(x(92f), y(26f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(x(10f))
        )
        drawRoundRect(
            Color(0xFFF2A93B), topLeft = Offset(x(86f), y(170f)) + carOffset,
            size = androidx.compose.ui.geometry.Size(x(44f), y(22f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(x(8f))
        )
        drawRoundRect(
            Color(0xFF151A21).copy(alpha = 0.35f), topLeft = Offset(x(90f), y(176f)) + carOffset,
            size = androidx.compose.ui.geometry.Size(x(34f), y(12f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(x(3f))
        )
        listOf(80f, 135f).forEach { wx ->
            val center = Offset(x(wx), y(220f)) + carOffset
            drawCircle(Color(0xFF151A21), radius = x(10f), center = center)
            val rad = Math.toRadians(wheelAngle.toDouble())
            val spokeEnd = Offset(
                center.x + (x(8f) * kotlin.math.cos(rad)).toFloat(),
                center.y + (x(8f) * kotlin.math.sin(rad)).toFloat()
            )
            drawLine(Color(0xFF4B5563), start = center, end = spokeEnd, strokeWidth = x(2f))
        }
        drawCircle(Color(0xFFFFE9C2), radius = x(4f), center = Offset(x(151f), y(199f)) + carOffset)

        val popupPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(x(98f), y(142f)); lineTo(x(110f), y(142f)); lineTo(x(100f), y(154f)); close()
        }
        drawPath(popupPath, Color(0xFFF3F1EC), alpha = popupAlpha.value)
        drawRoundRect(
            Color(0xFFF3F1EC), topLeft = Offset(x(52f), y(88f)),
            size = androidx.compose.ui.geometry.Size(x(150f), y(56f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(x(12f)),
            alpha = popupAlpha.value
        )
        drawRoundRect(
            Color.Transparent, topLeft = Offset(x(66f), y(104f)),
            size = androidx.compose.ui.geometry.Size(x(20f), y(16f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(x(3f)),
            style = Stroke(width = x(2f)), alpha = popupAlpha.value
        )
        drawCircle(
            Color(0xFFE5484D), radius = x(4f), center = Offset(x(76f), y(112f)),
            style = Stroke(width = x(2f)), alpha = popupAlpha.value
        )

        if (popupAlpha.value > 0.05f) {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = 13f * scale
                    color = android.graphics.Color.parseColor("#12161B")
                    alpha = (popupAlpha.value * 255).toInt()
                }
                canvas.nativeCanvas.drawText("Camera ahead", x(96f), y(110f), paint)
                paint.textSize = 11f * scale
                paint.color = android.graphics.Color.parseColor("#6E7684")
                canvas.nativeCanvas.drawText("180 m · limit 50 km/h", x(96f), y(127f), paint)
            }
        }
    }
}
