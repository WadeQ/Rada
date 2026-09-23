package com.rada.android.tracking

import com.rada.android.features.drive.DriveState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * TrackingService is the sole owner of live Drive state — the phone's Drive screen only ever
 * reads from here (DriveViewModel mirrors this into its own MviState), the same way the
 * Android Auto screen only ever reads CarDriveState. Neither screen being open is required for
 * tracking to keep running; this is what makes that true for the phone UI specifically.
 */
object DrivingSession {
    private val _state = MutableStateFlow(DriveState())
    val state: StateFlow<DriveState> = _state

    fun update(transform: (DriveState) -> DriveState) {
        _state.update(transform)
    }

    fun reset() {
        _state.value = DriveState()
    }
}
