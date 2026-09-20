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

/// Applies the user's chosen appearance to the whole app. `ProfileStore` writes the same
/// UserDefaults key, so flipping Dark mode in Settings takes effect immediately.
/// nil follows the phone until the user makes an explicit choice.
private struct AppTheme: ViewModifier {
    @AppStorage("themeMode") private var themeMode = ThemeMode.system.rawValue
    func body(content: Content) -> some View {
        content.preferredColorScheme(ThemeMode(rawValue: themeMode)?.colorScheme)
    }
}
