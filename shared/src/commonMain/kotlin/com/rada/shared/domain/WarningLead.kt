package com.rada.shared.domain

enum class WarningLead(val seconds: Double, val maxMeters: Double) {
    STANDARD(7.0, 350.0),
    EARLY(10.0, 500.0),
    EARLIEST(14.0, 700.0)
}
