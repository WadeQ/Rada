package com.rada.shared.alert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera

class AndroidAlertNotifier(
    private val context: Context,
    private val alertSoundResId: Int
) : AlertNotifier {

    companion object {
        private const val CHANNEL_ID = "rada_proximity_alerts"
    }

    init {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID, "Camera and checkpoint alerts", NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    override fun triggerAlert(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double) {
        val title = when (camera.type) {
            ReportType.CAMERA -> "Speed camera ahead"
            ReportType.CHECKPOINT -> "Police checkpoint ahead"
            ReportType.ALCOBLOW -> "Alcohol check ahead"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("${distanceMeters.toInt()} m ahead")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(camera.id.hashCode(), notification)

        MediaPlayer.create(context, alertSoundResId)?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .build()
            )
            setOnCompletionListener { it.release() }
            start()
        }
    }
}
