package com.rada.android.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.theme.RadaColors
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class ObPage(val title: String, val body: String)

private val pages = listOf(
    ObPage(
        "Cameras, before you reach them",
        "Get warned about speed cameras, checkpoints and alcohol checks, from known spots and from drivers like you. Locations are approximate, not officially confirmed by NTSA."
    ),
    ObPage(
        "What's shared, and what isn't",
        "Reporting a camera uploads only the spot you choose, so other drivers can see it. Your live position while driving is never stored or shared."
    ),
    ObPage(
        "Alerts, even when Rada is closed",
        "Rada keeps watching the road while you use other apps. That needs location access, and you can turn it off anytime in Settings."
    )
)

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex

    Column(modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(
            Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!last) {
                Text(
                    "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onDone).padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
            val offset = (pager.currentPage - page) + pager.currentPageOffsetFraction
            Column(
                Modifier.fillMaxSize().graphicsLayer { alpha = 1f - (abs(offset) * 0.7f).coerceIn(0f, 1f) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier.height(280.dp).fillMaxWidth().graphicsLayer { translationX = offset * 140f },
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> OnboardingCameraAlertIllustration(Modifier.fillMaxWidth(0.92f))
                        1 -> PrivacyHero()
                        else -> LocationHero()
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    pages[page].title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    pages[page].body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }

        PageDots(pager) { target -> scope.launch { pager.animateScrollToPage(target) } }
        Spacer(Modifier.height(22.dp))
        key(pager.currentPage) {
            SlideToConfirm(
                label = if (last) "Slide to get started" else "Slide to continue",
                doneLabel = if (last) "Let's go" else "Continuing…",
                onConfirm = {
                    if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                }
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PageDots(pager: PagerState, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pages.size) { i ->
            val active = pager.currentPage == i
            val width by animateDpAsState(if (active) 28.dp else 8.dp, label = "dotWidth")
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(if (active) RadaColors.Accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f))
                    .clickable { onSelect(i) }
            )
        }
    }
}

@Composable
private fun PrivacyHero() {
    val transition = rememberInfiniteTransition(label = "privacy")
    val bob by transition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val burgundy = RadaColors.Burgundy
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(burgundy.copy(alpha = 0.55f), Color.Transparent)), radius = size.minDimension / 2f)
        }
        Box(
            Modifier.size(150.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Shield, null, tint = RadaColors.Accent, modifier = Modifier.size(78.dp))
            Icon(Icons.Filled.Lock, null, tint = RadaColors.AccentInk, modifier = Modifier.size(28.dp).offset(y = 3.dp))
        }
        FloatBadge(Icons.Filled.PhoneAndroid, Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = (28 + bob).dp))
        FloatBadge(Icons.Filled.Lock, Modifier.align(Alignment.BottomStart).offset(x = 14.dp, y = (-28 - bob).dp))
    }
}

@Composable
private fun FloatBadge(icon: ImageVector, modifier: Modifier) {
    Box(
        modifier.size(48.dp).clip(CircleShape).background(RadaColors.Burgundy),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun LocationHero() {
    val transition = rememberInfiniteTransition(label = "location")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )
    val accent = RadaColors.Accent
    val burgundy = RadaColors.Burgundy
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Brush.radialGradient(listOf(burgundy.copy(alpha = 0.45f), Color.Transparent), center = center, radius = size.minDimension / 2f))
            for (i in 0..2) {
                val p = ((phase + i / 3f) % 1f)
                drawCircle(
                    color = accent.copy(alpha = (1f - p) * 0.55f),
                    radius = 44.dp.toPx() + (size.minDimension / 2f - 44.dp.toPx()) * p,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
            }
        }
        Box(
            Modifier.size(96.dp).clip(CircleShape).background(RadaColors.Accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocationOn, null, tint = RadaColors.AccentInk, modifier = Modifier.size(52.dp))
        }
    }
}
