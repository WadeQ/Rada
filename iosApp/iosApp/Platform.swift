import SwiftUI
#if canImport(UIKit)
import UIKit
typealias PlatformImage = UIImage
#elseif canImport(AppKit)
import AppKit
typealias PlatformImage = NSImage
#endif

/// The few places the app touches UIKit, kept behind one file so everything else stays plain
/// SwiftUI (and so the views can also be type-checked and rendered on macOS).
extension Image {
    init(platformImage: PlatformImage) {
        #if canImport(UIKit)
        self.init(uiImage: platformImage)
        #else
        self.init(nsImage: platformImage)
        #endif
    }
}

enum Haptics {
    /// A light tick when something is pressed.
    static func tick() {
        #if canImport(UIKit) && !os(macOS)
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
        #endif
    }
    /// A firmer confirmation when something succeeds.
    static func success() {
        #if canImport(UIKit) && !os(macOS)
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        #endif
    }
    /// A rejection buzz when something fails.
    static func error() {
        #if canImport(UIKit) && !os(macOS)
        UINotificationFeedbackGenerator().notificationOccurred(.error)
        #endif
    }
}

func openURLExternally(_ url: URL) {
    #if canImport(UIKit)
    UIApplication.shared.open(url)
    #else
    NSWorkspace.shared.open(url)
    #endif
}

extension View {
    /// Phone-pad keyboard on iOS; a no-op elsewhere.
    @ViewBuilder func phoneKeyboard() -> some View {
        #if canImport(UIKit)
        self.keyboardType(.phonePad)
        #else
        self
        #endif
    }
}

/// Press feedback used by the app's custom buttons: a small springy shrink.
struct RadaPressStyle: ButtonStyle {
    var scale: CGFloat = 0.96
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? scale : 1)
            .animation(.spring(response: 0.25, dampingFraction: 0.55), value: configuration.isPressed)
    }
}
