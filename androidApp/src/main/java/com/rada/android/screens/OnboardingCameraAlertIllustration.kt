package com.rada.android.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OnboardingCameraAlertIllustration(modifier: Modifier = Modifier) {
    val loop = rememberInfiniteTransition(label = "ob-scene")
    val road by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(700, easing = LinearEasing)), label = "road")
    val hills by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "hills")
    val wheel by loop.animateFloat(0f, 360f, infiniteRepeatable(tween(420, easing = LinearEasing)), label = "wheel")
    val bob by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "bob")
    val pulse by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "pulse")
    val beam by loop.animateFloat(0f, 40f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "beam")
    val approach by loop.animateFloat(0f, 1f, infiniteRepeatable(tween(5200, easing = LinearEasing)), label = "approach")

    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(380f / 260f)
            .clip(RoundedCornerShape(20.dp))
    ) {
        val s = size.width / 380f
        fun x(v: Float) = v * s
        fun y(v: Float) = v * s
        val horizon = y(158f)

        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFF2A0F2E), 0.5f to Color(0xFF7A1F3D), 1f to Color(0xFFF2A93B),
                startY = 0f, endY = horizon
            ),
            size = Size(size.width, horizon)
        )
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFFFE2A0), Color(0x66FFB35C), Color.Transparent),
                center = Offset(x(250f), horizon), radius = x(120f)
            ),
            radius = x(120f), center = Offset(x(250f), horizon)
        )
        drawCircle(Color(0xFFFFEBB8), radius = x(26f), center = Offset(x(250f), horizon - y(8f)))
        listOf(40f to 30f, 95f to 18f, 160f to 42f, 320f to 26f, 350f to 60f).forEach { (sx, sy) ->
            drawCircle(Color.White.copy(alpha = 0.55f), radius = 1.5f * s, center = Offset(x(sx), y(sy)))
        }

        fun ridge(color: Color, base: Float, amp: Float, scroll: Float, waves: Float) {
            val p = Path().apply {
                moveTo(0f, horizon)
                var px = 0f
                while (px <= size.width + 4f) {
                    val t = px / size.width * waves * 2f * PI.toFloat() + scroll * 2f * PI.toFloat()
                    lineTo(px, horizon - y(base) - y(amp) * (0.5f + 0.5f * sin(t)))
                    px += 4f
                }
                lineTo(size.width, horizon); close()
            }
            drawPath(p, color)
        }
        ridge(Color(0xFF4A1A45), 14f, 26f, hills, 1.6f)
        ridge(Color(0xFF2E0F30), 4f, 16f, hills * 2f, 2.4f)

        drawRect(Color(0xFF1F2128), topLeft = Offset(0f, horizon), size = Size(size.width, size.height - horizon))
        drawRect(Color(0xFFCCFF00), topLeft = Offset(0f, horizon), size = Size(size.width, y(3f)))
        val dashW = x(30f); val gap = x(30f)
        var dx = -(road * (dashW + gap))
        while (dx < size.width) {
            drawRoundRect(
                Color.White.copy(alpha = 0.85f), topLeft = Offset(dx, y(214f)),
                size = Size(dashW, y(5f)), cornerRadius = CornerRadius(y(2.5f))
            )
            dx += dashW + gap
        }

        val poleBase = Offset(x(318f), y(196f))
        drawRoundRect(Color(0xFF9CA3AF), topLeft = Offset(poleBase.x - x(3f), y(112f)), size = Size(x(6f), y(84f)), cornerRadius = CornerRadius(x(2f)))
        val cone = Path().apply {
            moveTo(x(300f), y(110f)); lineTo(x(190f), y(150f)); lineTo(x(190f), y(196f)); lineTo(x(300f), y(122f)); close()
        }
        drawPath(cone, Brush.horizontalGradient(listOf(Color(0x00E5484D), Color(0x33E5484D)), startX = x(190f), endX = x(300f)))
        drawRoundRect(Color(0xFFE5E7EB), topLeft = Offset(x(296f), y(94f)), size = Size(x(48f), y(32f)), cornerRadius = CornerRadius(x(7f)))
        drawRoundRect(Color(0xFF111827), topLeft = Offset(x(302f), y(99f)), size = Size(x(36f), y(22f)), cornerRadius = CornerRadius(x(5f)))
        drawCircle(Color(0xFFE5484D).copy(alpha = 0.55f * (1f - pulse)), radius = x(8f + 12f * pulse), center = Offset(x(320f), y(110f)), style = Stroke(width = x(2f)))
        drawCircle(Color(0xFFFF5A5F), radius = x(6f), center = Offset(x(320f), y(110f)))

        val bounce = y(sin(bob * PI.toFloat()) * 1.6f)
        val carX = x(58f)
        val carY = y(188f) + bounce

        val headlight = Path().apply {
            moveTo(carX + x(96f), carY + y(10f)); lineTo(carX + x(190f), carY - y(4f)); lineTo(carX + x(190f), carY + y(26f)); close()
        }
        drawPath(headlight, Brush.horizontalGradient(listOf(Color(0x66FFF3C4), Color.Transparent), startX = carX + x(96f), endX = carX + x(190f)))

        listOf(0f, 9f, 18f).forEach { i ->
            drawLine(
                Color.White.copy(alpha = 0.35f - i / 90f),
                Offset(carX - x(8f) - x(i * 1.5f), carY + y(6f + i * 0.8f)),
                Offset(carX - x(30f) - x(i * 2.5f), carY + y(6f + i * 0.8f)),
                strokeWidth = x(2f), cap = StrokeCap.Round
            )
        }

        drawRoundRect(Color(0xFFCCFF00), topLeft = Offset(carX, carY), size = Size(x(98f), y(28f)), cornerRadius = CornerRadius(x(11f)))
        drawRoundRect(Color(0xFFCCFF00), topLeft = Offset(carX + x(24f), carY - y(19f)), size = Size(x(50f), y(26f)), cornerRadius = CornerRadius(x(10f)))
        drawRoundRect(Color(0xFF1A0A10), topLeft = Offset(carX + x(30f), carY - y(14f)), size = Size(x(38f), y(15f)), cornerRadius = CornerRadius(x(4f)))
        drawRect(Color(0xFF7A1F3D), topLeft = Offset(carX + x(4f), carY + y(13f)), size = Size(x(90f), y(4f)))
        drawCircle(Color(0xFFFFF3C4), radius = x(4.5f), center = Offset(carX + x(95f), carY + y(9f)))
        drawCircle(Color(0xFFE5484D), radius = x(3f), center = Offset(carX + x(2f), carY + y(8f)))

        listOf(22f, 76f).forEach { wx ->
            val c = Offset(carX + x(wx), carY + y(29f))
            drawCircle(Color(0xFF0B0C10), radius = x(12f), center = c)
            drawCircle(Color(0xFFD1D5DB), radius = x(6.5f), center = c)
            val rad = Math.toRadians(wheel.toDouble())
            for (k in 0..2) {
                val a = rad + k * 2.0 * PI / 3.0
                drawLine(Color(0xFF6B7280), c, Offset(c.x + x(6f) * cos(a).toFloat(), c.y + x(6f) * sin(a).toFloat()), strokeWidth = x(2f))
            }
        }

        val dash = PathEffect.dashPathEffect(floatArrayOf(x(6f), x(6f)), phase = x(beam))
        drawPath(
            Path().apply { moveTo(carX + x(60f), carY - y(20f)); quadraticBezierTo(x(210f), y(70f), x(296f), y(104f)) },
            Color(0xFFCCFF00), style = Stroke(width = x(2.4f), cap = StrokeCap.Round, pathEffect = dash), alpha = 0.9f
        )

        val meters = (180f - approach * 130f).toInt().coerceAtLeast(40)
        val scalePop = 1f + 0.03f * sin(pulse * 2f * PI.toFloat())
        val bx = x(24f); val by = y(24f); val bw = x(172f); val bh = y(58f)
        drawRoundRect(Color(0x33000000), topLeft = Offset(bx + x(2f), by + y(4f)), size = Size(bw * scalePop, bh * scalePop), cornerRadius = CornerRadius(x(14f)))
        drawRoundRect(Color(0xFFF7F4EE), topLeft = Offset(bx, by), size = Size(bw * scalePop, bh * scalePop), cornerRadius = CornerRadius(x(14f)))
        drawPath(Path().apply { moveTo(x(104f), y(82f)); lineTo(x(118f), y(82f)); lineTo(x(112f), y(94f)); close() }, Color(0xFFF7F4EE))
        drawRoundRect(Color(0xFFE5484D), topLeft = Offset(x(38f), y(42f)), size = Size(x(24f), y(18f)), cornerRadius = CornerRadius(x(4f)))
        drawCircle(Color.White, radius = x(5f), center = Offset(x(50f), y(51f)))
        drawCircle(Color(0xFF1F2128), radius = x(2.4f), center = Offset(x(50f), y(51f)))

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply { isAntiAlias = true; isFakeBoldText = true }
            paint.textSize = 14f * s; paint.color = android.graphics.Color.parseColor("#1F1216")
            canvas.nativeCanvas.drawText("Camera ahead", x(72f), y(48f), paint)
            paint.isFakeBoldText = false; paint.textSize = 11.5f * s; paint.color = android.graphics.Color.parseColor("#7A1F3D")
            canvas.nativeCanvas.drawText("$meters m · limit 50 km/h", x(72f), y(67f), paint)
        }
    }
}
