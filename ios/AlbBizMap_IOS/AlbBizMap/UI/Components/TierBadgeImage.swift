// Bismillah Hir Rahman Nir Raheem
import SwiftUI

/// The MeTont tier badge coins (bronze/silver/gold), bundled as loose
/// resource files rather than asset catalog entries — mirrors Android's use
/// of R.drawable.metont_bronze / metont_silver / metont_gold.
public struct TierBadgeImage: View {
    public let resourceName: String

    public init(resourceName: String) {
        self.resourceName = resourceName
    }

    public var body: some View {
        if let path = Bundle.main.path(forResource: resourceName, ofType: "png"),
           let uiImage = UIImage(contentsOfFile: path) {
            Image(uiImage: uiImage)
                .resizable()
                .aspectRatio(contentMode: .fit)
        } else {
            // Fallback so a missing bundle resource never crashes the UI.
            Image(systemName: "crown.fill")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .foregroundColor(.white)
        }
    }
}
