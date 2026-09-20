package com.rada.shared.coordinator

import LocationProvider
import com.rada.shared.alert.AlertNotifier
import com.rada.shared.domain.SpeedAlertEngine

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
