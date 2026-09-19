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
}
