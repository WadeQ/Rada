package com.rada.android.features.drive

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera

/**
 * A currently-in-range camera/checkpoint/alcoblow report. Null when nothing is nearby —
 * the old DriveScreen always showed a banner; this only shows one when the engine actually
 * fires, which is the honest behavior (most of a drive has no camera nearby at all).
 */
data class ActiveAlert(
    val cameraId: String,
    val type: ReportType,
    val distanceMeters: Int,
    val speedLimitKmh: Int?,
    /** Falls back to a generic "Speed camera ahead" style label in the UI when null — real
     *  for seed cameras (researched corridor names) and for crowd reports where reverse
     *  geocoding succeeded; null when geocoding failed or the report predates that field. */
    val placeName: String? = null
)

data class DriveState(
    val currentSpeedKmh: Int = 0,
    val activeAlert: ActiveAlert? = null,
    val nearbyCameraCount: Int = 0,
    /** The actual nearby set, for the map to draw markers from — nearbyCameraCount alone
     *  (used for the "X cameras nearby" text) doesn't carry enough to render pins. */
    val nearbyCameras: List<SpeedCamera> = emptyList(),
    /** Driver's current position, for centering the map — null until the first GPS fix. */
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    /** True until the first real GPS fix arrives — the UI shows shimmer placeholders for
     *  currentSpeedKmh/nearbyCameraCount/the limit ring while this is true, rather than
     *  showing "0" and letting it look like confirmed data ("no cameras nearby") when it's
     *  actually just "we don't know yet." Cleared by the FIRST location update regardless of
     *  whether any cameras turn out to be nearby — an empty result is still a real answer. */
    val isLoading: Boolean = true,
    /** True once isLoading has lasted longer than a normal cold GPS start should reasonably
     *  take. Doesn't cancel or replace the location flow — a fix can still arrive late and
     *  will clear this normally — it just stops leaving the user staring at an unexplained
     *  shimmer forever if GPS genuinely never gets a fix (indoors, no signal, permission
     *  edge cases, an emulator with no mock location set). */
    val locationTakingTooLong: Boolean = false
) : MviState

sealed interface DriveIntent : MviIntent {
    /** Fired once, when location permission is confirmed granted — starts the GPS collection. */
    data object StartTracking : DriveIntent
}

sealed interface DriveEffect : MviEffect {
    data class ShowError(val message: String) : DriveEffect
}