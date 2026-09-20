import SwiftUI
import ImageIO
import UniformTypeIdentifiers

enum ThemeMode: String, CaseIterable {
    case system, light, dark

    /// nil means "follow the phone".
    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }
}

/// Local persistence for everything Android keeps in DataStore: the name, profile photo,
/// alert and theme preferences, and the log of reports submitted from this device. Every
/// change is written immediately; there is no save step. Cleared on sign-out (except the two
/// preferences) so the next person on the phone doesn't inherit the profile.
///
/// iOS has no backend yet, so report history is purely local, same as Android's counter.
final class ProfileStore: ObservableObject {
    private enum Key {
        static let name = "userName", alerts = "alertsEnabled", theme = "themeMode"
        static let photoFile = "photoFile", reports = "reportTimes"
    }

    private let defaults: UserDefaults
    private let photoDirectory: URL

    @Published var name: String { didSet { defaults.set(name, forKey: Key.name) } }
    @Published var alertsEnabled: Bool { didSet { defaults.set(alertsEnabled, forKey: Key.alerts) } }
    @Published var themeMode: ThemeMode { didSet { defaults.set(themeMode.rawValue, forKey: Key.theme) } }
    @Published private(set) var photo: PlatformImage?
    @Published private(set) var reportTimes: [TimeInterval]

    init(defaults: UserDefaults = .standard, photoDirectory: URL? = nil) {
        self.defaults = defaults
        self.photoDirectory = photoDirectory
            ?? FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
                .appendingPathComponent("profile", isDirectory: true)
        name = defaults.string(forKey: Key.name) ?? ""
        alertsEnabled = defaults.object(forKey: Key.alerts) as? Bool ?? true
        themeMode = ThemeMode(rawValue: defaults.string(forKey: Key.theme) ?? "") ?? .system
        reportTimes = (defaults.array(forKey: Key.reports) as? [TimeInterval]) ?? []
        photo = nil
        if let file = defaults.string(forKey: Key.photoFile) {
            photo = PlatformImage(contentsOfFile: self.photoDirectory.appendingPathComponent(file).path)
        }
    }

    /// Display name, falling back the way the Android home screen does.
    var displayName: String { name.trimmingCharacters(in: .whitespaces).isEmpty ? "Driver" : name }

    var reportsThisWeek: Int {
        let cutoff = Date().timeIntervalSince1970 - 7 * 24 * 3600
        return reportTimes.filter { $0 >= cutoff }.count
    }

    // MARK: Reports

    @discardableResult
    func recordReport() -> TimeInterval {
        let now = Date().timeIntervalSince1970
        let cutoff = now - 7 * 24 * 3600
        reportTimes = reportTimes.filter { $0 >= cutoff } + [now]
        defaults.set(reportTimes, forKey: Key.reports)
        return now
    }

    func removeReport(_ timestamp: TimeInterval) {
        reportTimes.removeAll { $0 == timestamp }
        defaults.set(reportTimes, forKey: Key.reports)
    }

    // MARK: Photo

    /// Square-crops and downsizes the picked image, then keeps an app-private copy: picker
    /// results are only temporarily readable. The file name changes each time so caches see it.
    func setPhoto(from data: Data) {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            guard let self, let jpeg = Self.squareJPEG(from: data) else { return }
            let name = "photo_\(Int(Date().timeIntervalSince1970)).jpg"
            try? FileManager.default.createDirectory(at: self.photoDirectory, withIntermediateDirectories: true)
            self.clearPhotoFiles()
            let url = self.photoDirectory.appendingPathComponent(name)
            guard (try? jpeg.write(to: url)) != nil else { return }
            let image = PlatformImage(contentsOfFile: url.path)
            DispatchQueue.main.async {
                self.defaults.set(name, forKey: Key.photoFile)
                self.photo = image
            }
        }
    }

    func removePhoto() {
        clearPhotoFiles()
        defaults.removeObject(forKey: Key.photoFile)
        photo = nil
    }

    private func clearPhotoFiles() {
        (try? FileManager.default.contentsOfDirectory(at: photoDirectory, includingPropertiesForKeys: nil))?
            .forEach { try? FileManager.default.removeItem(at: $0) }
    }

    private static func squareJPEG(from data: Data) -> Data? {
        guard let source = CGImageSourceCreateWithData(data as CFData, nil) else { return nil }
        let options: [CFString: Any] = [
            kCGImageSourceCreateThumbnailFromImageAlways: true,
            kCGImageSourceCreateThumbnailWithTransform: true,   // respects EXIF orientation
            kCGImageSourceThumbnailMaxPixelSize: 640
        ]
        guard let scaled = CGImageSourceCreateThumbnailAtIndex(source, 0, options as CFDictionary) else { return nil }
        let side = min(scaled.width, scaled.height)
        let crop = CGRect(x: (scaled.width - side) / 2, y: (scaled.height - side) / 2, width: side, height: side)
        guard let square = scaled.cropping(to: crop) else { return nil }
        let out = NSMutableData()
        guard let dest = CGImageDestinationCreateWithData(out, UTType.jpeg.identifier as CFString, 1, nil) else { return nil }
        CGImageDestinationAddImage(dest, square, [kCGImageDestinationLossyCompressionQuality: 0.9] as CFDictionary)
        return CGImageDestinationFinalize(dest) ? out as Data : nil
    }

    // MARK: Sign-out

    func signOut() {
        name = ""
        removePhoto()
        reportTimes = []
        defaults.removeObject(forKey: Key.reports)
    }
}
