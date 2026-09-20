package com.rada.android.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.rada.android.R
import com.rada.android.theme.RadaPalette
import kotlinx.coroutines.delay

// Fixed ink regardless of the app's theme: the lime Twiga needs a dark ground, and the Android 12+
// system splash that precedes this screen is the same ink (see values-v31/styles.xml).
private val SplashInk = RadaPalette.DarkBackground

/**
 * The Twiga sits exactly where the system splash draws it (centre of the screen, same size), so
 * the handoff reads as one continuous screen; only the name and tagline fade in below it.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier, onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onFinished()
    }

    // The ink ground needs light status/nav bar icons even when the app theme is light;
    // restore whatever the theme had set once the splash leaves.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? android.app.Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevStatus = controller?.isAppearanceLightStatusBars
        val prevNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (prevStatus != null) controller.isAppearanceLightStatusBars = prevStatus
            if (prevNav != null) controller.isAppearanceLightNavigationBars = prevNav
        }
    }

    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(250)
        reveal.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
    }

    Box(modifier.fillMaxSize().background(SplashInk), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Rada",
            // Size and offset measured against the Android 12+ system splash so the Twiga does not
            // jump or resize when this screen takes over.
            modifier = Modifier.size(LOGO_SIZE).offset(y = LOGO_OFFSET_Y)
        )
        // The wordmark and caption are each stretched (via letter-spacing) to the same width,
        // wider than the Twiga so they run past it on both sides, and centred on the Twiga's own
        // visual centre (the giraffe sits ~1dp right of the screen centre).
        Column(
            Modifier
                .align(Alignment.Center)
                .offset(x = LOGO_CENTER_X, y = 104.dp)
                .graphicsLayer {
                    alpha = reveal.value
                    translationY = (1f - reveal.value) * 14.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StretchedText(
                "Rada",
                width = LOCKUP_WIDTH,
                style = TextStyle(color = RadaPalette.DarkOnBackground, fontSize = 40.sp, fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(6.dp))
            StretchedText(
                "kaa rada na ntsa",
                width = LOCKUP_WIDTH,
                style = TextStyle(color = RadaPalette.DarkOnSurfaceVariant, fontSize = 13.sp)
            )
        }
    }
}

/**
 * Draws [text] as one line exactly [width] wide by measuring it and spreading the difference
 * across the gaps as letter-spacing (Compose adds none after the last letter, so the drawn
 * span really is [width] and stays centred).
 */
@Composable
private fun StretchedText(text: String, width: Dp, style: TextStyle) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val naturalPx = remember(text, style) {
        measurer.measure(AnnotatedString(text), style.copy(letterSpacing = 0.sp)).size.width
    }
    val gaps = (text.length - 1).coerceAtLeast(1)
    val trackingPx = ((with(density) { width.toPx() } - naturalPx) / gaps).coerceAtLeast(0f)
    Text(
        text,
        style = style.copy(letterSpacing = with(density) { trackingPx.toSp() }),
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.wrapContentWidth(unbounded = true)
    )
}

private val LOGO_SIZE = 288.dp
private val LOCKUP_WIDTH = 148.dp   // the Twiga is ~109dp wide, so this overhangs ~20dp each side
private val LOGO_CENTER_X = 1.5.dp
private val LOGO_OFFSET_Y = (-14).dp
