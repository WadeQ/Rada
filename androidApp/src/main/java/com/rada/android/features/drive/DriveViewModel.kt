package com.rada.android.features.drive

import LocationProvider
import androidx.lifecycle.viewModelScope
import com.rada.android.data.AlertPreferences
import com.rada.android.data.ReportRepository
import com.rada.android.data.StatsRepository
import com.rada.android.tracking.AlertNotifications
import com.rada.android.tracking.AlertSpeaker
import com.rada.android.domain.isVisibleTo
import com.rada.android.mvi.MviViewModel
import com.rada.shared.data.MockData
import com.rada.shared.domain.CameraGridIndex
import com.rada.shared.domain.SpeedAlertEngine
import com.rada.shared.domain.SpeedCamera
import android.os.SystemClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DriveViewModel(
    private val locationProvider: LocationProvider,
    private val reportRepository: ReportRepository,
    private val alertPreferences: AlertPreferences,
    private val notifications: AlertNotifications,
    private val speaker: AlertSpeaker,
    private val stats: StatsRepository
) : MviViewModel<DriveIntent, DriveState, DriveEffect>(DriveState()) {

    companion object {
        private const val ACTIVE_REPORT_MAX_AGE_MINUTES = 60L

        private const val LOCATION_STALL_WARNING_MS = 20_000L
        private const val STALE_AFTER_MS = 6_000L
        private const val MIN_SPEED_FOR_HEADING_KMH = 4.0
        private const val WATCHDOG_INTERVAL_MS = 2_000L
        private const val FINAL_WARNING_METERS = 120f
    }

    private var cameraIndex = CameraGridIndex(MockData.nairobiCameras + MockData.demoAlcoblowReport)
    private val engine = SpeedAlertEngine(cameraIndex)
    private var trackingStarted = false
    private var locationJob: Job? = null
    @Volatile private var isPro = false
    @Volatile private var inForeground = true
    @Volatile private var alertsEnabled = true
    private var lastFixAt = 0L
    private var liveReports: List<SpeedCamera> = emptyList()
    private val finalWarned = mutableSetOf<String>()

    override fun onCleared() {
        speaker.release()
        super.onCleared()
    }

    override fun onIntent(intent: DriveIntent) {
        when (intent) {
            is DriveIntent.StartTracking -> startTracking()
            is DriveIntent.AppResumed -> {
                inForeground = true
                if (trackingStarted) startLocation()
            }
            is DriveIntent.AppPaused -> inForeground = false
            is DriveIntent.ProChanged -> isPro = intent.isPro
        }
    }

    private fun startTracking() {
        if (trackingStarted) return
        trackingStarted = true

        viewModelScope.launch {
            alertPreferences.alertsEnabled.collect { alertsEnabled = it }
        }

        viewModelScope.launch {
            alertPreferences.warningLead.collect { engine.warningLead = it }
        }

        viewModelScope.launch {
            reportRepository.observeActiveCameraReports(ACTIVE_REPORT_MAX_AGE_MINUTES).collect { reports ->
                liveReports = reports
                cameraIndex = CameraGridIndex(MockData.nairobiCameras + MockData.demoAlcoblowReport + liveReports)
                engine.updateGridIndex(cameraIndex)
            }
        }

        viewModelScope.launch {
            delay(LOCATION_STALL_WARNING_MS)
            if (currentState.isLoading) {
                setState { copy(locationTakingTooLong = true) }
            }
        }

        viewModelScope.launch {
            locationProvider.getLastKnownLocation()?.let { last ->
                setState {
                    if (currentLatitude == null) copy(currentLatitude = last.latitude, currentLongitude = last.longitude)
                    else this
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(WATCHDOG_INTERVAL_MS)
                val running = locationJob?.isActive == true
                if (running && SystemClock.elapsedRealtime() - lastFixAt > STALE_AFTER_MS) {
                    if (!currentState.gpsStale) setState { copy(gpsStale = true) }
                    startLocation()
                }
            }
        }

        startLocation()
    }

    private fun handleWarning(camera: SpeedCamera, distanceMeters: Double) {
        if (!camera.type.isVisibleTo(isPro)) return
        stats.recordWarning()
        if (alertsEnabled) speaker.warn(camera.type, distanceMeters.toInt(), camera.placeName)
        if (!inForeground && alertsEnabled) {
            notifications.post(camera.id, camera.type, distanceMeters.toInt(), camera.placeName)
        }
        setState {
            copy(
                activeAlert = ActiveAlert(
                    cameraId = camera.id,
                    type = camera.type,
                    distanceMeters = distanceMeters.toInt(),
                    speedLimitKmh = camera.speedLimit.takeIf { it > 0 }?.toInt(),
                    placeName = camera.placeName,
                    approximate = camera.approximate
                )
            )
        }
    }

    private fun startLocation() {
        locationJob?.cancel()
        lastFixAt = SystemClock.elapsedRealtime()
        locationJob = viewModelScope.launch {
            locationProvider.observeLocation().collect { driverState ->
                lastFixAt = SystemClock.elapsedRealtime()
                setState {
                    copy(
                        gpsStale = false,
                        currentSpeedKmh = driverState.speedCurrent.toInt(),
                        isLoading = false,
                        locationTakingTooLong = false,
                        currentLatitude = driverState.latitude,
                        currentLongitude = driverState.longitude,
                        bearingDegrees = if (driverState.speedCurrent > MIN_SPEED_FOR_HEADING_KMH) driverState.bearingCurrent else bearingDegrees
                    )
                }

                val nearbyAll = cameraIndex.nearby(driverState.latitude, driverState.longitude)
                val nearby = nearbyAll.filter { it.type.isVisibleTo(isPro) }
                val locked = nearbyAll.size - nearby.size
                setState { copy(nearbyCameraCount = nearby.size, nearbyCameras = nearby, lockedNearbyCount = locked) }

                setState {
                    val alert = activeAlert
                    if (alert != null && nearby.none { it.id == alert.cameraId }) copy(activeAlert = null) else this
                }

                finalWarned.retainAll(nearby.map { it.id }.toSet())
                currentState.activeAlert?.let { alert ->
                    val camera = nearby.firstOrNull { it.id == alert.cameraId } ?: return@let
                    val result = FloatArray(1)
                    android.location.Location.distanceBetween(
                        driverState.latitude, driverState.longitude, camera.latitude, camera.longitude, result
                    )
                    setState { copy(activeAlert = activeAlert?.copy(distanceMeters = result[0].toInt())) }
                    if (alertsEnabled && result[0] <= FINAL_WARNING_METERS && finalWarned.add(camera.id)) {
                        speaker.finalWarning()
                    }
                }

                engine.evaluateDriverSafety(driverState, object : SpeedAlertEngine.AlertListener {
                    override fun onOutcome(camera: SpeedCamera, evaded: Boolean, speedKmh: Double, limitKmh: Double) {
                        if (evaded) stats.recordEvaded()
                    }

                    override fun onWarning(camera: SpeedCamera, currentSpeed: Double, distanceMeters: Double) {
                        handleWarning(camera, distanceMeters)
                    }
                })
            }
        }
    }
}
