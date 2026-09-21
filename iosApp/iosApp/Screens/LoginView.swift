import SwiftUI

struct LoginView: View {
    let onSignedIn: () -> Void
    @State private var phone: String = ""

    var body: some View {
        VStack(spacing: 0) {
            VStack(spacing: 12) {
                OnboardingCameraAlertIllustration()
                    .frame(maxWidth: 220)
                Text("Rada").font(.system(size: 20, weight: .semibold)).foregroundColor(.radaTextPrimary)
                Text("kaa rada na ntsa").font(.system(size: 12.5)).foregroundColor(.radaTextSecondary)
            }
            .frame(maxWidth: .infinity)
            .frame(maxHeight: .infinity)
            .layoutPriority(1.1)

            VStack(alignment: .leading, spacing: 4) {
                Text("Welcome back").font(.system(size: 16, weight: .semibold)).foregroundColor(.radaTextPrimary)
                Text("Sign in to sync saved routes and alerts.").font(.system(size: 12.5)).foregroundColor(.radaTextSecondary)

                HStack {
                    Text("+254").foregroundColor(.radaTextSecondary)
                    TextField("712 345 678", text: $phone)
                        .phoneKeyboard()
                        .onChange(of: phone) { value in
                            var digits = value.filter(\.isNumber)
                            if digits.hasPrefix("254") { digits.removeFirst(3) }
                            if digits.hasPrefix("0") { digits.removeFirst() }
                            let trimmed = String(digits.prefix(9))
                            if trimmed != value { phone = trimmed }
                        }
                        .foregroundColor(.radaTextPrimary)
                        .padding(.horizontal, 12)
                        .frame(height: 46)
                        .background(RoundedRectangle(cornerRadius: 9).fill(Color.radaInk2))
                }
                .padding(.top, 16)

                SlideToConfirm(label: "Slide to sign in", doneLabel: "Signed in", onConfirm: onSignedIn)
                    .padding(.top, 16)

                HStack(spacing: 10) {
                    socialButton("Google")
                    socialButton("Apple")
                }
                .padding(.top, 16)

                Text("By continuing you agree to the terms and privacy policy.")
                    .font(.system(size: 10.5))
                    .foregroundColor(.radaTextMuted)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 14)
            }
        }
        .padding(.horizontal, 22)
        .padding(.bottom, 22)
    }

    private func socialButton(_ title: String) -> some View {
        Text(title)
            .font(.system(size: 13))
            .foregroundColor(.radaTextPrimary)
            .frame(maxWidth: .infinity)
            .frame(height: 44)
            .overlay(RoundedRectangle(cornerRadius: 9).stroke(Color.radaInk3))
    }
}
