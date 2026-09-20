package com.rada.android.features.report

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.domain.ReportType

const val UNDO_WINDOW_MS = 5_000L

data class SubmittedReport(
    val type: ReportType,
    val id: String,
    val token: Long
)

data class ReportState(
    val hasLocationPermission: Boolean = false,
    val submittingType: ReportType? = null,
    val lastReport: SubmittedReport? = null,
    val reportsThisWeek: Int = 0,
    val submissionError: String? = null
) : MviState

sealed interface ReportIntent : MviIntent {
    data class LocationPermissionChanged(val granted: Boolean) : ReportIntent
    data class ReportTapped(val type: ReportType) : ReportIntent
    data object UndoTapped : ReportIntent
    data object DoneTapped : ReportIntent
}

sealed interface ReportEffect : MviEffect {
    data object NavigateToDrive : ReportEffect
    data object RequestLocationPermission : ReportEffect
}
