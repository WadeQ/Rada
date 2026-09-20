package com.rada.android.features.report

import LocationProvider
import androidx.lifecycle.viewModelScope
import com.rada.android.data.PlaceNameResolver
import com.rada.android.data.ReportHistoryPreferences
import com.rada.android.data.ReportRepository
import com.rada.android.mvi.MviViewModel
import com.rada.shared.domain.ReportType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val locationProvider: LocationProvider,
    private val placeNameResolver: PlaceNameResolver,
    private val history: ReportHistoryPreferences
) : MviViewModel<ReportIntent, ReportState, ReportEffect>(ReportState()) {
    companion object {
        private const val LOCATION_TIMEOUT_MS = 10_000L
        private const val GEOCODE_TIMEOUT_MS = 2_000L
    }

    private var undoWindowJob: Job? = null

    init {
        viewModelScope.launch {
            history.reportsThisWeek.collect { count -> setState { copy(reportsThisWeek = count) } }
        }
    }

    override fun onIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.LocationPermissionChanged -> setState { copy(hasLocationPermission = intent.granted) }
            is ReportIntent.ReportTapped -> report(intent.type)
            is ReportIntent.UndoTapped -> undo()
            is ReportIntent.DoneTapped -> sendEffect(ReportEffect.NavigateToDrive)
        }
    }

    private fun report(type: ReportType) {
        // One report in flight at a time — ignores double-taps while a fix/save is pending.
        if (currentState.submittingType != null) return
        if (!currentState.hasLocationPermission) {
            sendEffect(ReportEffect.RequestLocationPermission)
            return
        }
        undoWindowJob?.cancel()
        setState { copy(submittingType = type, lastReport = null, submissionError = null) }

        viewModelScope.launch {
            val driverState = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                locationProvider.observeLocation().first()
            }
            if (driverState == null) {
                setState {
                    copy(
                        submittingType = null,
                        submissionError = "Couldn't get your location — try again outdoors or with GPS on."
                    )
                }
                return@launch
            }

            // A slow geocoder shouldn't hold up the confirmation; the report just goes without a name.
            val placeName = withTimeoutOrNull(GEOCODE_TIMEOUT_MS) {
                placeNameResolver.resolve(driverState.latitude, driverState.longitude)
            }
            val result = reportRepository.submitReport(
                type = type,
                latitude = driverState.latitude,
                longitude = driverState.longitude,
                bearing = driverState.bearingCurrent,
                placeName = placeName
            )

            result.onFailure {
                setState {
                    copy(
                        submittingType = null,
                        submissionError = "Couldn't save your report — check your connection."
                    )
                }
            }
            result.onSuccess { id ->
                val token = System.currentTimeMillis()
                history.record(token)
                setState { copy(submittingType = null, lastReport = SubmittedReport(type, id, token)) }
                undoWindowJob = viewModelScope.launch {
                    delay(UNDO_WINDOW_MS)
                    setState { if (lastReport?.token == token) copy(lastReport = null) else this }
                }
            }
        }
    }

    private fun undo() {
        val report = currentState.lastReport ?: return
        undoWindowJob?.cancel()
        setState { copy(lastReport = null) }
        viewModelScope.launch {
            history.remove(report.token)
            reportRepository.deleteReport(report.id).onFailure {
                // The report is already live for other drivers; be honest that it wasn't removed.
                setState { copy(submissionError = "Couldn't undo — the report is still live.") }
            }
        }
    }
}
