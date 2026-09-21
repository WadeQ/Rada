package com.rada.shared.rada

/// Mirrors shared/src/commonMain/.../MockData.kt by value. In a fully wired build these
/// would instead come from `import Shared` and read the Kotlin MockData object directly —
/// duplicated here so this iOS target is self-contained and readable without requiring the
/// built XCFramework. Replace both copies with a real repository before shipping.
enum RadaMockData {

    struct RecentAlert { let place: String; let whenLabel: String; let type: ReportKind }
    static let recentAlerts = [
    RecentAlert(place: "Thika Road, near Allsops", whenLabel: "Today", type: .camera),
    RecentAlert(place: "Southern Bypass", whenLabel: "Yesterday", type: .camera)
    ]

    struct WeeklyBar { let day: String; let heightPercent: Double; let isWarningDay: Bool }
    static let weeklyChart = [
    WeeklyBar(day: "M", heightPercent: 20, isWarningDay: false),
    WeeklyBar(day: "T", heightPercent: 45, isWarningDay: true),
    WeeklyBar(day: "W", heightPercent: 10, isWarningDay: false),
    WeeklyBar(day: "T", heightPercent: 55, isWarningDay: true),
    WeeklyBar(day: "F", heightPercent: 15, isWarningDay: false),
    WeeklyBar(day: "S", heightPercent: 30, isWarningDay: false),
    WeeklyBar(day: "S", heightPercent: 5, isWarningDay: false)
    ]

    static let estimatedFinesAvoidedKes = 40000

    enum ContactCategory { case emergency, tow, mechanic }
    struct Contact { let name: String; let meta: String; let phone: String; let category: ContactCategory }
    static let roadsideContacts = [
    Contact(name: "Police", meta: "999 / 112", phone: "999", category: .emergency),
    Contact(name: "Ambulance", meta: "St John · 1199", phone: "1199", category: .emergency),
    Contact(name: "Fire brigade", meta: "Nairobi County · 020 222 2181", phone: "0202222181", category: .emergency),
    Contact(name: "AA Kenya Rescue", meta: "2.1 km · 24 hours", phone: "+254709945000", category: .tow),
    Contact(name: "Kenswitch Towing", meta: "3.4 km · Thika Rd", phone: "+254722000000", category: .tow),
    Contact(name: "Yaya Motors", meta: "4.6 rating · Industrial Area", phone: "+254733000000", category: .mechanic),
    Contact(name: "Westlands Auto Care", meta: "4.4 rating · Westlands", phone: "+254720000000", category: .mechanic)
    ]

    struct PricingPlan { let id: String; let name: String; let priceLabel: String; let perMonthLabel: String; let recommended: Bool; let amountKes: Int }
    static let pricingPlans = [
    PricingPlan(id: "annual", name: "Annual", priceLabel: "KES 1,800 / year", perMonthLabel: "KES 150/mo", recommended: true, amountKes: 1800),
    PricingPlan(id: "monthly", name: "Monthly", priceLabel: "Billed every month", perMonthLabel: "KES 200/mo", recommended: false, amountKes: 200)
    ]
}

enum ReportKind: String, CaseIterable {
    case camera = "Camera"
    case checkpoint = "Checkpoint"
    case alcoblow = "Alcoblow"
}