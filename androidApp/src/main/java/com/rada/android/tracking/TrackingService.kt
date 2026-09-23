package com.rada.android.tracking

import LocationProvider
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.rada.android.MainActivity
import com.rada.android.car.CarDriveState
import com.rada.android.data.AlertPreferences
import com.rada.android.data.DriveAccess
import com.rada.android.data.FirestoreReportRepository
import com.rada.android.data.ReportRepository
import com.rada.android.data.StatsRepository
import com.rada.android.domain.isVisibleTo
import com.rada.android.features.drive.ActiveAlert
import com.rada.shared.data.MockData
import com.rada.shared.domain.CameraGridIndex
import com.rada.shared.domain.SpeedAlertEngine
import com.rada.shared.domain.SpeedCamera
import com.rada.shared.location.AndroidLocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ACTIVE_REPORT_MAX_AGE_MINUTES = 60L
private const val LOCATION_STALL_WARNING_MS = 20_000L
private const val STALE_AFTER_MS = 6_000L
private const val MIN_SPEED_FOR_HEADING_KMH = 4.0
private const val WATCHDOG_INTERVAL_MS = 2_000L
private const val FINAL_WARNING_METERS = 120f

class TrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var trackingStarted = false
    private var locationJob: Job? = null
    private var cameraIndex = CameraGridIndex(MockData.nairobiCameras + MockData.demoAlcoblowReport)
    private val engine = SpeedAlertEngine(cameraIndex)
    @Volatile private var hasAccess = false
    @Volatile private var inForeground = true
    @Volatile private var alertsEnabled = true
    private var lastFixAt = 0L
    private val finalWarned = mutableSetOf<String>()

    private lateinit var locationProvider: LocationProvider
    private lateinit var reportRepository: ReportRepository
    private lateinit var speaker: AlertSpeaker
    private lateinit var notifications: AlertNotifications
    private lateinit var stats: StatsRepository

    private val foregroundObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> inForeground = true
            Lifecycle.Event.ON_STOP -> inForeground = false
            else -> Unit
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationProvider = AndroidLocationProvider(LocationServices.getFusedLocationProviderClient(this))
        reportRepository = FirestoreReportRepository()
        speaker = AlertSpeaker(this)
        notifications = AlertNotifications(applicationContext)
        stats = StatsRepository()

        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTrackingAndSelf()
            return START_NOT_STICKY
        }
        ensureChannel(this)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
        startTracking()

        return START_STICKY
    }

    private fun startTracking() {
        if (trackingStarted) return
        trackingStarted = true
        CarDriveState.trackingStarted()

        serviceScope.launch { DriveAccess.flow(applicationContext).collect { hasAccess = it } }
        serviceScope.launch { AlertPreferences(applicationContext).alertsEnabled.collect { alertsEnabled = it } }
        serviceScope.launch { AlertPreferences(applicationContext).warningLead.collect { engine.warningLead = it } }

        serviceScope.launch {
            reportRepository.observeActiveCameraReports(ACTIVE_REPORT_MAX_AGE_MINUTES).collect { reports ->
                cameraIndex = CameraGridIndex(MockData.nairobiCameras + MockData.demoAlcoblowReport + reports)
                engine.updateGridIndex(cameraIndex)
            }
        }

        serviceScope.launch {
            delay(LOCATION_STALL_WARNING_MS)
            if (DrivingSession.state.value.isLoading) {
                DrivingSession.update { it.copy(locationTakingTooLong = true) }
            }
        }

        serviceScope.launch {
            locationProvider.getLastKnownLocation()?.let { last ->
                DrivingSession.update {
                    if (it.currentLatitude == null) it.copy(currentLatitude = last.latitude, currentLongitude = last.longitude) else it
                }
            }
        }

        serviceScope.launch {
            while (true) {
                delay(WATCHDOG_INTERVAL_MS)
                val running = locationJob?.isActive == true
                if (running && SystemClock.elapsedRealtime() - lastFixAt > STALE_AFTER_MS) {
                    if (!DrivingSession.state.value.gpsStale) DrivingSession.update { it.copy(gpsStale = true) }
                    startLocation()
                }
            }
        }

        startLocation()
    }

    private fun handleWarning(camera: SpeedCamera, distanceMeters: Double) {
        if (!camera.type.isVisibleTo(hasAccess)) return
        stats.recordWarning()
        val limitKmh = camera.speedLimit.takeIf { it > 0 }?.toInt()
        if (alertsEnabled) speaker.warn(camera.type, distanceMeters.toInt(), camera.placeName, limitKmh)
        CarDriveState.warningFired(camera.type, distanceMeters.toInt(), limitKmh, camera.placeName)
        if (!inForeground && alertsEnabled) {
            notifications.post(camera.id, camera.type, distanceMeters.toInt(), camera.placeName)
        }
        DrivingSession.update {
            it.copy(
                activeAlert = ActiveAlert(
                    cameraId = camera.id,
                    type = camera.type,
                    distanceMeters = distanceMeters.toInt(),
                    speedLimitKmh = limitKmh,
                    placeName = camera.placeName,
                    approximate = camera.approximate
                )
            )
        }
    }

    private fun startLocation() {
        locationJob?.cancel()
        lastFixAt = SystemClock.elapsedRealtime()
        locationJob = serviceScope.launch {
            locationProvider.observeLocation().collect { driverState ->
                lastFixAt = SystemClock.elapsedRealtime()
                DrivingSession.update {
                    it.copy(
                        gpsStale = false,
                        currentSpeedKmh = driverState.speedCurrent.toInt(),
                        isLoading = false,
                        locationTakingTooLong = false,
                        currentLatitude = driverState.latitude,
                        currentLongitude = driverState.longitude,
                        bearingDegrees = if (driverState.speedCurrent > MIN_SPEED_FOR_HEADING_KMH) driverState.bearingCurrent else it.bearingDegrees
                    )
                }

                val nearbyAll = cameraIndex.nearby(driverState.latitude, driverState.longitude)
                val nearby = nearbyAll.filter { it.type.isVisibleTo(hasAccess) }
                val locked = nearbyAll.size - nearby.size
                DrivingSession.update { it.copy(nearbyCameraCount = nearby.size, nearbyCameras = nearby, lockedNearbyCount = locked) }
                CarDriveState.updateDriving(driverState.speedCurrent.toInt(), nearby.size)

                DrivingSession.update { state ->
                    val alert = state.activeAlert
                    if (alert != null && nearby.none { it.id == alert.cameraId }) {
                        CarDriveState.alertCleared()
                        state.copy(activeAlert = null)
                    } else state
                }

                finalWarned.retainAll(nearby.map { it.id }.toSet())
                DrivingSession.state.value.activeAlert?.let { alert ->
                    val camera = nearby.firstOrNull { it.id == alert.cameraId } ?: return@let
                    val result = FloatArray(1)
                    Location.distanceBetween(
                        driverState.latitude, driverState.longitude, camera.latitude, camera.longitude, result
                    )
                    DrivingSession.update { it.copy(activeAlert = it.activeAlert?.copy(distanceMeters = result[0].toInt())) }
                    CarDriveState.alertDistanceUpdated(result[0].toInt())
                    if (alertsEnabled && result[0] <= FINAL_WARNING_METERS && finalWarned.add(camera.id)) {
                        speaker.finalWarning()
                    }
                }

                engine.evaluateDriverSafety(driverState, object : SpeedAlertEngine.AlertListener {
                    override fun onOutcome(camera: SpeedCamera, evaded: Boolean, speedKmh: Double, limitKmh: Double) {
                        if (evaded) {
                            stats.recordEvaded()
                            CarDriveState.cameraEvaded()
                        }
                    }

                    override fun onWarning(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double) {
                        handleWarning(camera, distanceMeters)
                    }
                })
            }
        }
    }

    private fun stopTrackingAndSelf() {
        serviceScope.cancel()
        speaker.release()
        trackingStarted = false
        DrivingSession.reset()
        runCatching { ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        if (::speaker.isInitialized) speaker.release()
        runCatching { ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver) }
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, TrackingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_TRACKING)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Rada is watching the road")
            .setContentText("Alerts stay on while you use other apps")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(open)
            .addAction(0, "Stop", stop)
            .build()
    }

    companion object {
        private const val ACTION_STOP = "com.rada.android.tracking.STOP"
        private const val NOTIFICATION_ID = 4101
        const val CHANNEL_TRACKING = "rada_tracking"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, TrackingService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingService::class.java))
        }

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_TRACKING, "Drive tracking", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }
}
