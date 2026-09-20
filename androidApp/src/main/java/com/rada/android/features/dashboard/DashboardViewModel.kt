package com.rada.android.features.dashboard

import LocationProvider
import androidx.lifecycle.viewModelScope
import com.rada.android.data.ReportEvent
import com.rada.android.data.ReportRepository
import com.rada.android.data.UserProfilePreferences
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.launch
import com.rada.shared.domain.ReportType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class DashboardViewModel(
    private val userProfilePreferences: UserProfilePreferences,
    private val reportRepository: ReportRepository,
    private val locationProvider: LocationProvider?
) : MviViewModel<DashboardIntent, DashboardState, DashboardEffect>(
    DashboardState(greeting = computeGreeting())
) {
    companion object {
        private const val NEARBY_RADIUS_KM = 25.0
        private const val MAX_ALERTS = 10L

        fun computeGreeting(): String {
            val hour = LocalTime.now().hour
            return when {
                hour < 12 -> "Good morning"
                hour < 17 -> "Good afternoon"
                else -> "Good evening"
            }
        }
    }

    init {
        viewModelScope.launch {
            userProfilePreferences.userName.collect { name ->
                setState { copy(userName = name ?: "Driver") }
            }
        }
        viewModelScope.launch {
            userProfilePreferences.photoPath.collect { path -> setState { copy(photoPath = path) } }
        }
        viewModelScope.launch {
            val location = locationProvider?.getLastKnownLocation()
            setState { copy(isLocationFiltered = location != null) }

            launch {
                val alerts = if (location != null) {
                    reportRepository.observeNearbyReports(
                        location.latitude, location.longitude, NEARBY_RADIUS_KM, limit = MAX_ALERTS
                    )
                } else {
                    reportRepository.observeRecentReports(limit = MAX_ALERTS)
                }
                alerts.collect { list -> setState { copy(recentAlerts = list) } }
            }
            launch {
                reportRepository.observeReportEvents(
                    maxAgeDays = 7,
                    centerLat = location?.latitude,
                    centerLng = location?.longitude,
                    radiusKm = NEARBY_RADIUS_KM
                ).collect { events -> setState { copy(weeklyBars = buildWeek(events)) } }
            }
        }
    }

    private fun buildWeek(events: List<ReportEvent>): List<DayBar> {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val byDay = events.groupBy { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() }
        return (6 downTo 0).map { daysAgo ->
            val day = today.minusDays(daysAgo.toLong())
            val counts = byDay[day].orEmpty().groupingBy { it.type }.eachCount()
            DayBar(
                label = if (daysAgo == 0) "Today" else day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                isToday = daysAgo == 0,
                counts = ReportType.entries.associateWith { counts[it] ?: 0 }
            )
        }
    }

    override fun onIntent(intent: DashboardIntent) {
    }
}
