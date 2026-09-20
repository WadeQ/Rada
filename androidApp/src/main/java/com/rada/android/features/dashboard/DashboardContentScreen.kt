package com.rada.android.features.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.ProfileAvatar
import com.rada.android.screens.BOTTOM_BAR_CLEARANCE
import com.rada.android.theme.RadaColors
import com.rada.shared.data.MockData
import com.rada.shared.domain.ReportType

@Composable
fun DashboardContentScreen(
    state: DashboardState,
    isPro: Boolean,
    onDrive: () -> Unit,
    onReport: () -> Unit,
    onHelp: () -> Unit,
    onUpgrade: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(state.greeting, color = RadaColors.TextSecondary, fontSize = 12.sp)
                Text(
                    state.userName,
                    color = RadaColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            ProfileAvatar(
                name = state.userName,
                photoPath = state.photoPath,
                size = 36.dp,
                initialsCount = 2
            )
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(RadaColors.Ink2)
                    .padding(16.dp)
            ) {
                Text(
                    "Estimated fines avoided this month",
                    color = RadaColors.Blue,
                    fontSize = 11.5.sp
                )
                Text(
                    "KES ${state.estimatedFinesAvoidedKes}",
                    color = RadaColors.TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "4 warnings acted on · 10k avg fine per strike",
                    color = RadaColors.Blue,
                    fontSize = 11.5.sp
                )
            }
            Spacer(Modifier.height(16.dp))

            WeeklyReportsChart(state.weeklyBars)
            Spacer(Modifier.height(20.dp))

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(RadaColors.Ink2)
                    .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = RadaColors.Blue)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isPro) "You're on Pro" else "Go Pro · KES 150/month",
                        color = RadaColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (isPro) "Real-time checkpoints, longer warnings and offline maps are on."
                        else "Crowd checkpoints, longer warning distance, offline maps.",
                        color = RadaColors.TextSecondary, fontSize = 11.5.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onUpgrade,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RadaColors.Accent,
                        contentColor = RadaColors.AccentInk
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(if (isPro) "Manage" else "Upgrade", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))

            // Only the newest few alerts show by default so the list stays short; the rest
            // expand inline via "See all".
            var showAllAlerts by rememberSaveable { mutableStateOf(false) }
            val hasMoreAlerts = state.recentAlerts.size > COLLAPSED_ALERT_COUNT

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.isLocationFiltered) "Alerts near you" else "Recent alerts",
                    color = RadaColors.TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
                if (hasMoreAlerts) {
                    Text(
                        if (showAllAlerts) "Show less" else "See all ${state.recentAlerts.size}",
                        color = RadaColors.Blue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAllAlerts = !showAllAlerts }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (state.recentAlerts.isEmpty()) {
                Text(
                    if (state.isLocationFiltered) "No alerts near you yet — reports from your area will show up here."
                    else "No alerts yet — reports you and other drivers make will show up here.",
                    color = RadaColors.TextMuted, fontSize = 11.5.sp
                )
            } else {
                state.recentAlerts.take(COLLAPSED_ALERT_COUNT).forEach { alert ->
                    RecentAlertCard(alert)
                    Spacer(Modifier.height(8.dp))
                }
                AnimatedVisibility(
                    visible = showAllAlerts,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        state.recentAlerts.drop(COLLAPSED_ALERT_COUNT).forEach { alert ->
                            RecentAlertCard(alert)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(BOTTOM_BAR_CLEARANCE))
        }

    }
}

private const val COLLAPSED_ALERT_COUNT = 3

@Composable
private fun RecentAlertCard(alert: MockData.RecentAlert) {
    val (badgeColor, icon) = when (alert.type) {
        ReportType.CAMERA -> RadaColors.Amber to Icons.Filled.CameraAlt
        ReportType.CHECKPOINT -> RadaColors.Orange to Icons.Filled.Shield
        ReportType.ALCOBLOW -> RadaColors.Red to Icons.Filled.LocalBar
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RadaColors.Ink2)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(badgeColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                alert.place,
                color = RadaColors.TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                when (alert.type) {
                    ReportType.CAMERA -> "Speed camera"
                    ReportType.CHECKPOINT -> "Police checkpoint"
                    ReportType.ALCOBLOW -> "Alcohol check"
                },
                color = RadaColors.TextMuted, fontSize = 10.5.sp
            )
        }
        Text(alert.whenLabel, color = RadaColors.TextMuted, fontSize = 11.sp)
    }
}