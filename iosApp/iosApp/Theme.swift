import SwiftUI
#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

/// Mirrors the Android palette (RadaPalette / RadaColors) so both apps read the same.
/// Surfaces and text adapt to light/dark; the brand accent and the type colours are fixed.
///
/// Accent has two roles, same as Android: `radaAccent` (bright lime) is for fills and always
/// carries `radaAccentInk` on top; `radaAccentText` is for text and icons sitting on a surface,
/// which is lime in dark mode and a deeper lime in light mode so it stays legible.
extension Color {
    // Surfaces (light / dark)
    static let radaInk = adaptive(light: 0xF7F6F3, dark: 0x12161B)      // screen background
    static let radaInk2 = adaptive(light: 0xFFFFFF, dark: 0x1C222A)     // cards
    static let radaInk3 = adaptive(light: 0xEDEBE6, dark: 0x262E38)     // raised / dividers

    static let radaTextPrimary = adaptive(light: 0x1B1E22, dark: 0xF3F1EC)
    static let radaTextSecondary = adaptive(light: 0x565C64, dark: 0xA9B0BC)
    static let radaTextMuted = adaptive(light: 0x8A9099, dark: 0x6E7684)

    // Brand accent
    static let radaAccent = Color(hex: 0xCCFF00)
    static let radaAccentInk = Color(hex: 0x10130A)
    static let radaAccentText = adaptive(light: 0x557300, dark: 0xCCFF00)

    // Type / status colours
    static let radaAmber = Color(hex: 0xF2A93B)
    static let radaAmberInk = Color(hex: 0x3A2600)
    static let radaOrange = Color(hex: 0xD97706)
    static let radaRed = Color(hex: 0xE5484D)
    static let radaRedInk = Color(hex: 0x3A0709)

    /// The splash is always ink (the lime Twiga needs a dark ground), whatever the app theme.
    static let radaSplashInk = Color(hex: 0x12161B)

    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }

    /// A colour that resolves differently in light and dark appearance.
    static func adaptive(light: UInt32, dark: UInt32) -> Color {
        #if canImport(UIKit)
        return Color(UIColor { trait in
            trait.userInterfaceStyle == .dark ? UIColor(hex: dark) : UIColor(hex: light)
        })
        #else
        return Color(NSColor(name: nil) { appearance in
            appearance.bestMatch(from: [.darkAqua, .aqua]) == .darkAqua ? NSColor(hex: dark) : NSColor(hex: light)
        })
        #endif
    }
}

#if canImport(UIKit)
private extension UIColor {
    convenience init(hex: UInt32) {
        self.init(red: CGFloat((hex >> 16) & 0xFF) / 255, green: CGFloat((hex >> 8) & 0xFF) / 255,
                  blue: CGFloat(hex & 0xFF) / 255, alpha: 1)
    }
}
#else
private extension NSColor {
    convenience init(hex: UInt32) {
        self.init(srgbRed: CGFloat((hex >> 16) & 0xFF) / 255, green: CGFloat((hex >> 8) & 0xFF) / 255,
                  blue: CGFloat(hex & 0xFF) / 255, alpha: 1)
    }
}
#endif
