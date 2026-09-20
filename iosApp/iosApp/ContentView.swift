import SwiftUI

enum RadaScreen {
    case splash, onboarding, login, dashboard, drive, report, help, settings, paywall

    var tab: RadaTab? {
        switch self {
        case .dashboard: return .home
        case .drive: return .drive
        case .help: return .help
        case .settings: return .settings
        default: return nil
        }
    }
}

struct ContentView: View {
    @StateObject private var profile = ProfileStore()
    @State private var current: RadaScreen = .splash
    @State private var isPro: Bool = false

    var body: some View {
        ZStack {
            (current == .splash ? Color.radaSplashInk : Color.radaInk).ignoresSafeArea()

            VStack(spacing: 0) {
                screen
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

                if let tab = current.tab {
                    RadaTabBar(selected: tab) { go(to: $0) }
                }
            }
        }
        .environmentObject(profile)
    }

    @ViewBuilder private var screen: some View {
        switch current {
        case .splash:
            SplashView { current = .onboarding }
        case .onboarding:
            OnboardingView { current = .login }
        case .login:
            LoginView { current = .dashboard }
        case .dashboard:
            DashboardView(isPro: isPro, onUpgrade: { current = .paywall })
        case .drive:
            DriveView(isPro: isPro, onUnlockTapped: { current = .paywall })
        case .report:
            ReportView { current = .drive }
        case .help:
            HelpView()
        case .settings:
            SettingsView { current = .login }
        case .paywall:
            PaywallView {
                isPro = true
                current = .dashboard
            }
        }
    }

    private func go(to tab: RadaTab) {
        switch tab {
        case .home: current = .dashboard
        case .drive: current = .drive
        case .report: current = .report
        case .help: current = .help
        case .settings: current = .settings
        }
    }
}
