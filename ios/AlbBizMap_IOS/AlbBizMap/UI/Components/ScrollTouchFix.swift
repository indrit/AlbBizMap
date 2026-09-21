// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import UIKit

/// Invisible helper: place as a `.background()` on content INSIDE a SwiftUI `ScrollView`.
/// It walks up its own superview chain to find the ScrollView's underlying `UIScrollView`
/// and sets `delaysContentTouches = false` on it — without replacing or restructuring the
/// SwiftUI ScrollView itself, so none of its layout/rendering behavior changes.
///
/// WHY: UIScrollView holds every touch briefly (delaysContentTouches, default true) to
/// decide whether it's a scroll or a tap meant for a subview. Nested inside another
/// scroll view — exactly StoryBarView's horizontal row inside MapBottomSheetContent's
/// outer vertical ScrollView — that hold/cancel logic can swallow taps on subviews
/// entirely, silently. This flips it off for just the ScrollView it's placed in.
struct ScrollTouchFix: UIViewRepresentable {
    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: .zero)
        view.isUserInteractionEnabled = false
        view.backgroundColor = .clear
        DispatchQueue.main.async {
            Self.fixNearestScrollView(from: view)
        }
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        DispatchQueue.main.async {
            Self.fixNearestScrollView(from: uiView)
        }
    }

    private static func fixNearestScrollView(from view: UIView) {
        var current: UIView? = view.superview
        while let v = current {
            if let scrollView = v as? UIScrollView {
                scrollView.delaysContentTouches = false
                return
            }
            current = v.superview
        }
    }
}
