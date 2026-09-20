import SwiftUI

struct SplashView: View {
    let onFinished: () -> Void
    @State private var reveal: CGFloat

    init(initialReveal: CGFloat = 0, onFinished: @escaping () -> Void) {
        _reveal = State(initialValue: initialReveal)
        self.onFinished = onFinished
    }

    static let markSize: CGFloat = 250
    static let lockupWidth: CGFloat = 130

    var body: some View {
        ZStack {
            Color.radaSplashInk.ignoresSafeArea()

            TwigaMark()
                .frame(width: Self.markSize, height: Self.markSize)

            VStack(spacing: 6) {
                StretchedText(text: "Rada", width: Self.lockupWidth,
                              font: .system(size: 35, weight: .semibold), color: Color(hex: 0xF3F1EC))
                StretchedText(text: "kaa rada na ntsa", width: Self.lockupWidth,
                              font: .system(size: 11.5), color: Color(hex: 0xA9B0BC))
            }
            .offset(y: 118)
            .opacity(reveal)
            .offset(y: (1 - reveal) * 14)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.65).delay(0.25)) { reveal = 1 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.8) { onFinished() }
        }
    }
}

struct StretchedText: View {
    let text: String
    let width: CGFloat
    let font: Font
    let color: Color

    @State private var natural: CGFloat = 0

    var body: some View {
        let gaps = CGFloat(max(text.count - 1, 1))
        let tracking = natural > 0 ? max((width - natural) / gaps, 0) : 0
        Text(text)
            .font(font)
            .tracking(tracking)
            .foregroundColor(color)
            .fixedSize()
            .offset(x: tracking / 2)
            .background(
                Text(text).font(font).fixedSize().hidden()
                    .background(GeometryReader { geo in
                        Color.clear.preference(key: NaturalWidthKey.self, value: geo.size.width)
                    })
            )
            .onPreferenceChange(NaturalWidthKey.self) { natural = $0 }
    }
}

private struct NaturalWidthKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = max(value, nextValue()) }
}
