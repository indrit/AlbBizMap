// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Shared white rounded card with a bold red title, used by AddBusinessScreen
// and EditBusinessScreen (and available to any other form-style screen) to
// group a set of fields under one heading.
public struct SectionCard<Content: View>: View {
    let title: String
    let content: Content

    public init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.meTontRed)
            content
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
    }
}
