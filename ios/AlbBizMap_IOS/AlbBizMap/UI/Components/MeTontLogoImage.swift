// Bismillah Hir Rahman Nir Raheem
import SwiftUI

/// The real MeTont app logo (transparent background), bundled as a loose
/// resource file rather than an asset catalog entry. Mirrors Android's use
/// of R.drawable.metont_nobackgroundcolor.
public struct MeTontLogoImage: View {
    public var contentMode: ContentMode = .fit

    public init(contentMode: ContentMode = .fit) {
        self.contentMode = contentMode
    }

    public var body: some View {
        if let path = Bundle.main.path(forResource: "metont_nobackgroundcolor", ofType: "png"),
           let uiImage = UIImage(contentsOfFile: path) {
            Image(uiImage: uiImage)
                .resizable()
                .aspectRatio(contentMode: contentMode)
        } else {
            // Fallback so a missing bundle resource never crashes the UI.
            Image(systemName: "map.fill")
                .resizable()
                .aspectRatio(contentMode: contentMode)
                .foregroundColor(.white)
        }
    }
}
