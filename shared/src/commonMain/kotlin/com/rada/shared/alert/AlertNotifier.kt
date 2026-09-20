package com.rada.shared.alert

import com.rada.shared.domain.SpeedCamera

interface AlertNotifier {
    fun triggerAlert(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double)
}
