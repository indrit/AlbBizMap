// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Bundled multi-color Google "G" mark (same asset used on the Android "Continue
// with Google" button), for the matching iOS button.
public struct GoogleLogoImage: View {
    public init() {}

    public var body: some View {
        if let path = Bundle.main.path(forResource: "google_logo", ofType: "png"),
           let uiImage = UIImage(contentsOfFile: path) {
            Image(uiImage: uiImage).resizable().aspectRatio(contentMode: .fit)
        } else {
            Text("G").fontWeight(.bold)
        }
    }
}
