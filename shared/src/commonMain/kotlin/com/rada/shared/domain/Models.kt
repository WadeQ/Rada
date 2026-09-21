package com.rada.shared.domain

enum class ReportType {
    CAMERA,
    CHECKPOINT,
    ALCOBLOW
}

data class SpeedCamera(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedLimit: Double,
    val bearing: Float,
    val type: ReportType = ReportType.CAMERA,
    val isMobile: Boolean = false,
    val placeName: String? = null,
    val approximate: Boolean = false
) {
    companion object {
        const val ANY_DIRECTION = -1f
    }
}

data class DriverState(
    val latitude: Double,
    val longitude: Double,
    val speedCurrent: Double,
    val bearingCurrent: Float
)

enum class CameraAlertStatus {
    NOT_ALERTED,
    ALERTED,
    PASSED
}

data class CameraTrackingState(
    var status: CameraAlertStatus = CameraAlertStatus.NOT_ALERTED,
    var lastDistanceMeters: Double = Double.MAX_VALUE,
    var closestDistanceMeters: Double = Double.MAX_VALUE,
    var speedAtClosestKmh: Double = 0.0
)

object FineEstimates {
    const val AVG_SPEED_FINE_KES = 10_000
}
