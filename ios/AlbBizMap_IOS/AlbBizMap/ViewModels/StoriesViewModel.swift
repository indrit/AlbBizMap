// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class StoriesViewModel: ObservableObject {
    @Published public var stories: [Story] = []
    @Published public var isLoading: Bool = false
    
    private var cancellables = Set<AnyCancellable>()
    
    public init() {
        StoriesRepository.shared.$stories
            .assign(to: \.stories, on: self)
            .store(in: &cancellables)
    }
    
    public func postStory(story: Story, photoData: [Data] = []) async -> Bool {
        isLoading = true
        let res = await StoriesRepository.shared.addStory(story, photoData: photoData)
        await MainActor.run {
            self.isLoading = false
        }
        switch res {
        case .success: return true
        case .failure: return false
        }
    }
    
    public func markViewed(storyId: String, userId: String) {
        StoriesRepository.shared.markStoryViewed(storyId: storyId, userId: userId)
    }
}
