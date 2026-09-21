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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
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
    val axisMax = niceMax(week.maxOfOrNull { day -> day.counts.values.maxOrNull() ?: 0 } ?: 0)
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedDay = selected?.let { week.getOrNull(it) }

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
                val labelStyle = TextStyle(fontSize = 9.sp, color = mutedInk)

                for (tick in intArrayOf(0, axisMax / 2, axisMax)) {
                    val y = size.height - tick.toFloat() / axisMax * plotH
                    drawLine(
                        gridInk.copy(alpha = if (tick == 0) 0.16f else 0.07f),
                        Offset(gutterPx, y), Offset(size.width, y), strokeWidth = 1.dp.toPx()
                    )
                    val text = measurer.measure(AnnotatedString(tick.toString()), labelStyle)
                    drawText(text, topLeft = Offset(gutterPx - text.size.width - 6.dp.toPx(), y - text.size.height / 2f))
                }

                fun xAt(i: Int) = gutterPx + slotW * (i + 0.5f)
                fun yAt(count: Int) = size.height - count.toFloat() / axisMax * plotH

                selected?.let { i ->
                    drawLine(
                        gridInk.copy(alpha = 0.14f),
                        Offset(xAt(i), topPad - 4.dp.toPx()), Offset(xAt(i), size.height), strokeWidth = 1.dp.toPx()
                    )
                }

                clipRect(right = gutterPx + (size.width - gutterPx) * progress.value) {
                    ReportType.entries.forEach { type ->
                        val color = seriesColor(type, dark)
                        val counts = week.map { it.counts[type] ?: 0 }
                        val path = Path().apply {
                            moveTo(xAt(0), yAt(counts[0]))
                            for (i in 1..6) {
                                val midX = (xAt(i - 1) + xAt(i)) / 2f
                                cubicTo(midX, yAt(counts[i - 1]), midX, yAt(counts[i]), xAt(i), yAt(counts[i]))
                            }
                        }
                        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    for (i in 0..6) {
                        val isSelected = selected == i
                        ReportType.entries.forEach { type ->
                            val count = week[i].counts[type] ?: 0
                            if (count == 0 && !isSelected) return@forEach
                            val center = Offset(xAt(i), yAt(count))
                            val r = 5f - type.ordinal * 1.25f + if (isSelected) 1f else 0f
                            if (type.ordinal == 0) drawCircle(colors.surface, radius = (r + 1.5f).dp.toPx(), center = center)
                            drawCircle(seriesColor(type, dark), radius = r.dp.toPx(), center = center)
                        }
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
                    Box(Modifier.size(10.dp).clip(CircleShape).background(seriesColor(type, dark)))
                    Spacer(Modifier.width(6.dp))
                    Text("${typeLabel(type)} $count", color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}
