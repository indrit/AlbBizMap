// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct StoryBarView: View {
    public let stories: [Story]
    public let currentUserId: String
    public let onAddStoryClick: () -> Void
    public let onStoryClick: (Int) -> Void
    
    public init(stories: [Story], currentUserId: String, onAddStoryClick: @escaping () -> Void, onStoryClick: @escaping (Int) -> Void) {
        self.stories = stories
        self.currentUserId = currentUserId
        self.onAddStoryClick = onAddStoryClick
        self.onStoryClick = onStoryClick
    }
    
    public var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                // Add Story Button. Uses a real Button instead of a bare
                // .onTapGesture — this bar sits inside MapBottomSheetContent's
                // vertical ScrollView, and a plain .onTapGesture on a non-Button
                // view nested inside (or near) a ScrollView is a known SwiftUI
                // gotcha: its UITapGestureRecognizer loses gesture arbitration
                // to the ScrollView's own pan recognizer and silently never
                // fires, even though the view renders normally. Button's
                // UIControl-based hit testing doesn't have that conflict.
                Button(action: onAddStoryClick) {
                    VStack(spacing: 4) {
                        ZStack(alignment: .bottomTrailing) {
                            Circle()
                                .fill(Color.meTontRed.opacity(0.1))
                                .frame(width: 58, height: 58)

                            Image(systemName: "plus.circle.fill")
                                .font(.system(size: 20))
                                .foregroundColor(.meTontRed)
                                .background(Circle().fill(.white))
                        }
                        Text("Add Story")
                            .font(.caption2)
                            .fontWeight(.medium)
                            .foregroundColor(.meTontBlack)
                    }
                }
                .buttonStyle(.plain)

                // Story avatars — same Button-instead-of-onTapGesture fix as above.
                ForEach(Array(stories.enumerated()), id: \.element.id) { index, story in
                    let isViewed = story.viewedBy.contains(currentUserId)
                    Button(action: { onStoryClick(index) }) {
                        VStack(spacing: 4) {
                            ZStack {
                                Circle()
                                    .stroke(isViewed ? Color.gray.opacity(0.4) : Color.meTontRed, lineWidth: 2.5)
                                    .frame(width: 62, height: 62)

                                if let photoUrl = story.photos.first, let url = URL(string: photoUrl) {
                                    AsyncImage(url: url) { img in
                                        img.resizable().aspectRatio(contentMode: .fill)
                                    } placeholder: {
                                        Color.meTontRed.opacity(0.2)
                                    }
                                    .frame(width: 54, height: 54)
                                    .clipShape(Circle())
                                } else {
                                    ZStack {
                                        Circle().fill(Color.meTontRed.opacity(0.2))
                                        Text(story.userName.prefix(1).uppercased())
                                            .font(.headline)
                                            .foregroundColor(.meTontRed)
                                    }
                                    .frame(width: 54, height: 54)
                                }
                            }

                            Text(story.userName)
                                .font(.caption2)
                                .lineLimit(1)
                                .frame(width: 62)
                                .foregroundColor(.meTontBlack)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
    }
}
