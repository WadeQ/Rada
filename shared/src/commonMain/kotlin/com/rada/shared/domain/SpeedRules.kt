package com.rada.shared.domain

object SpeedRules {
    const val URBAN_LIMIT_KMH = 50
    const val HIGHWAY_MIN_LIMIT_KMH = 80
    const val HIGHWAY_MAX_LIMIT_KMH = 110

    sealed interface Penalty {
        data object None : Penalty
        data object Warning : Penalty
        data class Fine(val kes: Int) : Penalty
        data object BeyondInstantFines : Penalty
    }

    fun penaltyFor(speedKmh: Double, limitKmh: Double): Penalty {
        val over = speedKmh - limitKmh
        return when {
            over < 1.0 -> Penalty.None
            over <= 5.0 -> Penalty.Warning
            over <= 10.0 -> Penalty.Fine(500)
            over <= 15.0 -> Penalty.Fine(3_000)
            over <= 20.0 -> Penalty.Fine(10_000)
            else -> Penalty.BeyondInstantFines
        }
    }
}
