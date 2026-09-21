import SwiftUI

struct DashboardView: View {
    @EnvironmentObject private var profile: ProfileStore
    let isPro: Bool
    let onUpgrade: () -> Void

    @State private var animatedFines: Double = 0
    @State private var showAllAlerts = false

    init(isPro: Bool, onUpgrade: @escaping () -> Void, initialFines: Double = 0) {
        self.isPro = isPro
        self.onUpgrade = onUpgrade
        _animatedFines = State(initialValue: initialFines)
    }

    private static let collapsedAlertCount = 3
    private var alerts: [RadaMockData.RecentAlert] { RadaMockData.recentAlerts }

    var body: some View {
        VStack(spacing: 0) {
            header
                .padding(.horizontal, 18).padding(.top, 16).padding(.bottom, 16)

            ScrollView(showsIndicators: false) { scrollContent }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.9)) { animatedFines = Double(RadaMockData.estimatedFinesAvoidedKes) }
        }
    }

    var scrollContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            finesCard
            WeeklyReportsChart(bars: RadaMockData.weeklyReports())
                .padding(.top, 16)
            proBanner
                .padding(.top, 16)
            alertsSection
                .padding(.top, 20)
            Spacer().frame(height: bottomBarClearance)
        }
        .padding(.horizontal, 18)
    }

    private var greeting: String {
        let hour = Calendar.current.component(.hour, from: Date())
        return hour < 12 ? "Good morning" : (hour < 17 ? "Good afternoon" : "Good evening")
    }

    private var header: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(greeting).font(.system(size: 12)).foregroundColor(.radaTextSecondary)
                Text(profile.displayName)
                    .font(.system(size: 22, weight: .heavy)).foregroundColor(.radaBurgundyText)
            }
            Spacer()
            ProfileAvatar(name: profile.displayName, photo: profile.photo, size: 36, initialsCount: 2)
        }
    }

    private var finesCard: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("Estimated fines avoided this month").font(.system(size: 11.5)).foregroundColor(.radaAccentText)
            Text("KES \(Int(animatedFines))").font(.system(size: 28, weight: .bold)).foregroundColor(.radaTextPrimary)
            Text("4 warnings acted on · 10k avg fine per strike").font(.system(size: 11.5)).foregroundColor(.radaAccentText)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 22).fill(Color.radaInk2))
    }

    private var proBanner: some View {
        HStack(spacing: 10) {
            Image(systemName: "checkmark.shield.fill").foregroundColor(.radaAccentText)
            VStack(alignment: .leading, spacing: 2) {
                Text(isPro ? "You're on Pro" : "Go Pro · KES 200/month")
                    .font(.system(size: 13, weight: .medium)).foregroundColor(.radaTextPrimary)
                Text(isPro ? "Real-time checkpoints, longer warnings and offline maps are on."
                           : "Crowd checkpoints, longer warning distance, offline maps.")
                    .font(.system(size: 11.5)).foregroundColor(.radaTextSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
            Spacer(minLength: 6)
            Button(action: onUpgrade) {
                Text(isPro ? "Manage" : "Upgrade")
                    .font(.system(size: 12.5, weight: .semibold))
                    .foregroundColor(.radaAccentInk)
                    .padding(.horizontal, 16)
                    .frame(height: 38)
                    .background(Capsule().fill(Color.radaAccent))
            }
            .buttonStyle(RadaPressStyle())
        }
        .padding(.leading, 14).padding(.trailing, 12).padding(.vertical, 12)
        .background(RoundedRectangle(cornerRadius: 18).fill(Color.radaInk2))
    }

    private var alertsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Alerts near you").font(.system(size: 13.5, weight: .medium)).foregroundColor(.radaTextPrimary)
                Spacer()
                if alerts.count > Self.collapsedAlertCount {
                    Button {
                        withAnimation(.easeInOut(duration: 0.3)) { showAllAlerts.toggle() }
                    } label: {
                        Text(showAllAlerts ? "Show less" : "See all \(alerts.count)")
                            .font(.system(size: 12, weight: .semibold)).foregroundColor(.radaAccentText)
                    }
                    .buttonStyle(RadaPressStyle())
                }
            }
            ForEach(Array(alerts.enumerated()), id: \.offset) { index, alert in
                if index < Self.collapsedAlertCount || showAllAlerts {
                    AlertRow(alert: alert)
                        .transition(.opacity.combined(with: .move(edge: .top)))
                }
            }
        }
    }
}

private struct AlertRow: View {
    let alert: RadaMockData.RecentAlert

    private var tint: Color {
        switch alert.type {
        case .camera: return .radaAmber
        case .checkpoint: return .radaOrange
        case .alcoblow: return .radaRed
        }
    }
    private var icon: String {
        switch alert.type {
        case .camera: return "camera.fill"
        case .checkpoint: return "checkmark.shield.fill"
        case .alcoblow: return "wineglass.fill"
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            Circle().fill(tint.opacity(0.16)).frame(width: 40, height: 40)
                .overlay(Image(systemName: icon).font(.system(size: 16)).foregroundColor(tint))
            VStack(alignment: .leading, spacing: 2) {
                Text(alert.place).font(.system(size: 14, weight: .medium)).foregroundColor(.radaTextPrimary)
                Text(alert.type.title).font(.system(size: 12)).foregroundColor(.radaTextSecondary)
            }
            Spacer()
            Text(alert.whenLabel).font(.system(size: 12)).foregroundColor(.radaTextMuted)
        }
        .padding(.horizontal, 14).padding(.vertical, 12)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.radaInk2))
    }
}
