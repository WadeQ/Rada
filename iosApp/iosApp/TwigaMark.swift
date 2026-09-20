import SwiftUI

/// The Twiga launcher logo, drawn from the same coordinates as the Android vector
/// (`ic_launcher_foreground.xml`) on a 108-unit canvas: a giraffe whose eye is a camera lens.
/// Spots and the lens ring are true holes, so it works over any background.
struct TwigaMark: View {
    var lime: Color = .radaAccent
    var amber: Color = .radaAmber
    /// Colour of the lens pupil; the splash and icon ground.
    var pupil: Color = .radaSplashInk

    var body: some View {
        Canvas { ctx, size in
            let s = min(size.width, size.height) / 108
            // Same group transform as Android: scale 0.9 about (56, 54), shifted 2 left.
            let t = CGAffineTransform(translationX: -56, y: -54)
                .concatenating(CGAffineTransform(scaleX: 0.9, y: 0.9))
                .concatenating(CGAffineTransform(translationX: 54, y: 54))
                .concatenating(CGAffineTransform(scaleX: s, y: s))
            let k = 0.9 * s   // uniform scale factor, for stroke widths

            func circle(_ cx: CGFloat, _ cy: CGFloat, _ r: CGFloat) -> Path {
                Path(ellipseIn: CGRect(x: cx - r, y: cy - r, width: 2 * r, height: 2 * r))
            }
            func rotated(_ p: Path, degrees: CGFloat, about c: CGPoint) -> Path {
                let a = CGAffineTransform(translationX: -c.x, y: -c.y)
                    .concatenating(CGAffineTransform(rotationAngle: degrees * .pi / 180))
                    .concatenating(CGAffineTransform(translationX: c.x, y: c.y))
                return p.applying(a)
            }

            // Neck and chest, with four spots punched out
            var neck = Path()
            neck.move(to: CGPoint(x: 34, y: 86))
            neck.addCurve(to: CGPoint(x: 48, y: 54), control1: CGPoint(x: 34, y: 72), control2: CGPoint(x: 44, y: 66))
            neck.addLine(to: CGPoint(x: 52, y: 38))
            neck.addLine(to: CGPoint(x: 62, y: 41))
            neck.addCurve(to: CGPoint(x: 60, y: 72), control1: CGPoint(x: 59, y: 52), control2: CGPoint(x: 57, y: 62))
            neck.addCurve(to: CGPoint(x: 62, y: 86), control1: CGPoint(x: 62, y: 78), control2: CGPoint(x: 62, y: 82))
            neck.closeSubpath()
            for (x, y, r) in [(49.0, 62.0, 3.0), (54.0, 75.0, 3.0), (55.0, 50.0, 2.4), (43.0, 77.0, 2.6)] {
                neck.addPath(circle(x, y, r))
            }
            ctx.fill(neck.applying(t), with: .color(lime), style: FillStyle(eoFill: true))

            // Ossicones
            var horns = Path()
            horns.move(to: CGPoint(x: 57, y: 29)); horns.addLine(to: CGPoint(x: 55, y: 20))
            horns.move(to: CGPoint(x: 63, y: 28)); horns.addLine(to: CGPoint(x: 65, y: 19))
            ctx.stroke(horns.applying(t), with: .color(lime), style: StrokeStyle(lineWidth: 3 * k, lineCap: .round))
            ctx.fill(circle(55, 19.5, 2.9).applying(t), with: .color(amber))
            ctx.fill(circle(65, 18.5, 2.9).applying(t), with: .color(amber))

            // Ear
            let ear = rotated(Path(ellipseIn: CGRect(x: 52.5 - 4.8, y: 31.5 - 2.4, width: 9.6, height: 4.8)),
                              degrees: -35, about: CGPoint(x: 52.5, y: 31.5))
            ctx.fill(ear.applying(t), with: .color(lime))

            // Head, with the lens ring cut out of it
            var head = rotated(Path(ellipseIn: CGRect(x: 65 - 14.5, y: 33 - 8, width: 29, height: 16)),
                               degrees: 14, about: CGPoint(x: 65, y: 33))
            head.addPath(circle(68.5, 32.5, 5.3))
            ctx.fill(head.applying(t), with: .color(lime), style: FillStyle(eoFill: true))

            // Lens: amber glass, pupil, glint
            ctx.fill(circle(68.5, 32.5, 3.2).applying(t), with: .color(amber))
            ctx.fill(circle(68.5, 32.5, 1.4).applying(t), with: .color(pupil))
            ctx.fill(circle(69.6, 31.3, 0.9).applying(t), with: .color(lime))
        }
        .aspectRatio(1, contentMode: .fit)
        .accessibilityLabel("Rada")
    }
}
