package com.rada.android.features.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.theme.RadaColors

object PayBrand {
    val VisaNavy = Color(0xFF141A5E)
    val VisaBlue = Color(0xFF2D3A9E)
    val VisaGold = Color(0xFFE6C877)
}

@Composable
fun VisaStyleCard(
    selected: Boolean,
    enabled: Boolean,
    periodLabel: String,
    unavailableNote: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .alpha(if (selected) 1f else 0.62f)
            .clip(shape)
            .background(Brush.linearGradient(listOf(PayBrand.VisaNavy, PayBrand.VisaBlue)))
            .border(if (selected) BorderStroke(2.dp, RadaColors.Accent) else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), shape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.07f), radius = size.height * 0.95f, center = Offset(size.width * 0.95f, size.height * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.05f), radius = size.height * 0.6f, center = Offset(size.width * 0.05f, size.height * 1.05f))
        }
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Google Play", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text("Subscription", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "VISA",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardChip()
                Spacer(Modifier.width(12.dp))
                Contactless()
            }
            Column {
                Text(
                    "••••  ••••  ••••  ••••",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Visa · Mastercard · M-PESA GlobalPay", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    Text(
                        "Auto-renews $periodLabel",
                        color = PayBrand.VisaNavy,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(PayBrand.VisaGold).padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        if (selected) SelectedTick(Modifier.align(Alignment.TopEnd).padding(top = 44.dp, end = 14.dp), Color.White)
        if (unavailableNote != null) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
                Text(unavailableNote, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(24.dp))
            }
        }
    }
}

@Composable
private fun CardChip() {
    Canvas(Modifier.width(38.dp).height(29.dp)) {
        val gold = Brush.linearGradient(listOf(Color(0xFFF1D98C), PayBrand.VisaGold, Color(0xFFC9A24B)))
        drawRoundRect(gold, cornerRadius = CornerRadius(6.dp.toPx()))
        val line = Color(0xFF8C6B22).copy(alpha = 0.6f)
        val stroke = 1.dp.toPx()
        drawLine(line, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), stroke)
        drawLine(line, Offset(size.width / 3, 0f), Offset(size.width / 3, size.height), stroke)
        drawLine(line, Offset(size.width * 2 / 3, 0f), Offset(size.width * 2 / 3, size.height), stroke)
        drawRoundRect(line, topLeft = Offset(size.width * 0.3f, size.height * 0.28f), size = Size(size.width * 0.4f, size.height * 0.44f), cornerRadius = CornerRadius(3.dp.toPx()), style = Stroke(stroke))
    }
}

@Composable
private fun Contactless() {
    Canvas(Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        for (i in 1..3) {
            val r = size.minDimension * (0.18f + i * 0.2f)
            drawArc(
                Color.White.copy(alpha = 0.9f),
                startAngle = -40f, sweepAngle = 80f, useCenter = false,
                topLeft = Offset(-r + size.width * 0.1f, size.height / 2 - r),
                size = Size(r * 2, r * 2),
                style = stroke
            )
        }
    }
}

@Composable
private fun SelectedTick(modifier: Modifier, tint: Color) {
    Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = tint, modifier = modifier.size(22.dp))
}
