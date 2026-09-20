package com.rada.android.features.drive

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rada.android.screens.BOTTOM_BAR_CLEARANCE
import com.rada.android.components.ShimmerBlock
import com.rada.android.theme.RadaColors
import com.rada.shared.domain.ReportType
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun DriveContentScreen(
    state: DriveState,
    isPro: Boolean,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onHome: () -> Unit,
    onReport: () -> Unit,
    onHelp: () -> Unit,
    onUnlockTapped: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasLocationPermission) {
        PermissionNeededContent(modifier, onRequestPermission)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        val alert = state.activeAlert
        if (alert != null && alert.type != ReportType.ALCOBLOW) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(RadaColors.Amber)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = RadaColors.AmberInk)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        alert.placeName
                            ?: if (alert.type == ReportType.CHECKPOINT) "Police checkpoint ahead" else "Speed camera ahead",
                        color = RadaColors.AmberInk, fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                    val limitText = alert.speedLimitKmh?.let { " · limit $it km/h" } ?: ""
                    val typePrefix = if (alert.placeName != null) {
                        (if (alert.type == ReportType.CHECKPOINT) "Police checkpoint · " else "Speed camera · ")
                    } else ""
                    Text(
                        "$typePrefix${alert.distanceMeters} m$limitText",
                        color = RadaColors.AmberInk.copy(alpha = 0.75f), fontSize = 11.sp
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.isLoading) {
                    ShimmerBlock(modifier = Modifier
                        .width(56.dp)
                        .height(32.dp))
                } else {
                    val overLimit =
                        alert?.speedLimitKmh != null && state.currentSpeedKmh > alert.speedLimitKmh
                    Text(
                        "${state.currentSpeedKmh}",
                        color = if (overLimit) RadaColors.Red else RadaColors.TextPrimary,
                        fontSize = 38.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("km/h current", color = RadaColors.TextMuted, fontSize = 10.5.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RadaColors.Ink2)
                        .border(
                            3.dp,
                            if (alert != null) RadaColors.Red else RadaColors.Ink3,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        ShimmerBlock(modifier = Modifier.size(22.dp), cornerRadius = 4.dp)
                    } else {
                        Text(
                            alert?.speedLimitKmh?.toString() ?: "--",
                            color = RadaColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text("limit ahead", color = RadaColors.TextMuted, fontSize = 10.5.sp)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(RadaColors.Ink2)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (state.locationTakingTooLong) RadaColors.Amber else RadaColors.Accent)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.locationTakingTooLong) "Still finding your location…" else "Live tracking",
                    color = if (state.locationTakingTooLong) RadaColors.Amber else RadaColors.TextPrimary,
                    fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (state.isLoading) {
                    ShimmerBlock(modifier = Modifier
                        .width(28.dp)
                        .height(19.dp))
                    Spacer(Modifier.height(2.dp))
                } else {
                    Text(
                        "${state.nearbyCameraCount}",
                        color = RadaColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text("cameras nearby", color = RadaColors.TextMuted, fontSize = 10.sp)
            }
        }
        if (state.locationTakingTooLong) {
            Spacer(Modifier.height(6.dp))
            Text(
                "This is taking longer than usual — check that location is turned on and you have a clear view of the sky.",
                color = RadaColors.TextMuted, fontSize = 10.5.sp,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }
        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            DriveMap(state = state)
        }
        Spacer(Modifier.height(10.dp))
        if (alert != null && alert.type == ReportType.ALCOBLOW) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RadaColors.Ink2)
                    .clickable(enabled = !isPro) { onUnlockTapped() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Shield, contentDescription = null,
                    tint = if (isPro) RadaColors.Red else RadaColors.TextMuted
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (isPro) {
                        val place = alert.placeName?.let { "$it · " } ?: ""
                        "Alcoblow · $place${alert.distanceMeters} m ahead"
                    } else {
                        "Alcohol check reported ${alert.distanceMeters} m ahead"
                    },
                    color = RadaColors.TextSecondary,
                    fontSize = 11.5.sp,
                    modifier = if (isPro) Modifier.weight(1f) else Modifier
                        .weight(1f)
                        .blur(3.5.dp)
                )
                Text(
                    text = if (isPro) "View route" else "Unlock",
                    color = RadaColors.Blue,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(Modifier.height(BOTTOM_BAR_CLEARANCE))
        } else if (!isPro) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RadaColors.Ink2)
                    .clickable { onUnlockTapped() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = RadaColors.TextMuted)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Alcoblow alerts are a Pro feature — see checks reported by other drivers before you reach them.",
                    color = RadaColors.TextSecondary, fontSize = 11.5.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Upgrade",
                    color = RadaColors.Blue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(Modifier.height(BOTTOM_BAR_CLEARANCE))
        }

    }
}

private val FALLBACK_CENTER = LatLng(-1.2864, 36.8172)
private const val FOLLOW_ZOOM = 15f

@SuppressLint("MissingPermission", "UnrememberedMutableState")
@Composable
private fun DriveMap(state: DriveState) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(FALLBACK_CENTER, 11f)
    }
    var following by remember { mutableStateOf(true) }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.cameraMoveStartedReason }.collect { reason ->
            if (reason == CameraMoveStartedReason.GESTURE) following = false
        }
    }

    LaunchedEffect(state.currentLatitude, state.currentLongitude, following) {
        val lat = state.currentLatitude
        val lng = state.currentLongitude
        if (following && lat != null && lng != null) {
            val zoom = maxOf(cameraPositionState.position.zoom, FOLLOW_ZOOM)
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom))
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true),
        onMyLocationButtonClick = {
            following = true
            true
        }
    ) {
        state.nearbyCameras.forEach { camera ->
            val typeLabel = when (camera.type) {
                ReportType.CHECKPOINT -> "Police checkpoint"
                ReportType.ALCOBLOW -> "Alcohol check"
                ReportType.CAMERA -> "Speed camera"
            }
            Marker(
                state = MarkerState(position = LatLng(camera.latitude, camera.longitude)),
                title = camera.placeName ?: typeLabel,
                snippet = typeLabel
            )
        }
    }
}

@Composable
private fun PermissionNeededContent(modifier: Modifier, onRequestPermission: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = RadaColors.Amber)
        Spacer(Modifier.height(12.dp))
        Text(
            "Location access needed",
            color = RadaColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Rada checks your speed against nearby cameras using your location. Without it, Drive mode can't warn you.",
            color = RadaColors.TextSecondary, fontSize = 12.5.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant location access")
        }
    }
}
