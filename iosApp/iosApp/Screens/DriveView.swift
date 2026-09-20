import SwiftUI

struct DriveView: View {
    let isPro: Bool
    let onUnlockTapped: () -> Void

    @State private var distanceMeters = 180
    @State private var etaSeconds = 400
    let timer = Timer.publish(every: 0.7, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Image(systemName: "camera.fill").foregroundColor(.radaAmberInk)
                VStack(alignment: .leading) {
                    Text("Speed camera ahead").font(.system(size: 13, weight: .bold)).foregroundColor(.radaAmberInk)
                    Text("\(distanceMeters) m · limit 50 km/h").font(.system(size: 11)).foregroundColor(.radaAmberInk.opacity(0.75))
                }
                Spacer()
            }
            .padding(.horizontal, 16).padding(.vertical, 12)
            .background(Color.radaAmber)

            HStack(spacing: 40) {
                VStack {
                    Text("67").font(.system(size: 38, weight: .bold)).foregroundColor(.radaRed)
                    Text("km/h current").font(.system(size: 10.5)).foregroundColor(.radaTextMuted)
                }
                VStack {
                    Circle().fill(Color.radaInk2).frame(width: 48, height: 48)
                        .overlay(Circle().stroke(Color.radaRed, lineWidth: 3))
                        .overlay(Text("50").font(.system(size: 16, weight: .bold)).foregroundColor(.radaTextPrimary))
                    Text("limit ahead").font(.system(size: 10.5)).foregroundColor(.radaTextMuted)
                }
            }
            .padding(.vertical, 12)

            HStack {
                VStack(alignment: .leading) {
                    Text("Arriving").font(.system(size: 10.5)).foregroundColor(.radaTextMuted)
                    Text(formatEta(etaSeconds)).font(.system(size: 20, weight: .bold)).foregroundColor(.radaTextPrimary)
                }
                Spacer()
                VStack(alignment: .trailing) {
                    Text("3").font(.system(size: 15, weight: .semibold)).foregroundColor(.radaTextPrimary)
                    Text("cameras on route").font(.system(size: 10)).foregroundColor(.radaTextMuted)
                }
            }
            .padding(12)
            .background(RoundedRectangle(cornerRadius: 14).fill(Color.radaInk2))
            .padding(.horizontal, 18)

            RoundedRectangle(cornerRadius: 14)
                .fill(Color.radaInk2)
                .padding(.horizontal, 18)
                .padding(.vertical, 10)

            Button(action: { if !isPro { onUnlockTapped() } }) {
                HStack {
                    Image(systemName: "checkmark.shield.fill")
                        .foregroundColor(isPro ? .radaRed : .radaTextMuted)
                    Text(isPro ? "Alcoblow · 900 m ahead · alt route via Ngong Rd" : "Alcohol check reported 900 m ahead")
                        .font(.system(size: 11.5))
                        .foregroundColor(.radaTextSecondary)
                        .blur(radius: isPro ? 0 : 3.5)
                    Spacer()
                    Text(isPro ? "View route" : "Unlock")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.radaAccentText)
                }
                .padding(12)
                .background(RoundedRectangle(cornerRadius: 14).fill(Color.radaInk2))
                .padding(.horizontal, 18)
            }

            Spacer().frame(height: bottomBarClearance)
        }
        .onReceive(timer) { _ in
            distanceMeters -= 12
            if distanceMeters <= 20 { distanceMeters = 180 }
            etaSeconds -= 7
            if etaSeconds <= 0 { etaSeconds = 400 }
        }
    }

    private func formatEta(_ totalSeconds: Int) -> String {
        let m = totalSeconds / 60
        let s = totalSeconds % 60
        return "\(m):\(String(format: "%02d", s))"
    }
}
