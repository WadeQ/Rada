package com.rada.android.tracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rada.android.MainActivity
import com.rada.shared.domain.ReportType

class AlertNotifications(private val context: Context) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ALERTS, "Camera and checkpoint alerts", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    fun post(id: String, type: ReportType, distanceMeters: Int, placeName: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val title = when (type) {
            ReportType.CAMERA -> "Speed camera ahead"
            ReportType.CHECKPOINT -> "Police checkpoint ahead"
            ReportType.ALCOBLOW -> "Alcohol check ahead"
        }
        val text = listOfNotNull(placeName, "$distanceMeters m ahead").joinToString(" · ")
        val open = PendingIntent.getActivity(
            context, 2, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
    }

    private companion object {
        const val CHANNEL_ALERTS = "rada_proximity_alerts_v2"
    }
}
