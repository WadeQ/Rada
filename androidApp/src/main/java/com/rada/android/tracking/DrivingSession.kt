package com.rada.android.tracking

import com.rada.android.features.drive.DriveState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

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
