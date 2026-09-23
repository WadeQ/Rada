package com.rada.android.car

import com.rada.shared.domain.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object CarDriveState {

    data class CarAlert(
        val type: ReportType,
        val distanceMeters: Int,
        val speedLimitKmh: Int?,
        val placeName: String?
    )

    data class Snapshot(
        val trackingActive: Boolean = false,
        val speedKmh: Int = 0,
        val nearbyCameraCount: Int = 0,
        val activeAlert: CarAlert? = null,
        val warningsThisDrive: Int = 0,
        val evadedThisDrive: Int = 0
    )

    private val _state = MutableStateFlow(Snapshot())
    val state: StateFlow<Snapshot> = _state

    fun trackingStarted() {
        _state.update { it.copy(trackingActive = true) }
    }

    fun updateDriving(speedKmh: Int, nearbyCameraCount: Int) {
        _state.update { it.copy(speedKmh = speedKmh, nearbyCameraCount = nearbyCameraCount) }
    }

    fun warningFired(type: ReportType, distanceMeters: Int, speedLimitKmh: Int?, placeName: String?) {
        _state.update {
            it.copy(
                activeAlert = CarAlert(type, distanceMeters, speedLimitKmh, placeName),
                warningsThisDrive = it.warningsThisDrive + 1
            )
        }
    }

    fun alertDistanceUpdated(distanceMeters: Int) {
        _state.update { snapshot ->
            snapshot.activeAlert?.let { snapshot.copy(activeAlert = it.copy(distanceMeters = distanceMeters)) } ?: snapshot
        }
    }

    fun alertCleared() {
        _state.update { it.copy(activeAlert = null) }
    }

    fun cameraEvaded() {
        _state.update { it.copy(evadedThisDrive = it.evadedThisDrive + 1) }
    }
}
