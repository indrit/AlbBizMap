// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class StoriesRepository: ObservableObject {
    public static let shared = StoriesRepository()
    
    @Published public var stories: [Story] = []
    
    public init() {
        loadSampleStories()
    }
    
    private func loadSampleStories() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        self.stories = [
            Story(
                id: "story_1",
                userId: "user_1",
                userName: "Sofra Shqiptare",
                businessId: "biz_1",
                businessName: "Sofra Shqiptare",
                type: "business",
                category: "Restaurant",
                location: "New York",
                photos: ["https://images.unsplash.com/photo-1555396273-367ea4eb4db5"],
                text: "Fresh Tavë Kosi just out of the oven! Stop by today.",
                createdAt: now - 3600000,
                expiresAt: now + (23 * 3600000),
                isSponsored: true
            ),
            Story(
                id: "story_2",
                userId: "user_2",
                userName: "Arben B.",
                type: "user",
                location: "Bronx",
                photos: ["https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb"],
                text: "Best macchiato in town at Peja Espresso Bar!",
                createdAt: now - 7200000,
                expiresAt: now + (22 * 3600000)
            )
        ]
    }
    
    public func addStory(_ story: Story) async -> Result<String, Error> {
        var newStory = story
        if newStory.id.isEmpty {
            newStory.id = "story_" + UUID().uuidString.prefix(8)
        }
        await MainActor.run {
            self.stories.insert(newStory, at: 0)
        }
        return .success(newStory.id)
    }
    
    public func markStoryViewed(storyId: String, userId: String) {
        if let idx = stories.firstIndex(where: { $0.id == storyId }) {
            if !stories[idx].viewedBy.contains(userId) {
                stories[idx].viewedBy.append(userId)
            }
        }
    }
    
    public func deleteStory(storyId: String) {
        stories.removeAll(where: { $0.id == storyId })
    }
}
