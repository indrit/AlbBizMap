// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct StoryViewerScreen: View {
    public let stories: [Story]
    public let initialIndex: Int
    public let onClose: () -> Void
    public let onBusinessClick: (String) -> Void
    @ObservedObject public var storiesViewModel: StoriesViewModel
    
    @State private var currentIndex: Int = 0
    // True for as long as the user is pressing down anywhere on the story —
    // Instagram-style hold to pause. There's no auto-advance timer here to
    // actually freeze (unlike Android's story viewer), so what this really
    // fixes is that a tap gesture in SwiftUI has no built-in maximum
    // duration: holding down and then releasing used to still register as a
    // plain tap the instant you lifted your finger, immediately jumping to
    // the next story regardless of how long you held it. Tracking press
    // duration ourselves (below) is what lets a genuine hold do nothing on
    // release instead of always advancing.
    @State private var isPaused: Bool = false
    @State private var pressStartTime: Date? = nil

    public init(stories: [Story], initialIndex: Int, onClose: @escaping () -> Void, onBusinessClick: @escaping (String) -> Void, storiesViewModel: StoriesViewModel) {
        self.stories = stories
        self.initialIndex = initialIndex
        self.onClose = onClose
        self.onBusinessClick = onBusinessClick
        self.storiesViewModel = storiesViewModel
        _currentIndex = State(initialValue: initialIndex)
    }
    
    public var body: some View {
        ZStack {
            Color.black.edgesIgnoringSafeArea(.all)
            
            if stories.indices.contains(currentIndex) {
                let story = stories[currentIndex]
                
                VStack {
                    // Header bar
                    HStack {
                        VStack(alignment: .leading) {
                            Text(story.userName)
                                .font(.headline).foregroundColor(.white)
                            Text(story.location)
                                .font(.caption).foregroundColor(.gray)
                        }
                        Spacer()
                        Button(action: onClose) {
                            Image(systemName: "xmark.circle.fill")
                                .font(.title)
                                .foregroundColor(.white)
                        }
                    }
                    .padding(.top, 50)
                    .padding(.horizontal, 16)
                    
                    Spacer()
                    
                    if let photoUrl = story.photos.first, let url = URL(string: photoUrl) {
                        AsyncImage(url: url) { img in
                            img.resizable().aspectRatio(contentMode: .fit)
                        } placeholder: {
                            ProgressView()
                        }

                        if !story.text.isEmpty {
                            Text(story.text)
                                .font(.body)
                                .foregroundColor(.white)
                                .padding()
                                .background(Color.black.opacity(0.6))
                                .cornerRadius(10)
                                .padding()
                        }
                    } else {
                        // No photo (e.g. a free-tier business's auto "Just opened"
                        // story) — show a colored background with the story text
                        // as a big centered headline instead of a blank black
                        // screen, matching Android's StoryViewerScreen fallback.
                        ZStack {
                            storyBackgroundColor(for: story.type)
                            Text(story.text)
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(.white)
                                .multilineTextAlignment(.center)
                                .padding(32)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    }
                    
                    if let bizId = story.businessId {
                        Button(action: {
                            onBusinessClick(bizId)
                        }) {
                            Text("View Business Profile")
                                .fontWeight(.bold)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 10)
                                .background(Color.meTontRed)
                                .foregroundColor(.white)
                                .cornerRadius(20)
                        }
                        .padding(.bottom, 30)
                    }
                    
                    Spacer()
                }
            }

            // Dims slightly while held, purely as feedback that the hold
            // registered — matches Instagram's own subtle dim-on-hold.
            Color.black.opacity(isPaused ? 0.15 : 0)
                .allowsHitTesting(false)
                .animation(.easeOut(duration: 0.15), value: isPaused)
        }
        // minimumDistance: 0 makes this fire on press-down (via onChanged)
        // rather than waiting for a drag, so it doubles as our press/release
        // detector. onChanged fires repeatedly while held — the pressStartTime
        // nil-check makes sure we only record the start once per press rather
        // than resetting it on every touch-move update.
        //
        // simultaneousGesture rather than gesture: a plain DragGesture(minimumDistance: 0)
        // attached with .gesture() claims a touch immediately on contact, which
        // reliably steals taps away from the Close and "View Business Profile"
        // buttons that live inside this same view. simultaneousGesture lets
        // both recognize the same touch independently — those buttons still
        // fire their own action, and any redundant currentIndex bump this
        // gesture also fires alongside them is harmless, since both buttons
        // already dismiss this whole screen (onClose / onBusinessClick) the
        // instant they're tapped.
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    if pressStartTime == nil {
                        pressStartTime = Date()
                        isPaused = true
                    }
                }
                .onEnded { value in
                    let heldDuration = pressStartTime.map { Date().timeIntervalSince($0) } ?? 0
                    pressStartTime = nil
                    isPaused = false
                    // Matches the ~0.5s tap-vs-hold threshold Android's story
                    // viewer gesture uses — anything shorter still advances
                    // like a normal tap; anything longer was a hold, so it
                    // just resumes without jumping to the next story.
                    guard heldDuration < 0.5 else { return }
                    // Same reliability fix as Android's tap zones: without
                    // this, a real swipe still resolved as a plain
                    // tap-to-advance on release, since nothing checked how
                    // far the touch actually moved — DragGesture's own
                    // translation makes this simpler here than Android
                    // needed, no extra offset-tracking required. A real
                    // swipe is now a no-op instead of misfiring as advance.
                    let moveDistance = hypot(value.translation.width, value.translation.height)
                    guard moveDistance < 24 else { return }
                    if currentIndex < stories.count - 1 {
                        currentIndex += 1
                    } else {
                        onClose()
                    }
                }
        )
    }

    private func storyBackgroundColor(for type: String) -> Color {
        switch type {
        case "community": return Color(red: 0x21 / 255.0, green: 0x96 / 255.0, blue: 0xF3 / 255.0)
        case "business": return .meTontRed
        default: return Color(red: 0x1A / 255.0, green: 0x1A / 255.0, blue: 0x1A / 255.0)
        }
    }
}
