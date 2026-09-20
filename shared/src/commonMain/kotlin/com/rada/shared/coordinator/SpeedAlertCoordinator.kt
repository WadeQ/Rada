package com.rada.shared.coordinator

import LocationProvider
import com.rada.shared.alert.AlertNotifier
import com.rada.shared.domain.SpeedAlertEngine

/**
 * Wires the platform location feed into the shared engine and routes results to the
 * platform alert sink. This class is the entire integration surface between shared logic
 * and platform code — everything above this (UI) and below it (GPS/notifications) is
 * platform-specific; everything inside the engine is shared and identical on both platforms.
 */
class SpeedAlertCoordinator(
    private val locationProvider: LocationProvider,
    private val engine: SpeedAlertEngine,
    private val notifier: AlertNotifier
) {
    private val listener = SpeedAlertEngine.AlertListener { camera, speed, distance ->
        notifier.triggerAlert(camera, speed, distance)
    }

    suspend fun start() {
        locationProvider.observeLocation().collect { driverState ->
            engine.evaluateDriverSafety(driverState, listener)
        }
    }
}
