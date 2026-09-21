package com.rada.shared.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpeedAlertEngineTest {
    private val camera = SpeedCamera(
        id = "cam", latitude = -1.2550, longitude = 36.8680, speedLimit = 80.0, bearing = 30f
    )

    private fun at(metresBefore: Double, speed: Double, lateral: Double = 0.0): DriverState {
        val b = 30.0 * PI / 180
        val lat = camera.latitude - metresBefore * cos(b) / 111_320.0
        val lng = camera.longitude - metresBefore * sin(b) / (111_320.0 * cos(camera.latitude * PI / 180)) + lateral / 111_320.0
        return DriverState(lat, lng, speed, 30f)
    }

    private class Recorder : SpeedAlertEngine.AlertListener {
        val warnings = mutableListOf<Double>()
        val outcomes = mutableListOf<Boolean>()
        override fun onWarning(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double) {
            warnings += distanceMeters
        }

        override fun onOutcome(camera: SpeedCamera, evaded: Boolean, speedKmh: Double, limitKmh: Double) {
            outcomes += evaded
        }
    }

    private fun drive(speeds: (Double) -> Double, from: Double = 400.0, to: Double = -120.0, step: Double = 20.0): Recorder {
        val engine = SpeedAlertEngine(CameraGridIndex(listOf(camera)))
        val recorder = Recorder()
        var d = from
        while (d >= to) {
            engine.evaluateDriverSafety(at(d, speeds(d)), recorder)
            d -= step
        }
        return recorder
    }

    @Test
    fun slowingDownAfterTheWarningCountsAsEvaded() {
        val r = drive({ d -> if (d > 150) 100.0 else 70.0 })
        assertEquals(1, r.warnings.size)
        assertEquals(listOf(true), r.outcomes)
    }

    @Test
    fun staying_over_the_limit_is_not_evaded() {
        val r = drive({ 100.0 })
        assertEquals(1, r.warnings.size)
        assertEquals(listOf(false), r.outcomes)
    }

    @Test
    fun slowingRightAfterTheWarningDoesNotEndTheApproachEarly() {
        val r = drive({ d -> if (d > 170) 100.0 else 25.0 })
        assertEquals(1, r.outcomes.size)
        assertTrue(r.outcomes.single())
    }

    @Test
    fun turningOffBeforeReachingTheCameraCountsNothing() {
        val engine = SpeedAlertEngine(CameraGridIndex(listOf(camera)))
        val r = Recorder()
        var d = 400.0
        while (d >= 200.0) { engine.evaluateDriverSafety(at(d, 100.0), r); d -= 20.0 }
        var lateral = 0.0
        repeat(30) { engine.evaluateDriverSafety(at(180.0, 100.0, lateral), r); lateral += 60.0 }
        assertEquals(1, r.warnings.size)
        assertTrue(r.outcomes.isEmpty())
    }

    @Test
    fun drivingUnderTheLimitNeverWarns() {
        val r = drive({ 60.0 })
        assertTrue(r.warnings.isEmpty())
        assertTrue(r.outcomes.isEmpty())
    }
}
