package com.rada.android.features.drive

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
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
import kotlinx.coroutines.delay
import kotlin.math.ln
import kotlin.math.cos
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
import com.rada.shared.domain.SpeedRules
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
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
                    val approxText = if (alert.approximate) " · approx. location" else ""
                    Text(
                        "$typePrefix${alert.distanceMeters} m$limitText$approxText",
                        color = RadaColors.AmberInk.copy(alpha = 0.75f), fontSize = 11.sp
                    )
                    val penaltyText = alert.speedLimitKmh?.let { limit ->
                        when (val penalty = SpeedRules.penaltyFor(state.currentSpeedKmh.toDouble(), limit.toDouble())) {
                            is SpeedRules.Penalty.Warning -> "Over the limit · warning"
                            is SpeedRules.Penalty.Fine -> "${state.currentSpeedKmh - limit} km/h over · KES ${"%,d".format(penalty.kes)} fine"
                            is SpeedRules.Penalty.BeyondInstantFines -> "${state.currentSpeedKmh - limit} km/h over · beyond instant fines"
                            else -> null
                        }
                    }
                    if (penaltyText != null) {
                        Text(penaltyText, color = RadaColors.AmberInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
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
                } else if (state.gpsStale) {
                    Text("--", color = RadaColors.TextMuted, fontSize = 38.sp, fontWeight = FontWeight.Bold)
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
                Text(
                    if (state.gpsStale) "Searching for GPS…" else "km/h current",
                    color = if (state.gpsStale) RadaColors.Amber else RadaColors.TextMuted,
                    fontSize = 10.5.sp
                )
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
        if ((alert != null && alert.type == ReportType.ALCOBLOW) || (!isPro && state.lockedNearbyCount > 0)) {
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
                    text = if (isPro && alert != null) {
                        val place = alert.placeName?.let { "$it · " } ?: ""
                        "Alcoblow · $place${alert.distanceMeters} m ahead"
                    } else {
                        "Checkpoint or alcohol check reported nearby"
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
                    "Checkpoint and alcohol-check alerts are a Pro feature — see checks reported by other drivers before you reach them.",
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
private const val FOLLOW_ZOOM = 16f
private const val RESUME_FOLLOW_MS = 8_000L
private const val ZOOM_ANIM_MS = 900

private fun followZoom(speedKmh: Int, latitude: Double?, cameraDistanceMeters: Int?): Float {
    val bySpeed = when {
        speedKmh < 25 -> 17f
        speedKmh < 50 -> 16.3f
        speedKmh < 80 -> 15.5f
        else -> 14.8f
    }
    if (cameraDistanceMeters == null || cameraDistanceMeters > CAMERA_ZOOM_RANGE_M || latitude == null) return bySpeed
    val metersPerDp = cameraDistanceMeters.coerceAtLeast(40) * 1.3 / HALF_MAP_DP
    val forCamera = (ln(156_543.03 * cos(Math.toRadians(latitude)) / metersPerDp) / ln(2.0)).toFloat()
    return minOf(bySpeed, forCamera).coerceIn(13.5f, 17.5f)
}

private const val CAMERA_ZOOM_RANGE_M = 1_000
private const val HALF_MAP_DP = 260.0

@SuppressLint("MissingPermission", "UnrememberedMutableState")
@Composable
private fun DriveMap(state: DriveState) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(FALLBACK_CENTER, 11f)
    }
    var following by remember { mutableStateOf(true) }
    val zoom = remember { Animatable(FOLLOW_ZOOM) }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.cameraMoveStartedReason }.collect { reason ->
            if (reason == CameraMoveStartedReason.GESTURE) following = false
        }
    }

    LaunchedEffect(following) {
        if (!following) {
            delay(RESUME_FOLLOW_MS)
            following = true
        }
    }

    val density = LocalDensity.current.density
    var carIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val appContext = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        MapsInitializer.initialize(appContext)
        carIcon = buildCarIcon(density)
    }
    val carState = remember { MarkerState(position = FALLBACK_CENTER) }
    val glide = remember { Animatable(1f) }
    var glideFrom by remember { mutableStateOf(FALLBACK_CENTER) }
    var glideTo by remember { mutableStateOf(FALLBACK_CENTER) }
    var placedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(state.currentLatitude, state.currentLongitude) {
        val lat = state.currentLatitude ?: return@LaunchedEffect
        val lng = state.currentLongitude ?: return@LaunchedEffect
        val target = LatLng(lat, lng)
        if (!placedOnce) {
            placedOnce = true
            glideFrom = target; glideTo = target
            carState.position = target
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target, zoom.value))
            return@LaunchedEffect
        }
        glideFrom = carState.position
        glideTo = target
        glide.snapTo(0f)
        glide.animateTo(1f, tween(GLIDE_MS, easing = LinearEasing)) {
            val f = value.toDouble()
            carState.position = LatLng(
                glideFrom.latitude + (glideTo.latitude - glideFrom.latitude) * f,
                glideFrom.longitude + (glideTo.longitude - glideFrom.longitude) * f
            )
            if (following) cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(carState.position, zoom.value))
        }
    }

    val alert = state.activeAlert
    val alertCamera = alert?.let { a -> state.nearbyCameras.firstOrNull { it.id == a.cameraId } }
    val targetZoom = followZoom(state.currentSpeedKmh, state.currentLatitude, alert?.distanceMeters?.takeIf { alertCamera != null })
    LaunchedEffect(targetZoom) {
        zoom.animateTo(targetZoom, tween(ZOOM_ANIM_MS, easing = LinearEasing)) {
            if (following && placedOnce) {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(carState.position, value))
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
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
                    snippet = if (camera.approximate) "$typeLabel · approximate location" else typeLabel
                )
            }
            val icon = carIcon
            if (placedOnce && icon != null) {
                Marker(
                    state = carState,
                    icon = icon,
                    flat = true,
                    rotation = state.bearingDegrees,
                    anchor = Offset(0.5f, 0.5f),
                    zIndex = 10f
                )
            }
        }
        IconButton(
            onClick = { following = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(RadaColors.Ink2)
        ) {
            Icon(
                Icons.Filled.MyLocation,
                contentDescription = "Recentre on my car",
                tint = if (following) RadaColors.Accent else RadaColors.TextSecondary
            )
        }
    }
}

private const val GLIDE_MS = 900

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
