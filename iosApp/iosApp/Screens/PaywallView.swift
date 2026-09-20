import SwiftUI

struct PaywallView: View {
    let onTrialStarted: () -> Void

    private let features = [
        "Real-time driver-reported checkpoints",
        "Longer, speed-scaled warning distance",
        "Offline maps for upcountry trips",
        "Full alert history and trends"
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Go Pro").font(.system(size: 19, weight: .semibold)).foregroundColor(.radaTextPrimary)
            Text("One avoided fine pays for over five years.")
                .font(.system(size: 13)).foregroundColor(.radaTextSecondary)
                .padding(.top, 4).padding(.bottom, 18)

            ForEach(RadaMockData.pricingPlans, id: \.name) { plan in
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        if plan.recommended {
                            Text("Best value")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(.radaAccentInk)
                                .padding(.horizontal, 8).padding(.vertical, 2)
                                .background(RoundedRectangle(cornerRadius: 8).fill(Color.radaAccent))
                        }
                        Text(plan.name).font(.system(size: 13.5, weight: .semibold)).foregroundColor(.radaTextPrimary)
                        Text(plan.priceLabel).font(.system(size: 11)).foregroundColor(.radaTextMuted)
                    }
                    Spacer()
                    Text(plan.perMonthLabel).font(.system(size: 15, weight: .bold)).foregroundColor(.radaTextPrimary)
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 18)
                        .fill(plan.recommended ? Color.radaAccent.opacity(0.10) : .clear)
                        .overlay(RoundedRectangle(cornerRadius: 18).stroke(plan.recommended ? Color.radaAccent : Color.radaInk3))
                )
                .padding(.bottom, 10)
            }

            ForEach(features, id: \.self) { feature in
                HStack {
                    Image(systemName: "checkmark").foregroundColor(.radaAccentText)
                    Text(feature).font(.system(size: 12.5)).foregroundColor(.radaTextSecondary)
                }
                .padding(.vertical, 5)
            }

            Spacer()

            SlideToConfirm(label: "Slide to start free trial", doneLabel: "Trial started", onConfirm: onTrialStarted)
            Text("7 days free, then KES 100/mo · cancel anytime")
                .font(.system(size: 11)).foregroundColor(.radaTextMuted)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .padding(.top, 8)

            Button("Restore purchase") {}
                .font(.system(size: 13.5, weight: .semibold))
                .foregroundColor(.radaTextPrimary)
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .overlay(RoundedRectangle(cornerRadius: 9).stroke(Color.radaInk3))
                .padding(.top, 10)
        }
        .padding(.horizontal, 22)
        .padding(.vertical, 30)
    }
}
