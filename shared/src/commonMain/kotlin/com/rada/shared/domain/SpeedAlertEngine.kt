package com.rada.shared.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The platform-agnostic core of the app: given a driver's location/speed and a nearby
 * camera list, decides when to fire an alert. Runs identically on Android and iOS since
 * it touches no platform APIs — only the location feed and the notification sink
 * (see LocationProvider / AlertNotifier) are platform-specific.
 */
class SpeedAlertEngine(private var gridIndex: CameraGridIndex) {
    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val REACTION_TIME_SECONDS = 7.0
        private const val MIN_ALERT_DISTANCE_METERS = 60.0
        private const val MAX_ALERT_DISTANCE_METERS = 350.0
        private const val MIN_SPEED_FOR_BEARING_CHECK_KMH = 8.0
        private const val BEARING_TOLERANCE_DEGREES = 20.0
        private const val RESET_DISTANCE_MULTIPLIER = 2.0
    }

    fun interface AlertListener {
        fun onWarning(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double)
    }
    private val trackingStates = mutableMapOf<String, CameraTrackingState>()
    fun updateGridIndex(newGridIndex: CameraGridIndex) {
        gridIndex = newGridIndex
    }
    fun evaluateDriverSafety(driver: DriverState, listener: AlertListener) {
        val localCameras = gridIndex.nearby(driver.latitude, driver.longitude)
        val alertDistance = computeAlertDistance(driver.speedCurrent)

        for (camera in localCameras) {
            val distance = haversineMeters(
                driver.latitude, driver.longitude,
                camera.latitude, camera.longitude
            )
            val state = trackingStates.getOrPut(camera.id) { CameraTrackingState() }
            state.lastDistanceMeters = distance

            when (state.status) {
                CameraAlertStatus.NOT_ALERTED -> {
                    if (distance <= alertDistance && isRelevantApproach(driver, camera)) {
                        val shouldFire = when (camera.type) {
                            ReportType.CAMERA -> driver.speedCurrent > camera.speedLimit
                            ReportType.CHECKPOINT, ReportType.ALCOBLOW -> true
                        }
                        if (shouldFire) {
                            listener.onWarning(camera, driver.speedCurrent, distance)
                            state.status = CameraAlertStatus.ALERTED
                        }
                    }
                }

                CameraAlertStatus.ALERTED -> {
                    if (distance > alertDistance) state.status = CameraAlertStatus.PASSED
                }

                CameraAlertStatus.PASSED -> {
                    if (distance > alertDistance * RESET_DISTANCE_MULTIPLIER) {
                        state.status = CameraAlertStatus.NOT_ALERTED
                    }
                }
            }
        }

        val localIds = localCameras.map { it.id }.toSet()
        trackingStates.keys.retainAll { id ->
            id in localIds || trackingStates[id]?.status == CameraAlertStatus.ALERTED
        }
    }

    /** Speed-scaled alert distance, clamped to a sane floor/ceiling. Tune against real driving. */
    private fun computeAlertDistance(speedKmh: Double): Double {
        val speedMs = speedKmh / 3.6
        return (speedMs * REACTION_TIME_SECONDS)
            .coerceIn(MIN_ALERT_DISTANCE_METERS, MAX_ALERT_DISTANCE_METERS)
    }

    private fun isRelevantApproach(driver: DriverState, camera: SpeedCamera): Boolean {
        if (driver.speedCurrent < MIN_SPEED_FOR_BEARING_CHECK_KMH) return true
        return isHeadingTowardCamera(driver.bearingCurrent, camera.bearing)
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
    private fun toRadians(degrees: Double): Double = degrees * PI / 180.0

    private fun isHeadingTowardCamera(driverBearing: Float, cameraBearing: Float): Boolean {
        val diff = abs(driverBearing - cameraBearing)
        val normalized = if (diff > 180) 360 - diff else diff
        return normalized <= BEARING_TOLERANCE_DEGREES
    }
}
