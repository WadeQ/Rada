package com.rada.shared.data

import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera

object MockData {

    private val seedCameras = listOf(
        SpeedCamera(
            id = "thika-rd-allsops", latitude = -1.2236554, longitude = 36.8874307,
            speedLimit = 80.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Thika Superhighway, Allsops area"
        ),
        SpeedCamera(
            id = "mombasa-rd-nyayo", latitude = -1.3041092, longitude = 36.8246444,
            speedLimit = 80.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Mombasa Road, near Nyayo Stadium"
        ),
        SpeedCamera(
            id = "southern-bypass", latitude = -1.3184824, longitude = 36.7708205,
            speedLimit = 80.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Southern Bypass"
        ),
        SpeedCamera(
            id = "waiyaki-way-kangemi-uthiru", latitude = -1.2645362, longitude = 36.7402469,
            speedLimit = 60.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Waiyaki Way, Kangemi–Uthiru"
        )
    )

    val nairobiCameras: List<SpeedCamera> = seedCameras + FixedCameras.approximate

    val demoAlcoblowReport = SpeedCamera(
        id = "checkpoint-1", latitude = -1.2550, longitude = 36.8680,
        speedLimit = 0.0, bearing = 30f, type = ReportType.ALCOBLOW, isMobile = true,
        placeName = "Thika Road area"
    )

    data class RecentAlert(
        val place: String,
        val whenLabel: String,
        val type: ReportType = ReportType.CAMERA
    )

    val recentAlerts = listOf(
        RecentAlert("Thika Road, near Allsops", "Today", ReportType.CAMERA),
        RecentAlert("Southern Bypass", "Yesterday", ReportType.CAMERA)
    )

    data class WeeklyBar(val dayLabel: String, val heightPercent: Int, val isWarningDay: Boolean)

    val weeklyChart = listOf(
        WeeklyBar("M", 20, false),
        WeeklyBar("T", 45, true),
        WeeklyBar("W", 10, false),
        WeeklyBar("T", 55, true),
        WeeklyBar("F", 15, false),
        WeeklyBar("S", 30, false),
        WeeklyBar("S", 5, false)
    )

    const val estimatedFinesAvoidedKes = 40000

    data class Contact(
        val name: String,
        val meta: String,
        val phone: String,
        val category: ContactCategory
    )

    enum class ContactCategory { EMERGENCY, TOW, MECHANIC }

    val roadsideContacts = listOf(
        Contact("Police", "999 / 112", "999", ContactCategory.EMERGENCY),
        Contact("Ambulance", "St John · 1199", "1199", ContactCategory.EMERGENCY),
        Contact(
            "Fire brigade",
            "Nairobi County · 020 222 2181",
            "0202222181",
            ContactCategory.EMERGENCY
        ),
        Contact("AA Kenya Rescue", "2.1 km · 24 hours", "+254709945000", ContactCategory.TOW),
        Contact("Kenswitch Towing", "3.4 km · Thika Rd", "+254722000000", ContactCategory.TOW),
        Contact(
            "Yaya Motors",
            "4.6 rating · Industrial Area",
            "+254733000000",
            ContactCategory.MECHANIC
        ),
        Contact(
            "Westlands Auto Care",
            "4.4 rating · Westlands",
            "+254720000000",
            ContactCategory.MECHANIC
        )
    )

    data class PricingPlan(
        val id: String,
        val name: String,
        val priceLabel: String,
        val perMonthLabel: String,
        val recommended: Boolean,
        val amountKes: Int
    )

    val pricingPlans = listOf(
        PricingPlan(
            "annual",
            "Annual",
            "KES 1,500 / year",
            "KES 150/mo",
            recommended = true,
            amountKes = 1200
        ),
        PricingPlan(
            "monthly",
            "Monthly",
            "Billed every month",
            "KES 200/mo",
            recommended = false,
            amountKes = 2500
        )
    )
}
