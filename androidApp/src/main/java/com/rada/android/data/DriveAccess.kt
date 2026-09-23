package com.rada.android.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * "Does this account currently have full Drive access" — a real Pro subscription, or an active
 * or not-yet-started free trial. This is the same formula RadaNavHost/DriveRouteScreen use to
 * gate the Drive screen's UI; TrackingService uses it too, now that tracking runs independently
 * of any Activity being alive, so the engine can decide for itself which report types to alert
 * on rather than depending on the UI to tell it.
 */
object DriveAccess {
    fun flow(context: Context): Flow<Boolean> =
        combine(ProRepository().proUntilMillis, TrialPreferences(context).state) { proUntil, trial ->
            val proActive = proUntil?.let { it > System.currentTimeMillis() } == true
            proActive || trial is TrialState.Active || trial is TrialState.NotStarted
        }
}
