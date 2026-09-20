// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import UIKit

// Thin UIViewControllerRepresentable wrapper over UIImagePickerController's
// camera source — PHPickerViewController (used for the gallery picker via
// PhotosPicker) has no camera mode, so a photo taken with the camera needs
// this instead. Mirrors Android's ActivityResultContracts.TakePicture() half
// of the Gallery/Camera chooser in AddBusinessScreen.kt.
public struct CameraPicker: UIViewControllerRepresentable {
    public let onCapture: (Data) -> Void
    @Environment(\.dismiss) private var dismiss

    public init(onCapture: @escaping (Data) -> Void) {
        self.onCapture = onCapture
    }

    public func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }

    public func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    public func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    public final class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraPicker
        init(_ parent: CameraPicker) { self.parent = parent }

        public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let image = info[.originalImage] as? UIImage, let data = image.jpegData(compressionQuality: 0.85) {
                parent.onCapture(data)
            }
            parent.dismiss()
        }

        public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}
