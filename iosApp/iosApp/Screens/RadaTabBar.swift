import SwiftUI

enum RadaTab: CaseIterable {
    case home, drive, report, help, settings

    var label: String {
        switch self {
        case .home: return "Home"
        case .drive: return "Drive"
        case .report: return "Report"
        case .help: return "Help"
        case .settings: return "Settings"
        }
    }

    var systemImage: String {
        switch self {
        case .home: return "house.fill"
        case .drive: return "location.north.fill"
        case .report: return "exclamationmark.octagon.fill"
        case .help: return "shield.fill"
        case .settings: return "gearshape.fill"
        }
    }
}

/// Space to leave beneath a screen's last content so the active circle, which rises above the
/// bar, never covers it.
let bottomBarClearance: CGFloat = 34

/// Dark floating bar with a concave notch under the active tab; the active icon sits in a lime
/// circle that slides between tabs. One instance lives under the screen switcher so the circle
/// can animate across destinations. Same geometry as Android's RadaBottomBar.
struct RadaTabBar: View {
    let selected: RadaTab
    let onSelect: (RadaTab) -> Void

    private let barHeight: CGFloat = 64
    private let overshoot: CGFloat = 30
    private let circleSize: CGFloat = 52
    private let sidePadding: CGFloat = 28

    private let barColor = Color.adaptive(light: 0x14181D, dark: 0x262E38)

    var body: some View {
        GeometryReader { geo in
            let tabs = RadaTab.allCases
            let slotWidth = (geo.size.width - sidePadding * 2) / CGFloat(tabs.count)
            let index = CGFloat(tabs.firstIndex(of: selected) ?? 0)
            let center = sidePadding + slotWidth * (index + 0.5)

            ZStack(alignment: .topLeading) {
                NotchedBarShape(centerX: center)
                    .fill(barColor)
                    .frame(height: barHeight)

                HStack(spacing: 0) {
                    ForEach(tabs, id: \.self) { tab in
                        Button {
                            if tab != selected { Haptics.tick(); onSelect(tab) }
                        } label: {
                            ZStack {
                                if tab == selected {
                                    Text(tab.label)
                                        .font(.system(size: 11, weight: .medium))
                                        .foregroundColor(.white)
                                        .frame(maxHeight: .infinity, alignment: .bottom)
                                        .padding(.bottom, 8)
                                } else {
                                    Image(systemName: tab.systemImage)
                                        .font(.system(size: 20))
                                        .foregroundColor(.white.opacity(0.72))
                                }
                            }
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(tab.label)
                        .accessibilityAddTraits(tab == selected ? .isSelected : [])
                    }
                }
                .padding(.horizontal, sidePadding)
                .frame(height: barHeight)

                // Active circle, rising out of the notch
                Circle()
                    .fill(Color.radaAccent)
                    .frame(width: circleSize, height: circleSize)
                    .overlay(
                        Image(systemName: selected.systemImage)
                            .font(.system(size: 22))
                            .foregroundColor(.radaAccentInk)
                            .id(selected)
                            .transition(.opacity)
                    )
                    .offset(x: center - circleSize / 2, y: -(overshoot - 3))
                    .allowsHitTesting(false)
            }
            .animation(.spring(response: 0.42, dampingFraction: 0.72), value: selected)
        }
        .frame(height: barHeight)
        .padding(.horizontal, 12)
        .padding(.bottom, 8)
    }
}

/// Rounded rectangle with a smooth U-shaped dip in the top edge centred on `centerX`.
struct NotchedBarShape: Shape {
    var centerX: CGFloat
    private let corner: CGFloat = 22
    private let halfWidth: CGFloat = 38
    private let depth: CGFloat = 30

    var animatableData: CGFloat {
        get { centerX }
        set { centerX = newValue }
    }

    func path(in rect: CGRect) -> Path {
        let w = rect.width, h = rect.height
        let start = max(centerX - halfWidth, corner)
        let end = min(centerX + halfWidth, w - corner)
        var p = Path()
        p.move(to: CGPoint(x: 0, y: corner))
        p.addQuadCurve(to: CGPoint(x: corner, y: 0), control: CGPoint(x: 0, y: 0))
        p.addLine(to: CGPoint(x: start, y: 0))
        p.addCurve(to: CGPoint(x: centerX, y: depth),
                   control1: CGPoint(x: centerX - halfWidth * 0.55, y: 0),
                   control2: CGPoint(x: centerX - halfWidth * 0.62, y: depth))
        p.addCurve(to: CGPoint(x: end, y: 0),
                   control1: CGPoint(x: centerX + halfWidth * 0.62, y: depth),
                   control2: CGPoint(x: centerX + halfWidth * 0.55, y: 0))
        p.addLine(to: CGPoint(x: w - corner, y: 0))
        p.addQuadCurve(to: CGPoint(x: w, y: corner), control: CGPoint(x: w, y: 0))
        p.addLine(to: CGPoint(x: w, y: h - corner))
        p.addQuadCurve(to: CGPoint(x: w - corner, y: h), control: CGPoint(x: w, y: h))
        p.addLine(to: CGPoint(x: corner, y: h))
        p.addQuadCurve(to: CGPoint(x: 0, y: h - corner), control: CGPoint(x: 0, y: h))
        p.closeSubpath()
        return p
    }
}
