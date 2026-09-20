package com.rada.android.features.report

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.theme.RadaColors
import com.rada.shared.domain.ReportType
import kotlinx.coroutines.delay
import kotlin.math.hypot

private data class TileSpec(
    val title: String,
    val sub: String,
    val fill: Color,
    val ink: Color,
    val icon: ImageVector
)

private fun specFor(type: ReportType) = when (type) {
    ReportType.CAMERA -> TileSpec("Speed camera", "Fixed or mobile", RadaColors.Amber, RadaColors.AmberInk, Icons.Filled.CameraAlt)
    ReportType.CHECKPOINT -> TileSpec("Police checkpoint", "Document and vehicle checks", RadaColors.Orange, Color.White, Icons.Filled.Shield)
    ReportType.ALCOBLOW -> TileSpec("Alcohol check", "The warning drivers want most", RadaColors.Red, Color.White, Icons.Filled.LocalBar)
}

private const val SUCCESS_DISPLAY_MS = 2_200L

@Composable
fun ReportContentScreen(
    state: ReportState,
    onIntent: (ReportIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val view = LocalView.current

    LaunchedEffect(state.lastReport?.token) { if (state.lastReport != null) view.confirmHaptic() }
    LaunchedEffect(state.submissionError) { if (state.submissionError != null) view.rejectHaptic() }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Report",
                    color = colors.onBackground,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.6).sp
                )
                Text("One tap. We use your location.", color = colors.onSurfaceVariant, fontSize = 13.sp)
            }
            ImpactChip(state.reportsThisWeek)
        }
        Spacer(Modifier.height(24.dp))

        ReportType.entries.forEachIndexed { index, type ->
            StaggerIn(index) {
                ReportTile(
                    type = type,
                    state = state,
                    onTap = {
                        view.tickHaptic()
                        onIntent(ReportIntent.ReportTapped(type))
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.weight(1f))

        UndoChip(state.lastReport) {
            view.tickHaptic()
            onIntent(ReportIntent.UndoTapped)
        }

        AnimatedVisibility(
            visible = state.submissionError != null,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                state.submissionError ?: "",
                color = colors.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.error.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        Button(
            onClick = { onIntent(ReportIntent.DoneTapped) },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.onSurface),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Done reporting", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        progress.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 48.dp.toPx()
        }
    ) { content() }
}

@Composable
private fun ReportTile(type: ReportType, state: ReportState, onTap: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val spec = specFor(type)
    val isSubmitting = state.submittingType == type
    val busy = state.submittingType != null
    val token = state.lastReport?.takeIf { it.type == type }?.token

    var showSuccess by remember { mutableStateOf(false) }
    LaunchedEffect(token) {
        if (token == null) {
            showSuccess = false
        } else {
            showSuccess = true
            delay(SUCCESS_DISPLAY_MS)
            showSuccess = false
        }
    }

    val fill by animateFloatAsState(
        targetValue = if (showSuccess) 1f else 0f,
        animationSpec = tween(if (showSuccess) 450 else 300),
        label = "tileFill"
    )
    val check by animateFloatAsState(
        targetValue = if (showSuccess) 1f else 0f,
        animationSpec = tween(if (showSuccess) 380 else 150, delayMillis = if (showSuccess) 260 else 0),
        label = "tileCheck"
    )
    val burst = remember { Animatable(0f) }
    LaunchedEffect(token) {
        if (token != null) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(850, easing = LinearOutSlowInEasing))
        }
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tileScale"
    )
    val dim by animateFloatAsState(if (busy && !isSubmitting) 0.5f else 1f, label = "tileDim")

    val shape = RoundedCornerShape(26.dp)
    val badgeIdle = spec.fill.copy(alpha = 0.18f)
    val badgeDone = spec.ink.copy(alpha = 0.14f)

    Row(
        Modifier
            .fillMaxWidth()
            .height(104.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = dim
            }
            .clip(shape)
            .background(colors.surface)
            .drawBehind {
                val center = Offset(20.dp.toPx() + 28.dp.toPx(), size.height / 2f)
                if (fill > 0f) {
                    drawCircle(spec.fill, radius = hypot(size.width, size.height) * fill, center = center)
                }
                for (i in 0..2) {
                    val p = ((burst.value * 1.36f - i * 0.18f) / 0.82f).coerceIn(0f, 1f)
                    if (p > 0f && p < 1f) {
                        drawCircle(
                            color = spec.fill.copy(alpha = (1f - p) * 0.55f),
                            radius = 28.dp.toPx() + size.width * 0.9f * p,
                            center = center,
                            style = Stroke(3.dp.toPx())
                        )
                    }
                }
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = !busy,
                role = Role.Button,
                onClickLabel = "Report ${spec.title}",
                onClick = onTap
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(lerp(badgeIdle, badgeDone, fill)),
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = spec.fill,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(30.dp)
                )
            } else {
                Icon(
                    spec.icon,
                    contentDescription = null,
                    tint = lerp(spec.fill, spec.ink, fill),
                    modifier = Modifier.size(28.dp).graphicsLayer { alpha = 1f - check }
                )
                AnimatedCheck(progress = check, color = spec.ink, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                spec.title,
                color = lerp(colors.onSurface, spec.ink, fill),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Crossfade(
                targetState = when {
                    isSubmitting -> "Getting your location…"
                    showSuccess -> "Reported — thanks!"
                    else -> spec.sub
                },
                label = "tileSub"
            ) { text ->
                Text(text, color = lerp(colors.onSurfaceVariant, spec.ink.copy(alpha = 0.8f), fill), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AnimatedCheck(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (progress <= 0f) return@Canvas
        val path = Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.54f)
            lineTo(size.width * 0.42f, size.height * 0.76f)
            lineTo(size.width * 0.84f, size.height * 0.28f)
        }
        val measure = PathMeasure().apply { setPath(path, false) }
        val partial = Path()
        measure.getSegment(0f, measure.length * progress, partial, true)
        drawPath(
            partial,
            color,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun ImpactChip(count: Int) {
    val colors = MaterialTheme.colorScheme
    val shown by animateIntAsState(count, tween(600), label = "impactCount")

    val pop = remember { Animatable(1f) }
    var last by remember { mutableStateOf(count) }
    LaunchedEffect(count) {
        if (count > last) {
            pop.snapTo(1.3f)
            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        last = count
    }

    val active = count > 0
    Row(
        Modifier
            .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
            .clip(CircleShape)
            .background(if (active) RadaColors.Accent else colors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = if (active) RadaColors.AccentInk else colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (active) "$shown this week" else "None this week",
            color = if (active) RadaColors.AccentInk else colors.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun UndoChip(report: SubmittedReport?, onUndo: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val displayed by produceState<SubmittedReport?>(null, report) { if (report != null) value = report }

    AnimatedVisibility(
        visible = report != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        val shown = displayed ?: return@AnimatedVisibility
        val remaining = remember(shown.token) { Animatable(1f) }
        LaunchedEffect(shown.token) {
            remaining.snapTo(1f)
            remaining.animateTo(0f, tween(UNDO_WINDOW_MS.toInt(), easing = LinearEasing))
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .border(BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.08f)), CircleShape)
                .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                progress = { remaining.value },
                color = RadaColors.Accent,
                trackColor = colors.onSurface.copy(alpha = 0.1f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${specFor(shown.type).title} reported",
                color = colors.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Undo",
                color = RadaColors.AccentInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(RadaColors.Accent)
                    .clickable(onClick = onUndo)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private fun View.tickHaptic() {
    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}

private fun View.confirmHaptic() {
    performHapticFeedback(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.LONG_PRESS
    )
}

private fun View.rejectHaptic() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) performHapticFeedback(HapticFeedbackConstants.REJECT)
}
