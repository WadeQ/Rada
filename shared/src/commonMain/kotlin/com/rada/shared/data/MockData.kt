package com.rada.shared.data

import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera

/**
 * Static demo content standing in for a real backend/database. Mirrors the values used in
 * the HTML click-through prototype so the three surfaces (web mockup, Android, iOS) show
 * the same story. Replace with a real repository (Room/SQLDelight + a sync API) once the
 * backend exists — nothing here should survive into production as-is.
 */
object MockData {

    /**
     * Seed cameras — NOT NTSA-verified exact positions. NTSA has explicitly not published a
     * public map of precise camera locations (confirmed by search: they withhold exact
     * positions specifically to deter drivers from learning to dodge enforcement zones), so no
     * authoritative dataset exists to draw from. These four points are real, geocoded
     * locations (via Google Places) along road corridors that multiple news sources report as
     * having active NTSA speed enforcement — the coordinate is a verified point on that road,
     * not a confirmed camera position, and speed limits are as publicly reported, not officially
     * confirmed either. Treat this as a rough, disclosed-as-approximate starting seed; the real,
     * improving dataset is whatever drivers report via the Report screen into Firestore.
     */
    val nairobiCameras = listOf(
        SpeedCamera(
            // Thika Superhighway, Allsops/GSU area — reported 80 km/h zone (some sections up
            // to 110 km/h elsewhere on the same highway; 80 is the figure cited for this area).
            id = "thika-rd-allsops", latitude = -1.2236554, longitude = 36.8874307,
            speedLimit = 80.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Thika Superhighway, Allsops area"
        ),
        SpeedCamera(
            // Mombasa Road, Nyayo Stadium to Sameer Business Park stretch — reported 80 km/h.
            id = "mombasa-rd-nyayo", latitude = -1.3041092, longitude = 36.8246444,
            speedLimit = 80.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Mombasa Road, near Nyayo Stadium"
        ),
        SpeedCamera(
            // Southern Bypass — reported as an active NTSA testing/enforcement corridor;
            // no specific speed limit figure found in public reporting, so 80 here is an
            // estimate for a bypass-class road, not a cited number like the other three.
            id = "southern-bypass", latitude = -1.3184824, longitude = 36.7708205,
            speedLimit = 80.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Southern Bypass"
        ),
        SpeedCamera(
            // Waiyaki Way, Kangemi to Uthiru stretch — reported range is 60-80 km/h; using
            // the lower, more conservative bound rather than picking the upper one.
            id = "waiyaki-way-kangemi-uthiru", latitude = -1.2645362, longitude = 36.7402469,
            speedLimit = 60.0, bearing = 30f, type = ReportType.CAMERA,
            placeName = "Waiyaki Way, Kangemi–Uthiru"
        )
    )

    /** A single demo checkpoint report, used to drive the Drive screen's Pro-gated teaser.
     *  Same disclosure as above applies — an approximate point near real reported alcoblow
     *  activity, not a confirmed checkpoint location (checkpoints move constantly anyway). */
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
        /** Actual amount to charge, in KES — the display strings above are for the UI only;
         *  this is what a real payment call needs and the display strings never had. */
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