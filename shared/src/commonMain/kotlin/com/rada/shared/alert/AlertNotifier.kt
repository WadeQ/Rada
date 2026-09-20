package com.rada.shared.alert

import com.rada.shared.domain.SpeedCamera

/**
 * Fires the noisy warning. Platform actuals decide the mechanism (system notification vs
 * in-app audio vs both) since Android and iOS have very different constraints here — e.g.
 * routing audio through the media stream so it plays even with the ringer silenced.
 */
interface AlertNotifier {
    fun triggerAlert(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double)
}
