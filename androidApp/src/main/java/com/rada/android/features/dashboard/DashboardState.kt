package com.rada.android.features.dashboard

import com.rada.shared.domain.ReportType
import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.data.MockData

/**
 * userName now comes from UserProfilePreferences (set once via CompleteProfile after first
 * sign-in) instead of being hardcoded. greeting is computed from the actual current hour when
 * the ViewModel is created — see DashboardViewModel — not a fixed string.
 */
data class DashboardState(
    val userName: String = "",
    /** Path of the saved profile photo, or null to fall back to initials. */
    val photoPath: String? = null,
    val greeting: String = "",
    val estimatedFinesAvoidedKes: Int = MockData.estimatedFinesAvoidedKes,
    /** Reports per day for the last 7 days, oldest first, split by type. Empty until loaded. */
    val weeklyBars: List<DayBar> = emptyList(),
    val recentAlerts: List<MockData.RecentAlert> = MockData.recentAlerts,
    /** True once alerts are actually filtered to the user's area — false while location isn't
     *  available yet, so the UI can label the list honestly ("Alerts near you" vs "Recent
     *  alerts") rather than implying relevance filtering that isn't actually happening. */
    val isLocationFiltered: Boolean = false
) : MviState

/** One day on the weekly chart: how many reports of each type were made that day. */
data class DayBar(
    val label: String,
    val isToday: Boolean,
    val counts: Map<ReportType, Int>
) {
    val total: Int get() = counts.values.sum()
}

/**
 * Empty for now — Dashboard has no real async operations yet, only MockData rendered as-is.
 * Once a backend feeds real fines/alerts/chart data, add a LoadDashboard intent here that
 * calls a repository and updates state, following the same shape as DriveViewModel.
 */
sealed interface DashboardIntent : MviIntent

sealed interface DashboardEffect : MviEffect