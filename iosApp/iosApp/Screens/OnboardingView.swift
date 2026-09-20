import SwiftUI

private struct ObStep {
    let icon: String
    let tint: Color
    let title: String
    let body: String
}

private let obSteps = [
    ObStep(icon: "camera.fill", tint: .radaAmber, title: "Cameras, before they see you",
           body: "Fixed NTSA points and driver-reported checkpoints, both flagged before you reach them."),
    ObStep(icon: "checkmark.shield.fill", tint: .radaAccentText, title: "Your data stays on your phone",
           body: "Location is checked against nearby cameras on-device. No trip history is uploaded."),
    ObStep(icon: "location.fill", tint: .radaAmber, title: "Location, even when closed",
           body: "Alerts fire while Rada runs in the background — this needs location access to keep working.")
]

struct OnboardingView: View {
    let onDone: () -> Void
    @State private var step = 0

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 6) {
                ForEach(0..<3, id: \.self) { i in
                    RoundedRectangle(cornerRadius: 2)
                        .fill(i <= step ? Color.radaAccent : Color.radaInk3)
                        .frame(height: 4)
                }
            }
            .padding(.top, 16)

            Spacer()
            let current = obSteps[step]
            if step == 0 {
                // Step 1 gets the fuller illustration — highest-value screen in onboarding,
                // needs the space a full scene gives it. Steps 2/3 stay as simple icons since
                // they're abstract concepts (privacy, permissions), not a literal scene.
                OnboardingCameraAlertIllustration()
                    .frame(maxWidth: 260)
                    .padding(.bottom, 4)
            } else {
                Circle()
                    .fill(current.tint.opacity(0.14))
                    .frame(width: 64, height: 64)
                    .overlay(Image(systemName: current.icon).foregroundColor(current.tint))
            }
            Text(current.title)
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(.radaTextPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 16)
            Text(current.body)
                .font(.system(size: 13.5))
                .foregroundColor(.radaTextSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 8)
                .padding(.horizontal, 12)

            if step == 2 {
                Text("You can turn this off anytime in Settings — alerts just won't work while the app is closed.")
                    .font(.system(size: 11.5))
                    .foregroundColor(.radaTextMuted)
                    .padding(12)
                    .background(RoundedRectangle(cornerRadius: 14).fill(Color.radaInk2))
                    .padding(.top, 14)
            }
            Spacer()

            HStack(spacing: 10) {
                if step > 0 {
                    Button("Back") { step -= 1 }
                        .foregroundColor(.radaTextSecondary)
                        .frame(height: 48)
                        .padding(.horizontal, 18)
                        .overlay(RoundedRectangle(cornerRadius: 9).stroke(Color.radaInk3))
                }
                Button(step == 2 ? "Allow location access" : "Continue") {
                    if step < 2 { step += 1 } else { onDone() }
                }
                .font(.system(size: 14.5, weight: .semibold))
                .foregroundColor(.radaAccentInk)
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(RoundedRectangle(cornerRadius: 9).fill(Color.radaAccent))
            }
        }
        .padding(24)
    }
}
