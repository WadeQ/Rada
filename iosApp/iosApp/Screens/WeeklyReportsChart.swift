import SwiftUI

private func seriesColor(_ kind: ReportKind) -> Color {
    switch kind {
    case .camera: return .adaptive(light: 0xEDA100, dark: 0xC98500)
    case .checkpoint: return .adaptive(light: 0x2A78D6, dark: 0x3987E5)
    case .alcoblow: return .adaptive(light: 0xE34948, dark: 0xE66767)
    }
}

private func legendName(_ kind: ReportKind) -> String {
    switch kind {
    case .camera: return "Camera"
    case .checkpoint: return "Checkpoint"
    case .alcoblow: return "Alcohol"
    }
}

private func niceMax(_ value: Int) -> Int {
    [4, 6, 8, 10, 12, 16, 20, 30, 40, 50, 80, 100].first { $0 >= value } ?? ((value + 49) / 50 * 50)
}

private struct TopRoundedRect: Shape {
    var radius: CGFloat
    func path(in rect: CGRect) -> Path {
        let r = min(radius, rect.height / 2, rect.width / 2)
        var p = Path()
        p.move(to: CGPoint(x: rect.minX, y: rect.maxY))
        p.addLine(to: CGPoint(x: rect.minX, y: rect.minY + r))
        p.addQuadCurve(to: CGPoint(x: rect.minX + r, y: rect.minY), control: CGPoint(x: rect.minX, y: rect.minY))
        p.addLine(to: CGPoint(x: rect.maxX - r, y: rect.minY))
        p.addQuadCurve(to: CGPoint(x: rect.maxX, y: rect.minY + r), control: CGPoint(x: rect.maxX, y: rect.minY))
        p.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
        p.closeSubpath()
        return p
    }
}

struct WeeklyReportsChart: View {
    let bars: [DayBar]

    @State private var selected: Int? = nil
    @State private var progress: CGFloat = 0

    private let plotHeight: CGFloat = 84
    private let gutter: CGFloat = 22
    private let gap: CGFloat = 2

    private var axisMax: Int { niceMax(bars.map(\.total).max() ?? 0) }
    private var weekTotal: Int { bars.reduce(0) { $0 + $1.total } }
    private var selectedDay: DayBar? { selected.flatMap { bars.indices.contains($0) ? bars[$0] : nil } }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Text("This week").font(.system(size: 13.5, weight: .medium)).foregroundColor(.radaTextPrimary)
                Spacer()
                Text(summary).font(.system(size: 12)).foregroundColor(.radaTextSecondary)
            }
            .padding(.bottom, 6)

            plot
                .frame(height: plotHeight)
                .contentShape(Rectangle())
                .onTapGesture { location in
                    let slot = (chartWidth - gutter) / 7
                    guard location.x >= gutter else { return }
                    let index = Int((location.x - gutter) / slot)
                    guard (0..<7).contains(index) else { return }
                    withAnimation(.easeOut(duration: 0.2)) { selected = (selected == index) ? nil : index }
                }
                .background(GeometryReader { geo in
                    Color.clear.preference(key: ChartWidthKey.self, value: geo.size.width)
                })
                .onPreferenceChange(ChartWidthKey.self) { chartWidth = $0 }

            HStack(spacing: 0) {
                Spacer().frame(width: gutter)
                ForEach(bars) { day in
                    Text(day.label)
                        .font(.system(size: 10, weight: day.isToday ? .bold : .regular))
                        .foregroundColor(day.isToday || selected == day.id ? .radaTextPrimary : .radaTextSecondary)
                        .frame(maxWidth: .infinity)
                        .lineLimit(1)
                }
            }
            .padding(.top, 4)

            HStack(spacing: 16) {
                ForEach(ReportKind.allCases, id: \.self) { kind in
                    HStack(spacing: 6) {
                        RoundedRectangle(cornerRadius: 3).fill(seriesColor(kind)).frame(width: 10, height: 10)
                        Text("\(legendName(kind)) \(count(for: kind))")
                            .font(.system(size: 12)).foregroundColor(.radaTextSecondary)
                    }
                }
            }
            .padding(.top, 8)
        }
        .padding(16)
        .background(RoundedRectangle(cornerRadius: 22).fill(Color.radaInk2))
        .onAppear { withAnimation(.easeOut(duration: 0.9)) { progress = 1 } }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Reports per day, last 7 days: " + bars.map { "\($0.label) \($0.total)" }.joined(separator: ", "))
    }

    @State private var chartWidth: CGFloat = 300

    private var summary: String {
        if let d = selectedDay { return "\(d.label) · \(d.total) \(d.total == 1 ? "report" : "reports")" }
        return weekTotal == 0 ? "No reports yet" : "\(weekTotal) \(weekTotal == 1 ? "report" : "reports")"
    }

    private func count(for kind: ReportKind) -> Int {
        selectedDay?.counts[kind] ?? bars.reduce(0) { $0 + ($1.counts[kind] ?? 0) }
    }

    private var plot: some View {
        let top: CGFloat = 8
        let plotH = plotHeight - top
        return ZStack(alignment: .bottomLeading) {
            ForEach([0, axisMax / 2, axisMax], id: \.self) { tick in
                let y = CGFloat(tick) / CGFloat(axisMax) * plotH
                Rectangle()
                    .fill(Color.radaTextPrimary.opacity(tick == 0 ? 0.16 : 0.07))
                    .frame(height: 1)
                    .padding(.leading, gutter)
                    .offset(y: -y)
                Text("\(tick)")
                    .font(.system(size: 9)).foregroundColor(.radaTextMuted)
                    .frame(width: gutter - 6, alignment: .trailing)
                    .offset(y: -y + 6)
            }

            HStack(alignment: .bottom, spacing: 0) {
                Spacer().frame(width: gutter)
                ForEach(bars) { day in
                    ZStack(alignment: .bottom) {
                        if selected == day.id {
                            RoundedRectangle(cornerRadius: 10)
                                .fill(Color.radaTextPrimary.opacity(0.06))
                                .padding(.horizontal, 2)
                        }
                        stack(for: day, plotH: plotH)
                    }
                    .frame(maxWidth: .infinity, maxHeight: plotHeight)
                }
            }
        }
    }

    private func stack(for day: DayBar, plotH: CGFloat) -> some View {
        let present = ReportKind.allCases.filter { (day.counts[$0] ?? 0) > 0 }
        let dim = (selected != nil && selected != day.id) ? 0.45 : 1.0
        return VStack(spacing: gap) {
            ForEach(Array(present.reversed()), id: \.self) { kind in
                let h = max(CGFloat(day.counts[kind] ?? 0) / CGFloat(axisMax) * plotH, 3)
                TopRoundedRect(radius: kind == present.last ? 4 : 0)
                    .fill(seriesColor(kind))
                    .frame(height: h * progress)
            }
        }
        .frame(maxWidth: 26)
        .padding(.horizontal, 6)
        .opacity(dim)
        .animation(.easeOut(duration: 0.75).delay(Double(day.id) * 0.06), value: progress)
    }
}

private struct ChartWidthKey: PreferenceKey {
    static var defaultValue: CGFloat = 300
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = nextValue() }
}
