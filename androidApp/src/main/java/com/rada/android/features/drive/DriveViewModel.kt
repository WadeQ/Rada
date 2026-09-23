package com.rada.android.features.drive

import androidx.lifecycle.viewModelScope
import com.rada.android.mvi.MviViewModel
import com.rada.android.tracking.DrivingSession
import kotlinx.coroutines.launch

/**
 * A thin mirror of TrackingService's DrivingSession — the service is the sole owner of the
 * engine now (see TrackingService's class doc for why), so there's nothing left for this
 * ViewModel to do but reflect that shared state into MVI shape for DriveContentScreen. Starting
 * the service itself still happens at the Compose layer (DriveRouteScreen), the same call site
 * as before; DriveIntent's other cases are kept only so existing call sites keep compiling —
 * none of them mean anything to a ViewModel that no longer runs the engine itself.
 */
class DriveViewModel : MviViewModel<DriveIntent, DriveState, DriveEffect>(DriveState()) {

    init {
        viewModelScope.launch { DrivingSession.state.collect { setState { it } } }
    }

    override fun onIntent(intent: DriveIntent) {
        // No-op: TrackingService resolves access, alert preferences and foreground state for
        // itself (DriveAccess, AlertPreferences, ProcessLifecycleOwner) rather than being told.
    }
}
