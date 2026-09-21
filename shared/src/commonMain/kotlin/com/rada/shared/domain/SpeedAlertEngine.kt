package com.rada.shared.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class SpeedAlertEngine(private var gridIndex: CameraGridIndex) {
    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val MIN_ALERT_DISTANCE_METERS = 60.0
        private const val MIN_SPEED_FOR_BEARING_CHECK_KMH = 8.0
        private const val BEARING_TOLERANCE_DEGREES = 20.0
        private const val RESET_DISTANCE_MULTIPLIER = 2.0
        private const val MIN_CLOSING_METERS = 1.0
        private const val PASS_CONFIRM_METERS = 30.0
        private const val MAX_PASS_DISTANCE_METERS = 90.0
        private const val SPEED_TOLERANCE_KMH = 2.0
    }

    fun interface AlertListener {
        fun onWarning(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double)

        fun onOutcome(camera: SpeedCamera, evaded: Boolean, speedKmh: Double, limitKmh: Double) {}
    }
    @kotlin.concurrent.Volatile
    var warningLead: WarningLead = WarningLead.STANDARD

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
            val previousDistance = state.lastDistanceMeters
            state.lastDistanceMeters = distance

            when (state.status) {
                CameraAlertStatus.NOT_ALERTED -> {
                    if (distance <= alertDistance && isRelevantApproach(driver, camera, previousDistance, distance)) {
                        val shouldFire = when (camera.type) {
                            ReportType.CAMERA -> driver.speedCurrent > camera.speedLimit
                            ReportType.CHECKPOINT, ReportType.ALCOBLOW -> true
                        }
                        if (shouldFire) {
                            listener.onWarning(camera, driver.speedCurrent, distance)
                            state.status = CameraAlertStatus.ALERTED
                            state.closestDistanceMeters = distance
                            state.speedAtClosestKmh = driver.speedCurrent
                        }
                    }
                }

                CameraAlertStatus.ALERTED -> {
                    if (distance < state.closestDistanceMeters) {
                        state.closestDistanceMeters = distance
                        state.speedAtClosestKmh = driver.speedCurrent
                    }
                    val movedAway = distance > state.closestDistanceMeters + PASS_CONFIRM_METERS
                    val farAway = distance > warningLead.maxMeters * 2
                    if (movedAway || farAway) {
                        state.status = CameraAlertStatus.PASSED
                        val isFixedCamera = camera.type == ReportType.CAMERA && camera.speedLimit > 0
                        if (isFixedCamera && state.closestDistanceMeters <= MAX_PASS_DISTANCE_METERS) {
                            listener.onOutcome(
                                camera,
                                state.speedAtClosestKmh <= camera.speedLimit + SPEED_TOLERANCE_KMH,
                                state.speedAtClosestKmh,
                                camera.speedLimit
                            )
                        }
                        state.closestDistanceMeters = Double.MAX_VALUE
                    }
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

    private fun computeAlertDistance(speedKmh: Double): Double {
        val speedMs = speedKmh / 3.6
        val lead = warningLead
        return (speedMs * lead.seconds)
            .coerceIn(MIN_ALERT_DISTANCE_METERS, lead.maxMeters)
    }

    private fun isRelevantApproach(
        driver: DriverState,
        camera: SpeedCamera,
        previousDistance: Double,
        distance: Double
    ): Boolean {
        if (camera.bearing == SpeedCamera.ANY_DIRECTION) {
            return previousDistance != Double.MAX_VALUE && distance < previousDistance - MIN_CLOSING_METERS
        }
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
