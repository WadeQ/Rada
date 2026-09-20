import SwiftUI

/// Native re-implementation of the onboarding hero illustration, mirroring
/// OnboardingCameraAlertIllustration.kt exactly in content and timing. A GIF would have meant
/// a blurry, unthemeable raster asset, so this redraws the same scene with SwiftUI shapes and
/// animates it with SwiftUI's own animation system instead. Coordinates are defined in the
/// same 380x260 "design units" as the original SVG/Compose versions and scaled via
/// GeometryReader, so all three stay easy to compare against each other.
struct OnboardingCameraAlertIllustration: View {
    @State private var carOffsetX: CGFloat = -190
    @State private var idleBob = false
    @State private var wheelAngle: Double = 0
    @State private var arcPulse = false
    @State private var lensGlow = false
    @State private var popupVisible = false

    var body: some View {
        GeometryReader { geo in
            let scale = geo.size.width / 380
            ZStack(alignment: .topLeading) {
                Color(hex: 0x151A21).frame(height: 170 * scale)
                ForEach([CGPoint(x: 45, y: 35), CGPoint(x: 90, y: 20), CGPoint(x: 330, y: 30), CGPoint(x: 250, y: 45)], id: \.x) { p in
                    Circle().fill(Color(hex: 0x3A4250)).frame(width: 3.2 * scale, height: 3.2 * scale)
                        .position(x: p.x * scale, y: p.y * scale)
                }

                Color(hex: 0x262B33).frame(height: 60 * scale)
                    .position(x: geo.size.width / 2, y: 200 * scale)
                ForEach([20.0, 70.0, 270.0, 320.0], id: \.self) { lx in
                    RoundedRectangle(cornerRadius: 2 * scale).fill(Color(hex: 0x4B5563))
                        .frame(width: 26 * scale, height: 4 * scale)
                        .position(x: (lx + 13) * scale, y: 199 * scale)
                }

                // Camera pole
                Rectangle().fill(Color(hex: 0x4B5563)).frame(width: 6 * scale, height: 60 * scale)
                    .position(x: 301 * scale, y: 138 * scale)
                RoundedRectangle(cornerRadius: 6 * scale).fill(Color(hex: 0x333B46))
                    .frame(width: 44 * scale, height: 30 * scale)
                    .position(x: 302 * scale, y: 107 * scale)
                Circle().stroke(Color(hex: 0xE5484D), lineWidth: 2 * scale)
                    .frame(width: 16 * scale, height: 16 * scale)
                    .opacity(lensGlow ? 0.35 : 1)
                    .position(x: 302 * scale, y: 107 * scale)
                Circle().fill(Color(hex: 0xE5484D)).frame(width: 6 * scale, height: 6 * scale)
                    .position(x: 302 * scale, y: 107 * scale)

                // Radar arcs — dash phase animates via strokeStyle dashPhase to suggest travel.
                radarArc(from: CGPoint(x: 158, y: 168), control: CGPoint(x: 220, y: 150), to: CGPoint(x: 278, y: 118), scale: scale)
                    .opacity(arcPulse ? 0.15 : 0.9)
                radarArc(from: CGPoint(x: 158, y: 178), control: CGPoint(x: 215, y: 168), to: CGPoint(x: 272, y: 132), scale: scale)
                    .opacity(arcPulse ? 0.15 : 0.5)

                // Car — entrance offset + idle bob
                carGroup(scale: scale)
                    .offset(x: carOffsetX * scale, y: idleBob ? -2 * scale : 0)

                // Popup
                popupGroup(scale: scale)
                    .opacity(popupVisible ? 1 : 0)
                    .scaleEffect(popupVisible ? 1 : 0.7)
                    .offset(y: popupVisible ? 0 : 10 * scale)
            }
        }
        .aspectRatio(380.0 / 260.0, contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .onAppear { runAnimations() }
    }

    private func runAnimations() {
        withAnimation(.timingCurve(0.2, 0.8, 0.3, 1, duration: 0.8)) { carOffsetX = 8 }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
            withAnimation(.easeOut(duration: 0.15)) { carOffsetX = -4 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                withAnimation(.easeOut(duration: 0.15)) { carOffsetX = 0 }
            }
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.25) {
            withAnimation(.easeOut(duration: 0.5)) { popupVisible = true }
        }
        withAnimation(.easeInOut(duration: 1.4).repeatForever(autoreverses: true)) { idleBob = true }
        withAnimation(.linear(duration: 0.5).repeatForever(autoreverses: false)) { wheelAngle = 360 }
        withAnimation(.easeOut(duration: 1.6).repeatForever(autoreverses: false)) { arcPulse = true }
        withAnimation(.easeInOut(duration: 1.2).repeatForever(autoreverses: true)) { lensGlow = true }
    }

    private func radarArc(from: CGPoint, control: CGPoint, to: CGPoint, scale: CGFloat) -> some View {
        Path { path in
            path.move(to: CGPoint(x: from.x * scale, y: from.y * scale))
            path.addQuadCurve(to: CGPoint(x: to.x * scale, y: to.y * scale), control: CGPoint(x: control.x * scale, y: control.y * scale))
        }
        .stroke(Color(hex: 0x6FA1F0), style: StrokeStyle(lineWidth: 2 * scale, lineCap: .round, dash: [5 * scale, 5 * scale]))
    }

    private func carGroup(scale: CGFloat) -> some View {
        ZStack {
            RoundedRectangle(cornerRadius: 10 * scale).fill(Color(hex: 0xF2A93B))
                .frame(width: 92 * scale, height: 26 * scale)
                .position(x: 106 * scale, y: 201 * scale)
            RoundedRectangle(cornerRadius: 8 * scale).fill(Color(hex: 0xF2A93B))
                .frame(width: 44 * scale, height: 22 * scale)
                .position(x: 108 * scale, y: 181 * scale)
            RoundedRectangle(cornerRadius: 3 * scale).fill(Color(hex: 0x151A21).opacity(0.35))
                .frame(width: 34 * scale, height: 12 * scale)
                .position(x: 107 * scale, y: 182 * scale)
            ForEach([80.0, 135.0], id: \.self) { wx in
                wheelView(x: wx, scale: scale)
            }
            Circle().fill(Color(hex: 0xFFE9C2)).frame(width: 8 * scale, height: 8 * scale)
                .position(x: 151 * scale, y: 199 * scale)
        }
    }

    private func wheelView(x: Double, scale: CGFloat) -> some View {
        ZStack {
            Circle().fill(Color(hex: 0x151A21)).frame(width: 20 * scale, height: 20 * scale)
            Rectangle().fill(Color(hex: 0x4B5563)).frame(width: 8 * scale, height: 2 * scale)
                .rotationEffect(.degrees(wheelAngle))
        }
        .position(x: x * scale, y: 220 * scale)
    }

    private func popupGroup(scale: CGFloat) -> some View {
        ZStack {
            Path { path in
                path.move(to: CGPoint(x: 98 * scale, y: 142 * scale))
                path.addLine(to: CGPoint(x: 110 * scale, y: 142 * scale))
                path.addLine(to: CGPoint(x: 100 * scale, y: 154 * scale))
                path.closeSubpath()
            }.fill(Color(hex: 0xF3F1EC))

            RoundedRectangle(cornerRadius: 12 * scale).fill(Color(hex: 0xF3F1EC))
                .frame(width: 150 * scale, height: 56 * scale)
                .position(x: 127 * scale, y: 116 * scale)

            RoundedRectangle(cornerRadius: 3 * scale).stroke(Color(hex: 0xE5484D), lineWidth: 2 * scale)
                .frame(width: 20 * scale, height: 16 * scale)
                .position(x: 76 * scale, y: 112 * scale)
            Circle().stroke(Color(hex: 0xE5484D), lineWidth: 2 * scale)
                .frame(width: 8 * scale, height: 8 * scale)
                .position(x: 76 * scale, y: 112 * scale)

            Text("Camera ahead")
                .font(.system(size: 13 * scale, weight: .medium))
                .foregroundColor(Color(hex: 0x12161B))
                .position(x: 145 * scale, y: 106 * scale)
            Text("180 m · limit 50 km/h")
                .font(.system(size: 11 * scale))
                .foregroundColor(Color(hex: 0x6E7684))
                .position(x: 150 * scale, y: 123 * scale)
        }
    }
}
