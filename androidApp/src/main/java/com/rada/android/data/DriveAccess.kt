package com.rada.android.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

object DriveAccess {
    fun flow(context: Context): Flow<Boolean> =
        combine(ProRepository().proUntilMillis, TrialPreferences(context).state) { proUntil, trial ->
            val proActive = proUntil?.let { it > System.currentTimeMillis() } == true
            proActive || trial is TrialState.Active || trial is TrialState.NotStarted
        }
}
