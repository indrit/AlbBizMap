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

    // Sentinel grouping key for the shared "Community" circle below — chosen
    // to never collide with a real Firestore document id (businessId/userId).
    private let communityGroupKey = "__community__"

    // Groups stories the same way Android's MapScreen.kt does: by businessId
    // when present, otherwise by userId — except community-type stories,
    // which all collapse into one shared circle regardless of who posted
    // them, rather than blending invisibly into that person's own circle.
    // This didn't exist on iOS at all before — every individual story got
    // its own circle — so this also brings iOS in line with Android's
    // one-circle-per-person/business grouping generally, not just the
    // community special-case.
    private var groupedStories: [(key: String, stories: [Story])] {
        var order: [String] = []
        var groups: [String: [Story]] = [:]
        for story in stories {
            let key = story.type == "community" ? communityGroupKey : (story.businessId ?? story.userId)
            if groups[key] == nil {
                order.append(key)
                groups[key] = []
            }
            groups[key]?.append(story)
        }
        return order.map { key in (key: key, stories: groups[key] ?? []) }
    }

    public var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                // Add Story Button
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
                .contentShape(Rectangle())
                .highPriorityGesture(
                    TapGesture().onEnded { onAddStoryClick() }
                )
                
                // Story avatars — one circle per group (see groupedStories above),
                // not one per individual story.
                ForEach(groupedStories, id: \.key) { group in
                    let isCommunityGroup = group.key == communityGroupKey
                    let firstStory = group.stories[0]
                    let hasViewedAll = group.stories.allSatisfy { $0.viewedBy.contains(currentUserId) }
                    let displayName = isCommunityGroup ? "Community" : (firstStory.businessName ?? firstStory.userName)
                    // Tapping opens the viewer at this group's first story's
                    // position within the full flat stories list — mirrors
                    // Android's allStories.indexOfFirst { it.id == clickedStory.id }.
                    let groupIndex = stories.firstIndex(where: { $0.id == firstStory.id }) ?? 0

                    VStack(spacing: 4) {
                        ZStack {
                            Circle()
                                .stroke(hasViewedAll ? Color.gray.opacity(0.4) : Color.meTontRed, lineWidth: 2.5)
                                .frame(width: 62, height: 62)

                            if isCommunityGroup {
                                // A megaphone reads as "this is a shared channel",
                                // not "this is a person" — same emoji already used
                                // for this type in AddStoryScreen's type picker.
                                ZStack {
                                    Circle().fill(Color(red: 0x21 / 255.0, green: 0x96 / 255.0, blue: 0xF3 / 255.0).opacity(0.2))
                                    Text("📢").font(.system(size: 22))
                                }
                                .frame(width: 54, height: 54)
                            } else if let photoUrl = firstStory.photos.first, let url = URL(string: photoUrl) {
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
                                    Text(displayName.prefix(1).uppercased())
                                        .font(.headline)
                                        .foregroundColor(.meTontRed)
                                }
                                .frame(width: 54, height: 54)
                            }
                        }

                        Text(displayName)
                            .font(.caption2)
                            .lineLimit(1)
                            .frame(width: 62)
                            .foregroundColor(.meTontBlack)
                    }
                    .contentShape(Rectangle())
                    .highPriorityGesture(
                        TapGesture().onEnded { onStoryClick(groupIndex) }
                    )
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(ScrollTouchFix())
        }
    }
}
