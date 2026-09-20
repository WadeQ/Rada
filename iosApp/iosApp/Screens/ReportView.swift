import SwiftUI

/// One-tap reporting: all three types on screen, one tap reports. On success the tapped tile
/// floods with its colour from the icon, ripples outward and draws a checkmark, and an Undo chip
/// with a draining ring stays for five seconds. Reports are only logged locally here (there is
/// no iOS backend yet), which is what feeds the "this week" counter.
struct ReportView: View {
    @EnvironmentObject private var profile: ProfileStore
    let onDone: () -> Void

    private struct Success { let kind: ReportKind; let start: Date }
    private struct Undoable { let kind: ReportKind; let token: TimeInterval }

    @State private var submitting: ReportKind?
    @State private var success: Success?
    @State private var undoable: Undoable?
    @State private var remaining: CGFloat = 1
    @State private var appeared = false
    @State private var pop: CGFloat = 1
    @State private var undoTask: Task<Void, Never>?

    private static let undoWindow: Double = 5

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 0) {
                    Text("Report")
                        .font(.system(size: 30, weight: .heavy)).tracking(-0.6)
                        .foregroundColor(.radaTextPrimary)
                    Text("One tap. We use your location.")
                        .font(.system(size: 13)).foregroundColor(.radaTextSecondary)
                }
                Spacer()
                impactChip
            }
            .padding(.top, 20)

            VStack(spacing: 12) {
                ForEach(Array(ReportKind.allCases.enumerated()), id: \.element) { index, kind in
                    ReportTile(
                        kind: kind,
                        isSubmitting: submitting == kind,
                        busy: submitting != nil,
                        successStart: success?.kind == kind ? success?.start : nil,
                        onTap: { report(kind) }
                    )
                    .offset(y: appeared ? 0 : 48)
                    .opacity(appeared ? 1 : 0)
                    .animation(.easeOut(duration: 0.45).delay(Double(index) * 0.07), value: appeared)
                }
            }
            .padding(.top, 24)

            Spacer()

            if let undoable {
                undoChip(undoable)
                    .padding(.bottom, 12)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }

            Button(action: onDone) {
                Text("Done reporting")
                    .font(.system(size: 15, weight: .semibold)).foregroundColor(.radaTextPrimary)
                    .frame(maxWidth: .infinity).frame(height: 54)
                    .background(Capsule().fill(Color.radaInk2))
            }
            .buttonStyle(RadaPressStyle(scale: 0.98))
            .padding(.bottom, 16)
        }
        .padding(.horizontal, 20)
        .onAppear { appeared = true }
        .onChange(of: profile.reportsThisWeek) { _ in
            pop = 1.3
            withAnimation(.spring(response: 0.35, dampingFraction: 0.5)) { pop = 1 }
        }
    }

    // MARK: Flow

    private func report(_ kind: ReportKind) {
        guard submitting == nil else { return }
        Haptics.tick()
        undoTask?.cancel()
        withAnimation(.easeOut(duration: 0.2)) { undoable = nil; success = nil; submitting = kind }
        Task { @MainActor in
            // Stands in for "get a GPS fix, save the report" until iOS has a backend.
            try? await Task.sleep(nanoseconds: 1_100_000_000)
            let token = profile.recordReport()
            Haptics.success()
            withAnimation(.easeOut(duration: 0.25)) {
                submitting = nil
                success = Success(kind: kind, start: Date())
                undoable = Undoable(kind: kind, token: token)
                remaining = 1
            }
            withAnimation(.linear(duration: Self.undoWindow)) { remaining = 0 }
            undoTask = Task { @MainActor in
                try? await Task.sleep(nanoseconds: UInt64(Self.undoWindow * 1_000_000_000))
                guard !Task.isCancelled else { return }
                withAnimation(.easeIn(duration: 0.25)) { if undoable?.token == token { undoable = nil } }
            }
            try? await Task.sleep(nanoseconds: 2_600_000_000)
            if success?.kind == kind { success = nil }
        }
    }

    private func undo(_ item: Undoable) {
        Haptics.tick()
        undoTask?.cancel()
        profile.removeReport(item.token)
        withAnimation(.easeIn(duration: 0.25)) { undoable = nil; success = nil }
    }

    // MARK: Pieces

    private var impactChip: some View {
        let count = profile.reportsThisWeek
        let active = count > 0
        return HStack(spacing: 6) {
            Image(systemName: "flame.fill").font(.system(size: 13))
            Text(active ? "\(count) this week" : "None this week").font(.system(size: 12, weight: .semibold))
        }
        .foregroundColor(active ? .radaAccentInk : .radaTextSecondary)
        .padding(.horizontal, 12).padding(.vertical, 8)
        .background(Capsule().fill(active ? Color.radaAccent : Color.radaInk2))
        .scaleEffect(pop)
    }

    private func undoChip(_ item: Undoable) -> some View {
        HStack(spacing: 12) {
            Circle()
                .trim(from: 0, to: remaining)
                .stroke(Color.radaAccent, style: StrokeStyle(lineWidth: 3, lineCap: .round))
                .background(Circle().stroke(Color.radaTextPrimary.opacity(0.1), lineWidth: 3))
                .rotationEffect(.degrees(-90))
                .frame(width: 24, height: 24)
            Text("\(item.kind.title) reported")
                .font(.system(size: 14)).foregroundColor(.radaTextPrimary)
            Spacer()
            Button { undo(item) } label: {
                Text("Undo").font(.system(size: 13, weight: .bold)).foregroundColor(.radaAccentInk)
                    .padding(.horizontal, 16).padding(.vertical, 8)
                    .background(Capsule().fill(Color.radaAccent))
            }
            .buttonStyle(RadaPressStyle())
        }
        .padding(.leading, 14).padding(.trailing, 6).padding(.vertical, 8)
        .background(Capsule().fill(Color.radaInk2))
        .overlay(Capsule().stroke(Color.radaTextPrimary.opacity(0.08)))
    }
}

// MARK: - Tile

private struct TileSpec {
    let fill: Color, ink: Color, icon: String, sub: String
}

private func spec(_ kind: ReportKind) -> TileSpec {
    switch kind {
    case .camera: return TileSpec(fill: .radaAmber, ink: .radaAmberInk, icon: "camera.fill", sub: "Fixed or mobile")
    case .checkpoint: return TileSpec(fill: .radaOrange, ink: .white, icon: "checkmark.shield.fill", sub: "Document and vehicle checks")
    case .alcoblow: return TileSpec(fill: .radaRed, ink: .white, icon: "wineglass.fill", sub: "The warning drivers want most")
    }
}

private func ease(_ x: Double) -> Double { 1 - pow(1 - min(max(x, 0), 1), 3) }

/// Success animation as a pure function of seconds since the report succeeded.
private enum SuccessTiming {
    static let visibleFor = 2.2
    /// 0...1: how far the colour has flooded the tile.
    static func fill(_ e: Double) -> Double {
        if e < 0 { return 0 }
        if e < 0.45 { return ease(e / 0.45) }
        if e < visibleFor { return 1 }
        return 1 - ease((e - visibleFor) / 0.3)
    }
    /// 0...1: how much of the checkmark has been drawn.
    static func check(_ e: Double) -> Double {
        if e < 0.26 { return 0 }
        if e < visibleFor { return ease((e - 0.26) / 0.38) }
        return 1 - ease((e - visibleFor) / 0.15)
    }
}

/// Rotating arc, drawn in SwiftUI so it looks identical everywhere (and matches Android's).
private struct ArcSpinner: View {
    let color: Color
    var body: some View {
        TimelineView(.animation) { context in
            let angle = context.date.timeIntervalSinceReferenceDate.truncatingRemainder(dividingBy: 1) * 360
            Circle()
                .trim(from: 0, to: 0.72)
                .stroke(color, style: StrokeStyle(lineWidth: 3, lineCap: .round))
                .rotationEffect(.degrees(angle))
        }
    }
}

private struct CheckShape: Shape {
    func path(in r: CGRect) -> Path {
        var p = Path()
        p.move(to: CGPoint(x: r.width * 0.18, y: r.height * 0.54))
        p.addLine(to: CGPoint(x: r.width * 0.42, y: r.height * 0.76))
        p.addLine(to: CGPoint(x: r.width * 0.84, y: r.height * 0.28))
        return p
    }
}

struct ReportTile: View {
    let kind: ReportKind
    let isSubmitting: Bool
    let busy: Bool
    let successStart: Date?
    let onTap: () -> Void

    private let height: CGFloat = 100

    var body: some View {
        // Runs only while a success animation is playing.
        TimelineView(.animation(paused: successStart == nil)) { context in
            content(elapsed: successStart.map { context.date.timeIntervalSince($0) } ?? -1)
        }
    }

    private func content(elapsed e: Double) -> some View {
        let s = spec(kind)
        let fill = SuccessTiming.fill(e)
        let check = SuccessTiming.check(e)
        let showThanks = e >= 0 && e < SuccessTiming.visibleFor + 0.15

        return Button(action: onTap) {
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: 26).fill(Color.radaInk2)

                // Colour floods out from the icon, and rings ripple outward like a ping.
                Canvas { ctx, size in
                    let center = CGPoint(x: 48, y: size.height / 2)
                    if fill > 0 {
                        let r = hypot(size.width, size.height) * fill
                        ctx.fill(Path(ellipseIn: CGRect(x: center.x - r, y: center.y - r, width: 2 * r, height: 2 * r)),
                                 with: .color(s.fill))
                    }
                    if e >= 0 {
                        for i in 0..<3 {
                            let p = min(max((e / 0.85 * 1.36 - Double(i) * 0.18) / 0.82, 0), 1)
                            if p > 0 && p < 1 {
                                let r = 28 + size.width * 0.9 * p
                                ctx.stroke(Path(ellipseIn: CGRect(x: center.x - r, y: center.y - r, width: 2 * r, height: 2 * r)),
                                           with: .color(s.fill.opacity((1 - p) * 0.55)), lineWidth: 3)
                            }
                        }
                    }
                }
                .clipShape(RoundedRectangle(cornerRadius: 26))
                .allowsHitTesting(false)

                HStack(spacing: 16) {
                    ZStack {
                        Circle().fill(s.fill.opacity(0.18))
                        Circle().fill(s.ink.opacity(0.14)).opacity(fill)
                        if isSubmitting {
                            ArcSpinner(color: s.fill).frame(width: 30, height: 30)
                        } else {
                            Image(systemName: s.icon).font(.system(size: 24)).foregroundColor(s.fill)
                                .opacity(1 - check)
                            CheckShape().trim(from: 0, to: check)
                                .stroke(s.ink, style: StrokeStyle(lineWidth: 3.5, lineCap: .round, lineJoin: .round))
                                .frame(width: 28, height: 28)
                        }
                    }
                    .frame(width: 56, height: 56)

                    VStack(alignment: .leading, spacing: 2) {
                        ZStack(alignment: .leading) {
                            Text(kind.title).foregroundColor(.radaTextPrimary).opacity(1 - fill)
                            Text(kind.title).foregroundColor(s.ink).opacity(fill)
                        }
                        .font(.system(size: 17, weight: .semibold))
                        Text(isSubmitting ? "Getting your location…" : (showThanks ? "Reported — thanks!" : s.sub))
                            .font(.system(size: 13))
                            .foregroundColor(fill > 0.5 ? s.ink.opacity(0.8) : .radaTextSecondary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 20)
            }
            .frame(height: height)
        }
        .buttonStyle(RadaPressStyle())
        .disabled(busy)
        .opacity(busy && !isSubmitting ? 0.5 : 1)
        .accessibilityLabel("Report \(kind.title)")
    }
}
