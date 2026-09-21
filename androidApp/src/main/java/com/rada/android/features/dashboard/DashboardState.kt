package com.rada.android.features.dashboard

import com.rada.shared.domain.ReportType
import com.rada.android.data.MonthStats
import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.data.MockData

data class DashboardState(
    val userName: String = "",
    val photoPath: String? = null,
    val greeting: String = "",
    val monthStats: MonthStats = MonthStats(),
    val weeklyBars: List<DayBar> = emptyList(),
    val recentAlerts: List<MockData.RecentAlert> = MockData.recentAlerts,
    val isLocationFiltered: Boolean = false
) : MviState

data class DayBar(
    val label: String,
    val isToday: Boolean,
    val counts: Map<ReportType, Int>
) {
    val total: Int get() = counts.values.sum()
}

sealed interface DashboardIntent : MviIntent

sealed interface DashboardEffect : MviEffect
