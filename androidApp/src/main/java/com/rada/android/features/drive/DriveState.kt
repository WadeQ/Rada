package com.rada.android.features.drive

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera

data class ActiveAlert(
    val cameraId: String,
    val type: ReportType,
    val distanceMeters: Int,
    val speedLimitKmh: Int?,
    val placeName: String? = null
)

data class DriveState(
    val currentSpeedKmh: Int = 0,
    val activeAlert: ActiveAlert? = null,
    val nearbyCameraCount: Int = 0,
    val nearbyCameras: List<SpeedCamera> = emptyList(),
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val bearingDegrees: Float = 0f,
    val isLoading: Boolean = true,
    val locationTakingTooLong: Boolean = false,
    val gpsStale: Boolean = false,
    val lockedNearbyCount: Int = 0
) : MviState

sealed interface DriveIntent : MviIntent {
    data object StartTracking : DriveIntent
    data object AppResumed : DriveIntent
    data object AppPaused : DriveIntent
    data class ProChanged(val isPro: Boolean) : DriveIntent
}

sealed interface DriveEffect : MviEffect {
    data class ShowError(val message: String) : DriveEffect
}
