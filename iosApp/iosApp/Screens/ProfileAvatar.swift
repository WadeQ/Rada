import SwiftUI

struct ProfileAvatar: View {
    let name: String
    let photo: PlatformImage?
    let size: CGFloat
    var initialsCount: Int = 1

    var body: some View {
        ZStack {
            Circle().fill(Color.radaAccent)
            if let photo {
                Image(platformImage: photo)
                    .resizable()
                    .scaledToFill()
                    .frame(width: size, height: size)
                    .clipShape(Circle())
                    .accessibilityLabel("Profile photo")
            } else {
                Text(initials)
                    .font(.system(size: size * (initialsCount > 1 ? 0.35 : 0.4), weight: .bold))
                    .foregroundColor(.radaAccentInk)
            }
        }
        .frame(width: size, height: size)
    }

    private var initials: String {
        let t = String(name.trimmingCharacters(in: .whitespaces).prefix(initialsCount)).uppercased()
        return t.isEmpty ? "?" : t
    }
}
