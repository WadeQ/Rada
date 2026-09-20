import SwiftUI

@main
struct iosApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .modifier(AppTheme())
        }
    }
}

private struct AppTheme: ViewModifier {
    @AppStorage("themeMode") private var themeMode = ThemeMode.system.rawValue
    func body(content: Content) -> some View {
        content.preferredColorScheme(ThemeMode(rawValue: themeMode)?.colorScheme)
    }
}
