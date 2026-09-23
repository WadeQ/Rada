package com.rada.android.features.drive

import androidx.lifecycle.viewModelScope
import com.rada.android.mvi.MviViewModel
import com.rada.android.tracking.DrivingSession
import kotlinx.coroutines.launch

class DriveViewModel : MviViewModel<DriveIntent, DriveState, DriveEffect>(DriveState()) {

    init {
        viewModelScope.launch { DrivingSession.state.collect { setState { it } } }
    }

    override fun onIntent(intent: DriveIntent) {

    }
}
