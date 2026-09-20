package com.rada.android.features.drive

import LocationProvider
import androidx.lifecycle.viewModelScope
import com.rada.android.data.ReportRepository
import com.rada.android.mvi.MviViewModel
import com.rada.shared.data.MockData
import com.rada.shared.domain.CameraGridIndex
import com.rada.shared.domain.SpeedAlertEngine
import com.rada.shared.domain.SpeedCamera
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DriveViewModel(
    private val locationProvider: LocationProvider,
    private val reportRepository: ReportRepository
) : MviViewModel<DriveIntent, DriveState, DriveEffect>(DriveState()) {

    companion object {
        private const val ACTIVE_REPORT_MAX_AGE_MINUTES = 60L

        private const val LOCATION_STALL_WARNING_MS = 20_000L
    }

    private var cameraIndex = CameraGridIndex(MockData.nairobiCameras + MockData.demoAlcoblowReport)
    private val engine = SpeedAlertEngine(cameraIndex)
    private var trackingStarted = false
    private var liveReports: List<SpeedCamera> = emptyList()

    override fun onIntent(intent: DriveIntent) {
        when (intent) {
            is DriveIntent.StartTracking -> startTracking()
        }
    }

    private fun startTracking() {
        if (trackingStarted) return
        trackingStarted = true

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
            locationProvider.observeLocation().collect { driverState ->
                setState {
                    copy(
                        currentSpeedKmh = driverState.speedCurrent.toInt(),
                        isLoading = false,
                        locationTakingTooLong = false,
                        currentLatitude = driverState.latitude,
                        currentLongitude = driverState.longitude
                    )
                }

                val nearby = cameraIndex.nearby(driverState.latitude, driverState.longitude)
                setState { copy(nearbyCameraCount = nearby.size, nearbyCameras = nearby) }

                setState {
                    val alert = activeAlert
                    if (alert != null && nearby.none { it.id == alert.cameraId }) copy(activeAlert = null) else this
                }

                engine.evaluateDriverSafety(driverState) { camera, _, distanceMeters ->
                    setState {
                        copy(
                            activeAlert = ActiveAlert(
                                cameraId = camera.id,
                                type = camera.type,
                                distanceMeters = distanceMeters.toInt(),
                                speedLimitKmh = camera.speedLimit.takeIf { it > 0 }?.toInt(),
                                placeName = camera.placeName
                            )
                        )
                    }
                }
            }
        }
    }
}
