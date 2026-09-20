// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Expand/collapse toggle for a carousel section, matching Android's SeeMoreButton.
public struct SeeMoreButton: View {
    @Environment(\.appStrings) private var strings
    public let expanded: Bool
    public let onClick: () -> Void

    public init(expanded: Bool, onClick: @escaping () -> Void) {
        self.expanded = expanded
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            HStack(spacing: 4) {
                Text(expanded ? strings.seeLess : strings.seeMore)
                    .font(.system(size: 13, weight: .medium))
                Image(systemName: expanded ? "chevron.up" : "chevron.down")
                    .font(.system(size: 12))
            }
            .foregroundColor(.meTontRed)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
        }
    }
}

// Navigates to a full list/screen instead of expanding in place, matching Android's
// SeeMoreNavButton (used for Community Announcements and Most Favorited Worldwide).
public struct SeeMoreNavButton: View {
    @Environment(\.appStrings) private var strings
    public let onClick: () -> Void

    public init(onClick: @escaping () -> Void) {
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            HStack(spacing: 4) {
                Text(strings.seeMore)
                    .font(.system(size: 13, weight: .medium))
                Image(systemName: "arrow.right")
                    .font(.system(size: 12))
            }
            .foregroundColor(.meTontRed)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
        }
    }
}
