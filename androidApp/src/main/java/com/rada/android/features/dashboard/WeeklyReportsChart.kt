package com.rada.android.features.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.shared.domain.ReportType
import kotlin.math.max
import kotlin.math.min

/**
 * Categorical series colours, validated for colour-blind separation and contrast on the app's
 * card surface in both themes. Stack order is fixed (camera, checkpoint, alcohol), so the
 * pairs that touch are always camera/checkpoint and checkpoint/alcohol; yellow and red never
 * sit next to each other. The light-mode yellow is under 3:1 on white, so counts are always
 * shown as text in the legend, never by colour alone.
 */
private fun seriesColor(type: ReportType, dark: Boolean): Color = when (type) {
    ReportType.CAMERA -> if (dark) Color(0xFFC98500) else Color(0xFFEDA100)
    ReportType.CHECKPOINT -> if (dark) Color(0xFF3987E5) else Color(0xFF2A78D6)
    ReportType.ALCOBLOW -> if (dark) Color(0xFFE66767) else Color(0xFFE34948)
}

private fun typeLabel(type: ReportType) = when (type) {
    ReportType.CAMERA -> "Camera"
    ReportType.CHECKPOINT -> "Checkpoint"
    ReportType.ALCOBLOW -> "Alcohol"
}

/** Smallest even axis maximum that fits [value], so the mid gridline is a whole number. */
private fun niceMax(value: Int): Int {
    val steps = intArrayOf(4, 6, 8, 10, 12, 16, 20, 30, 40, 50, 80, 100)
    return steps.firstOrNull { it >= value } ?: ((value + 49) / 50 * 50)
}

private val EmptyWeek = List(7) { DayBar("", false, emptyMap()) }

@Composable
fun WeeklyReportsChart(bars: List<DayBar>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < 0.5f
    val week = bars.ifEmpty { EmptyWeek }
    val weekTotal = week.sumOf { it.total }
    val axisMax = niceMax(week.maxOfOrNull { it.total } ?: 0)
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedDay = selected?.let { week.getOrNull(it) }

    // Bars grow up from the baseline, staggered left to right, whenever the data changes.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(week.map { it.counts }) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900, easing = LinearOutSlowInEasing))
    }

    val summary = week.joinToString(", ") { "${it.label.ifEmpty { "day" }} ${it.total}" }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("This week", color = colors.onSurface, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
            Text(
                when {
                    selectedDay != null -> "${selectedDay.label} · ${selectedDay.total} ${if (selectedDay.total == 1) "report" else "reports"}"
                    weekTotal == 0 -> "No reports yet"
                    else -> "$weekTotal ${if (weekTotal == 1) "report" else "reports"}"
                },
                color = colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(6.dp))

        val gutter = 22.dp
        val measurer = rememberTextMeasurer()
        val mutedInk = colors.onSurfaceVariant.copy(alpha = 0.8f)
        val gridInk = colors.onSurface

        Box(Modifier.fillMaxWidth().height(84.dp).semantics { contentDescription = "Reports per day, last 7 days: $summary" }) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .pointerInput(week) {
                        detectTapGestures { offset ->
                            val slotW = (size.width - gutter.toPx()) / 7f
                            val index = ((offset.x - gutter.toPx()) / slotW).toInt()
                            if (offset.x >= gutter.toPx() && index in 0..6) {
                                selected = if (selected == index) null else index
                            }
                        }
                    }
            ) {
                val gutterPx = gutter.toPx()
                val topPad = 8.dp.toPx()
                val plotH = size.height - topPad
                val slotW = (size.width - gutterPx) / 7f
                val barW = min(slotW * 0.56f, 26.dp.toPx())
                val gap = 2.dp.toPx()
                val labelStyle = TextStyle(fontSize = 9.sp, color = mutedInk)

                // Recessive grid: 0, half, max
                for (tick in intArrayOf(0, axisMax / 2, axisMax)) {
                    val y = size.height - tick.toFloat() / axisMax * plotH
                    drawLine(
                        gridInk.copy(alpha = if (tick == 0) 0.16f else 0.07f),
                        Offset(gutterPx, y), Offset(size.width, y), strokeWidth = 1.dp.toPx()
                    )
                    val text = measurer.measure(AnnotatedString(tick.toString()), labelStyle)
                    drawText(text, topLeft = Offset(gutterPx - text.size.width - 6.dp.toPx(), y - text.size.height / 2f))
                }

                week.forEachIndexed { i, day ->
                    val cx = gutterPx + slotW * (i + 0.5f)
                    if (selected == i) {
                        drawRoundRect(
                            gridInk.copy(alpha = 0.06f),
                            topLeft = Offset(cx - slotW / 2f + 2.dp.toPx(), topPad - 6.dp.toPx()),
                            size = Size(slotW - 4.dp.toPx(), plotH + 6.dp.toPx()),
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                    }
                    val p = (progress.value * 1.5f - i * 0.08f).coerceIn(0f, 1f)
                    val alpha = if (selected != null && selected != i) 0.45f else 1f
                    val present = ReportType.entries.filter { (day.counts[it] ?: 0) > 0 }
                    var y = size.height
                    present.forEachIndexed { k, type ->
                        val h = max((day.counts.getValue(type).toFloat() / axisMax) * plotH, 3.dp.toPx()) * p
                        if (h <= 0f) return@forEachIndexed
                        val yTop = y - h
                        // 2dp surface gap between stacked segments; only the top segment is rounded
                        val yBottom = y - if (k > 0) gap else 0f
                        val isTop = k == present.lastIndex
                        val r = if (isTop) min(4.dp.toPx(), (yBottom - yTop) / 2f) else 0f
                        val path = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = cx - barW / 2f, top = yTop, right = cx + barW / 2f, bottom = yBottom,
                                    topLeftCornerRadius = CornerRadius(r), topRightCornerRadius = CornerRadius(r),
                                    bottomLeftCornerRadius = CornerRadius.Zero, bottomRightCornerRadius = CornerRadius.Zero
                                )
                            )
                        }
                        drawPath(path, seriesColor(type, dark).copy(alpha = alpha))
                        y = yTop
                    }
                }
            }
            if (bars.isNotEmpty() && weekTotal == 0) {
                Text(
                    "No reports near you in the last 7 days",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(start = gutter)
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Spacer(Modifier.width(gutter))
            week.forEachIndexed { i, day ->
                Text(
                    day.label,
                    color = if (day.isToday || selected == i) colors.onSurface else colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ReportType.entries.forEach { type ->
                val count = selectedDay?.counts?.get(type) ?: week.sumOf { it.counts[type] ?: 0 }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(seriesColor(type, dark)))
                    Spacer(Modifier.width(6.dp))
                    // Text stays in ink; the swatch carries the colour identity
                    Text("${typeLabel(type)} $count", color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}
