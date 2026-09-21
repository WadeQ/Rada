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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
    val MpesaGreen = Color(0xFF3AA335)
    val MpesaDeep = Color(0xFF2A7F27)
    val MpesaRed = Color(0xFFE2231A)
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
fun MpesaStyleCard(
    selected: Boolean,
    amountLabel: String,
    periodLabel: String,
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
            .background(Brush.linearGradient(listOf(PayBrand.MpesaGreen, PayBrand.MpesaDeep)))
            .border(if (selected) BorderStroke(2.dp, RadaColors.Accent) else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), shape)
            .clickable(onClick = onClick)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.08f), radius = size.height * 0.9f, center = Offset(size.width * 1.0f, size.height * 0.95f))
        }
        Row(Modifier.fillMaxSize().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                MpesaWordmark()
                Column {
                    Text("Pay once", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("$periodLabel of Pro", color = Color.White.copy(alpha = 0.9f), fontSize = 11.5.sp)
                }
                Text(
                    "No auto-renew",
                    color = PayBrand.MpesaDeep,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.align(Alignment.CenterVertically)) {
                StkPromptPreview(amountLabel)
            }
        }
        if (selected) SelectedTick(Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp), Color.White)
    }
}

@Composable
private fun MpesaWordmark() {
    Column {
        Text(
            "M-PESA",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 0.5.sp
        )
        Canvas(Modifier.width(92.dp).height(9.dp)) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.2f)
                quadraticTo(size.width * 0.5f, size.height * 1.5f, size.width, size.height * 0.1f)
            }
            drawPath(path, PayBrand.MpesaRed, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun StkPromptPreview(amountLabel: String) {
    Column(
        Modifier
            .width(118.dp)
            .rotate(-3f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("M-PESA", color = PayBrand.MpesaGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text("Pay $amountLabel to Rada?", color = Color(0xFF1E1E1E), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, lineHeight = 12.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(4) { Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF1E1E1E))) }
            Icon(Icons.Outlined.Lock, null, tint = PayBrand.MpesaGreen, modifier = Modifier.size(11.dp))
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Cancel", color = Color(0xFF6B6B6B), fontSize = 8.5.sp)
            Text("OK", color = PayBrand.MpesaGreen, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
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

@Composable
fun MethodStrip(
    brand: Brush,
    mark: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(brand)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(mark, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        }
    }
}
