// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import AVKit
import AVFoundation

/// Full-screen, non-interactive video player that reports when playback ends.
/// Mirrors the Android SplashScreen's use of ExoPlayer/PlayerView.
private struct SplashVideoPlayer: UIViewControllerRepresentable {
    let onEnded: () -> Void

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()
        controller.showsPlaybackControls = false
        controller.videoGravity = .resizeAspectFill
        controller.view.backgroundColor = UIColor(red: 0xE4/255.0, green: 0x1E/255.0, blue: 0x20/255.0, alpha: 1)

        if let url = Bundle.main.url(forResource: "MeTont_AnimationVid3", withExtension: "mp4") {
            let player = AVPlayer(url: url)
            controller.player = player

            NotificationCenter.default.addObserver(
                forName: .AVPlayerItemDidPlayToEndTime,
                object: player.currentItem,
                queue: .main
            ) { _ in
                context.coordinator.notifyEnded()
            }

            player.play()
        } else {
            // Asset missing from the bundle — don't block the app on a splash video.
            DispatchQueue.main.async {
                context.coordinator.notifyEnded()
            }
        }

        return controller
    }

    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(callback: onEnded)
    }

    class Coordinator {
        private let callback: () -> Void
        private var fired = false

        init(callback: @escaping () -> Void) {
            self.callback = callback
        }

        func notifyEnded() {
            guard !fired else { return }
            fired = true
            callback()
        }
    }
}

public struct SplashScreen: View {
    @Environment(\.appStrings) private var strings
    public let onFinished: () -> Void

    @State private var videoEnded: Bool = false
    @State private var hasFinished: Bool = false

    public init(onFinished: @escaping () -> Void) {
        self.onFinished = onFinished
    }

    public var body: some View {
        ZStack {
            Color.meTontRed.edgesIgnoringSafeArea(.all)

            if !videoEnded {
                SplashVideoPlayer {
                    videoEnded = true
                }
                .edgesIgnoringSafeArea(.all)
            } else {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(1.5)
            }
        }
        .onChange(of: videoEnded) { _, ended in
            guard ended else { return }
            // Gives the location fetch kicked off alongside this screen a little
            // more runway to land before MapScreen composes, matching Android.
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                finishOnce()
            }
        }
        .onAppear {
            // Fallback in case the video fails to load or play, matching Android's
            // 8s safety timeout.
            DispatchQueue.main.asyncAfter(deadline: .now() + 8.0) {
                finishOnce()
            }
        }
    }

    private func finishOnce() {
        guard !hasFinished else { return }
        hasFinished = true
        onFinished()
    }
}
