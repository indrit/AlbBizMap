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
//
// PERFORMANCE NOTE: the sheet's outer frame is fixed at `largeHeight` at all times.
// Dragging only changes a GPU-composited `.offset(y:)` transform, never `.frame(height:)`.
// Animating frame/height forces SwiftUI to re-layout the entire content subtree on every
// touch-move event (expensive, and the cause of the previous janky/hard-to-drag feel).
// Offset is a pure compositor operation — no layout pass — which is what makes Android's
// BottomSheetScaffold (itself offset-driven internally) feel smooth, and now matches here.
public struct PersistentBottomSheet<Content: View>: View {
    public let peekHeight: CGFloat
    public let mediumFraction: CGFloat
    public let largeFraction: CGFloat
    @ViewBuilder public let content: () -> Content

    @State private var settledOffset: CGFloat? = nil
    // Plain @State, not @GestureState -- @GestureState resets to its initial
    // value (0) the instant the gesture ends, OUTSIDE of any animation and
    // before `.onEnded` even runs. That produced a one-frame "snap back to
    // start, then animate to the target" flicker on every single drag release
    // (the live offset briefly recomputes with dragTranslation=0 before
    // settledOffset catches up), which is exactly what read as "glitchy."
    // With plain @State, both the drag delta and the settled offset are only
    // ever changed by our own code, inside the same explicit transaction, so
    // there's no gap for SwiftUI to render an intermediate, wrong frame.
    @State private var dragTranslation: CGFloat = 0

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
    }

    public var body: some View {
        GeometryReader { geo in
            let largeHeight = geo.size.height * largeFraction
            let mediumHeight = geo.size.height * mediumFraction

            // Offset is measured from the sheet's "large" (fully expanded) position.
            // 0 = fully expanded; larger values push the sheet further down.
            let peekOffset = largeHeight - peekHeight
            let mediumOffset = largeHeight - mediumHeight
            let largeOffset: CGFloat = 0

            let currentSettled = settledOffset ?? peekOffset
            let liveOffset = min(peekOffset, max(largeOffset, currentSettled - dragTranslation))

            VStack(spacing: 0) {
                Capsule()
                    .fill(Color.meTontGrey.opacity(0.35))
                    .frame(width: 40, height: 5)
                    .padding(.vertical, 10)
                    .frame(maxWidth: .infinity)
                    .contentShape(Rectangle())
                    .gesture(
                        DragGesture()
                            .onChanged { value in
                                // Drag DOWN (positive translation) should increase offset (collapse);
                                // drag UP (negative translation) should decrease offset (expand).
                                // No animation here -- this needs to track the finger 1:1 every frame.
                                dragTranslation = -value.translation.height
                            }
                            .onEnded { value in
                                let proposed = currentSettled - (-value.translation.height)
                                let snapPoints = [largeOffset, mediumOffset, peekOffset]
                                let nearest = snapPoints.min(by: { abs($0 - proposed) < abs($1 - proposed) }) ?? peekOffset
                                // Folding the drag-delta reset into the SAME withAnimation block
                                // that sets the new settledOffset is what eliminates the flicker:
                                // liveOffset is a function of (settledOffset, dragTranslation), and
                                // both operands now change together, in one animated transaction,
                                // instead of dragTranslation jumping to 0 on its own first.
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.85)) {
                                    settledOffset = nearest
                                    dragTranslation = 0
                                }
                            }
                    )

                content()
            }
            .frame(maxWidth: .infinity)
            .frame(height: largeHeight, alignment: .top)
            .background(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: -2)
            )
            .offset(y: liveOffset)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            .edgesIgnoringSafeArea(.bottom)
            .onAppear {
                if settledOffset == nil {
                    settledOffset = peekOffset
                }
            }
        }
    }
}
