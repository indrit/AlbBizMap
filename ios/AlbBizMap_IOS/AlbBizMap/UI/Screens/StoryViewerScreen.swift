// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct StoryViewerScreen: View {
    public let stories: [Story]
    public let initialIndex: Int
    public let onClose: () -> Void
    public let onBusinessClick: (String) -> Void
    @ObservedObject public var storiesViewModel: StoriesViewModel
    
    @State private var currentIndex: Int = 0
    
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
        }
        .onTapGesture {
            if currentIndex < stories.count - 1 {
                currentIndex += 1
            } else {
                onClose()
            }
        }
    }

    private func storyBackgroundColor(for type: String) -> Color {
        switch type {
        case "community": return Color(red: 0x21 / 255.0, green: 0x96 / 255.0, blue: 0xF3 / 255.0)
        case "business": return .meTontRed
        default: return Color(red: 0x1A / 255.0, green: 0x1A / 255.0, blue: 0x1A / 255.0)
        }
    }
}
