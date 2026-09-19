// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// A bottom sheet that lives INSIDE the screen's own view hierarchy, unlike SwiftUI's
// system `.sheet()` modifier, which presents at the window level and therefore floats
// above every other screen in the app (login, overlays, other tabs). This is the
// closest match to Android's `BottomSheetScaffold`, which is likewise just a normal
// part of that one screen's layout, not a separate window-level presentation.
//
// The sheet snaps between three heights — peek / medium / large — matching Android's
// peekHeight + expand behavior. Drag the handle to resize; the content below scrolls
// independently once expanded.
public struct PersistentBottomSheet<Content: View>: View {
    public let peekHeight: CGFloat
    public let mediumFraction: CGFloat
    public let largeFraction: CGFloat
    @ViewBuilder public let content: () -> Content

    @State private var settledHeight: CGFloat
    @GestureState private var dragTranslation: CGFloat = 0

    public init(
        peekHeight: CGFloat = 140,
        mediumFraction: CGFloat = 0.45,
        largeFraction: CGFloat = 0.92,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.peekHeight = peekHeight
        self.mediumFraction = mediumFraction
        self.largeFraction = largeFraction
        self.content = content
        _settledHeight = State(initialValue: peekHeight)
    }

    public var body: some View {
        GeometryReader { geo in
            let mediumHeight = geo.size.height * mediumFraction
            let largeHeight = geo.size.height * largeFraction
            let liveHeight = min(largeHeight, max(peekHeight, settledHeight - dragTranslation))

            VStack(spacing: 0) {
                Capsule()
                    .fill(Color.meTontGrey.opacity(0.35))
                    .frame(width: 40, height: 5)
                    .padding(.vertical, 10)
                    .frame(maxWidth: .infinity)
                    .contentShape(Rectangle())
                    .gesture(
                        DragGesture()
                            .updating($dragTranslation) { value, state, _ in
                                state = value.translation.height
                            }
                            .onEnded { value in
                                let proposed = settledHeight - value.translation.height
                                let snapPoints = [peekHeight, mediumHeight, largeHeight]
                                let nearest = snapPoints.min(by: { abs($0 - proposed) < abs($1 - proposed) }) ?? peekHeight
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.85)) {
                                    settledHeight = nearest
                                }
                            }
                    )

                content()
            }
            .frame(maxWidth: .infinity)
            .frame(height: liveHeight, alignment: .top)
            .background(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: -2)
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            .edgesIgnoringSafeArea(.bottom)
        }
    }
}
