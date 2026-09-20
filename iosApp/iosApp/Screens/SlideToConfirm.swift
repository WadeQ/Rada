import SwiftUI

struct SlideToConfirm: View {
    let label: String
    let doneLabel: String
    let onConfirm: () -> Void

    @State private var dragOffset: CGFloat = 0
    @State private var confirmed = false
    private let handleSize: CGFloat = 44

    var body: some View {
        GeometryReader { geo in
            let maxOffset = max(geo.size.width - handleSize - 8, 0)
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: 26)
                    .fill(Color.radaInk2)

                RoundedRectangle(cornerRadius: 26)
                    .fill(Color.radaAccent)
                    .frame(width: min(dragOffset + handleSize + 8, geo.size.width))
                    .opacity(confirmed ? 1 : min(dragOffset / max(maxOffset, 1), 1))

                Text(confirmed ? doneLabel : label)
                    .font(.system(size: 13.5, weight: .semibold))
                    .foregroundColor(confirmed ? .radaAccentInk : .radaTextSecondary)
                    .frame(maxWidth: .infinity, alignment: .center)

                Circle()
                    .fill(Color.radaAccent)
                    .frame(width: handleSize, height: handleSize)
                    .overlay(Image(systemName: "arrow.right").foregroundColor(.radaAccentInk))
                    .offset(x: 4 + dragOffset, y: 0)
                    .gesture(
                        DragGesture()
                            .onChanged { value in
                                guard !confirmed else { return }
                                dragOffset = min(max(value.translation.width, 0), maxOffset)
                            }
                            .onEnded { _ in
                                guard !confirmed else { return }
                                if dragOffset >= maxOffset * 0.8 {
                                    withAnimation(.easeOut(duration: 0.25)) { dragOffset = maxOffset }
                                    confirmed = true
                                    onConfirm()
                                } else {
                                    withAnimation(.easeOut(duration: 0.25)) { dragOffset = 0 }
                                }
                            }
                    )
            }
        }
        .frame(height: 52)
    }
}
