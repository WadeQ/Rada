import SwiftUI
import PhotosUI

struct SettingsView: View {
    @EnvironmentObject private var profile: ProfileStore
    let onLogOut: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Text("Settings")
                .font(.system(size: 18, weight: .semibold)).foregroundColor(.radaTextPrimary)
                .padding(.top, 24).padding(.bottom, 12)

            ScrollView(showsIndicators: false) { SettingsBody() }

            Button {
                profile.signOut()
                onLogOut()
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "rectangle.portrait.and.arrow.right").font(.system(size: 16))
                    Text("Log out").font(.system(size: 15, weight: .semibold))
                }
                .foregroundColor(.radaAccentInk)
                .frame(maxWidth: .infinity).frame(height: 54)
                .background(Capsule().fill(Color.radaAccent))
            }
            .buttonStyle(RadaPressStyle(scale: 0.98))
            .padding(.horizontal, 16)

            Spacer().frame(height: bottomBarClearance)
        }
    }
}

struct SettingsBody: View {
    @EnvironmentObject private var profile: ProfileStore
    @Environment(\.colorScheme) private var colorScheme

    @State private var nameInput: String
    @State private var nameError: String?
    @State private var saveTask: Task<Void, Never>?
    @State private var pickerItem: PhotosPickerItem?

    init(initialName: String = "") { _nameInput = State(initialValue: initialName) }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            photoHeader
            nameGroup.padding(.top, 20)
            groupLabel("Preferences").padding(.top, 24)
            preferences
            Spacer().frame(height: 16)
        }
        .padding(.horizontal, 16)
        .onAppear { if nameInput.isEmpty { nameInput = profile.name } }
        .onChange(of: pickerItem) { item in
            guard let item else { return }
            Task {
                if let data = try? await item.loadTransferable(type: Data.self) { profile.setPhoto(from: data) }
                pickerItem = nil
            }
        }
    }

    private var photoHeader: some View {
        VStack(spacing: 10) {
            PhotosPicker(selection: $pickerItem, matching: .images) {
                ZStack(alignment: .bottomTrailing) {
                    ProfileAvatar(name: nameInput.isEmpty ? profile.displayName : nameInput,
                                  photo: profile.photo, size: 104)
                    Circle().fill(Color.radaAccent).frame(width: 34, height: 34)
                        .overlay(Image(systemName: "camera").font(.system(size: 16)).foregroundColor(.radaAccentInk))
                        .overlay(Circle().stroke(Color.radaInk, lineWidth: 3))
                }
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Change photo")

            if profile.photo != nil {
                Button("Remove photo") { profile.removePhoto() }
                    .buttonStyle(RadaPressStyle())
                    .font(.system(size: 13)).foregroundColor(.radaTextSecondary)
            }
        }
        .frame(maxWidth: .infinity)
    }

    private var nameGroup: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Full name").font(.system(size: 14)).foregroundColor(.radaTextSecondary)
                TextField("Your name", text: $nameInput)
                    .multilineTextAlignment(.trailing)
                    .font(.system(size: 15))
                    .foregroundColor(.radaTextPrimary)
                    .onChange(of: nameInput) { scheduleSave($0) }
            }
            .padding(.horizontal, 16).frame(height: 56)
            .background(RoundedRectangle(cornerRadius: 22).fill(Color.radaInk2))

            if let nameError {
                Text(nameError).font(.system(size: 12)).foregroundColor(.radaRed).padding(.leading, 8)
            }
        }
    }

    private func scheduleSave(_ value: String) {
        saveTask?.cancel()
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty { nameError = "Enter a name"; return }
        nameError = nil
        saveTask = Task { @MainActor in
            try? await Task.sleep(nanoseconds: 600_000_000)
            guard !Task.isCancelled else { return }
            if trimmed != profile.name { profile.name = trimmed }
        }
    }

    private var darkOn: Bool {
        switch profile.themeMode {
        case .dark: return true
        case .light: return false
        case .system: return colorScheme == .dark
        }
    }

    private var preferences: some View {
        VStack(spacing: 0) {
            toggleRow(icon: "bell", title: "Alert notifications", subtitle: "Camera and checkpoint warnings",
                      isOn: Binding(get: { profile.alertsEnabled }, set: { profile.alertsEnabled = $0 }))
            Divider().padding(.leading, 52).opacity(0.5)
            toggleRow(icon: "moon", title: "Dark mode", subtitle: nil,
                      isOn: Binding(get: { darkOn }, set: { profile.themeMode = $0 ? .dark : .light }))
        }
        .background(RoundedRectangle(cornerRadius: 22).fill(Color.radaInk2))
    }

    private func toggleRow(icon: String, title: String, subtitle: String?, isOn: Binding<Bool>) -> some View {
        HStack(spacing: 16) {
            Image(systemName: icon).font(.system(size: 18)).foregroundColor(.radaTextSecondary).frame(width: 22)
            VStack(alignment: .leading, spacing: 1) {
                Text(title).font(.system(size: 15)).foregroundColor(.radaTextPrimary)
                if let subtitle { Text(subtitle).font(.system(size: 12)).foregroundColor(.radaTextSecondary) }
            }
            Spacer()
            Toggle("", isOn: isOn).labelsHidden().tint(.radaAccent)
        }
        .padding(.leading, 16).padding(.trailing, 12).padding(.vertical, 10)
    }

    private func groupLabel(_ text: String) -> some View {
        Text(text).font(.system(size: 13, weight: .medium)).foregroundColor(.radaTextSecondary)
            .padding(.leading, 8).padding(.bottom, 8)
    }
}
