package com.rada.shared.alert

import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

class IosAlertNotifier : AlertNotifier {

    override fun triggerAlert(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)

        val title = when (camera.type) {
            ReportType.CAMERA -> "Speed camera ahead"
            ReportType.CHECKPOINT -> "Police checkpoint ahead"
            ReportType.ALCOBLOW -> "Alcohol check ahead"
        }
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody("${distanceMeters.toInt()} m ahead")
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = camera.id,
            content = content,
            trigger = null
        )
        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request, withCompletionHandler = null)
    }
}
