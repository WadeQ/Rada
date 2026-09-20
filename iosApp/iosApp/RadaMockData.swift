import Foundation

/// Mirrors shared/src/commonMain/.../MockData.kt by value. In a fully wired build these
/// would instead come from `import Shared` and read the Kotlin MockData object directly —
/// duplicated here so this iOS target is self-contained and readable without requiring the
/// built XCFramework. Replace both copies with a real repository before shipping.
enum RadaMockData {

    struct RecentAlert { let place: String; let whenLabel: String; let type: ReportKind }
    /// Mock until iOS has a backend; more than three so the "See all" toggle has something to do.
    static let recentAlerts = [
        RecentAlert(place: "Kilimani", whenLabel: "4 h ago", type: .camera),
        RecentAlert(place: "Embakasi", whenLabel: "4 h ago", type: .camera),
        RecentAlert(place: "Kilimani", whenLabel: "5 h ago", type: .alcoblow),
        RecentAlert(place: "Thika Road, near Allsops", whenLabel: "Yesterday", type: .checkpoint),
        RecentAlert(place: "Southern Bypass, Karen exit", whenLabel: "Yesterday", type: .camera)
    ]

    /// Reports per day for the last 7 days, oldest first, split by type. Mock counts; labels are real.
    static func weeklyReports(now: Date = Date()) -> [DayBar] {
        let counts: [[ReportKind: Int]] = [
            [:],
            [.checkpoint: 1, .alcoblow: 1],
            [:],
            [.camera: 1],
            [.camera: 2, .checkpoint: 1],
            [:],
            [.camera: 3, .checkpoint: 1, .alcoblow: 3]
        ]
        let cal = Calendar.current
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE"
        return (0..<7).map { i in
            let daysAgo = 6 - i
            let day = cal.date(byAdding: .day, value: -daysAgo, to: now) ?? now
            return DayBar(id: i, label: daysAgo == 0 ? "Today" : formatter.string(from: day),
                          isToday: daysAgo == 0, counts: counts[i])
        }
    }

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
        PricingPlan(id: "annual", name: "Annual", priceLabel: "KES 1,200 / year", perMonthLabel: "KES 100/mo", recommended: true, amountKes: 1200),
        PricingPlan(id: "monthly", name: "Monthly", priceLabel: "Billed every month", perMonthLabel: "KES 150/mo", recommended: false, amountKes: 150)
    ]
}

enum ReportKind: String, CaseIterable {
    case camera = "Camera"
    case checkpoint = "Checkpoint"
    case alcoblow = "Alcoblow"

    /// How it's named to the driver.
    var title: String {
        switch self {
        case .camera: return "Speed camera"
        case .checkpoint: return "Police checkpoint"
        case .alcoblow: return "Alcohol check"
        }
    }
}

/// One day on the weekly chart: how many reports of each type were made that day.
struct DayBar: Identifiable {
    let id: Int
    let label: String
    let isToday: Bool
    let counts: [ReportKind: Int]
    var total: Int { counts.values.reduce(0, +) }
}
