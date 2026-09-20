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
        // Live reports older than this are treated as expired and dropped from the active
        // set — a checkpoint reported 3 hours ago has most likely moved on or been cleared,
        // and keeping it "active" indefinitely would eventually just be wrong information.
        private const val ACTIVE_REPORT_MAX_AGE_MINUTES = 60L

        // A normal cold GPS start can reasonably take several seconds — this is deliberately
        // longer than Report's 10s one-shot timeout (which fails outright) since this is a
        // "still waiting, here's why" notice rather than a hard failure; the location flow
        // itself is never cancelled, so a fix arriving after this still works normally.
        private const val LOCATION_STALL_WARNING_MS = 20_000L
    }

    // Mutable so it can be rebuilt whenever live reports change — kept as a single field
    // (rather than duplicating "build an index from a list" inside the engine too) so both
    // this class's own nearby-count queries and the engine's alert checks read the exact
    // same up-to-date set.
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
        // Guards against restarting collection on every recomposition — StartTracking can be
        // dispatched more than once, but each flow should only be collected once per
        // ViewModel lifetime.
        if (trackingStarted) return
        trackingStarted = true

        viewModelScope.launch {
            reportRepository.observeActiveCameraReports(ACTIVE_REPORT_MAX_AGE_MINUTES).collect { reports ->
                liveReports = reports
                cameraIndex = CameraGridIndex(MockData.nairobiCameras + MockData.demoAlcoblowReport + liveReports)
                engine.updateGridIndex(cameraIndex)
            }
        }

        // Doesn't cancel or race against the location flow below — just checks, once, whether
        // isLoading is still true after a generous grace period, and if so tells the user
        // why nothing's showing instead of leaving the shimmer running with no explanation.
        viewModelScope.launch {
            delay(LOCATION_STALL_WARNING_MS)
            if (currentState.isLoading) {
                setState { copy(locationTakingTooLong = true) }
            }
        }

        // The live flow can take several seconds for its first fix; the last known position is
        // available almost immediately, so seed the map's position from it. isLoading stays
        // true — this is only "where to look", not confirmed live data.
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
                // Idempotent after the first emission — cheap to keep setting false on every
                // update rather than tracking a separate "have we seen the first one yet" flag.
                // Also clears locationTakingTooLong in case a fix arrived after that notice
                // had already fired — the notice was never a hard failure, just a "still
                // waiting" state that a late fix resolves normally.
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

                // The engine only exposes a listener for a NEW alert firing, not for one
                // clearing — clearing a stale alert here once its camera falls out of the
                // grid's nearby cells (~1km resolution) is a coarse approximation of "you've
                // left the vicinity," not an exact match to the engine's precise alert radius.
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