// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class ReviewRepository: ObservableObject {
    public static let shared = ReviewRepository()
    
    @Published public var reviewsByBusiness: [String: [Review]] = [:]
    @Published public var repliesByReview: [String: [Reply]] = [:]
    
    private let reportThreshold = 3
    
    public init() {
        loadSampleReviews()
    }
    
    private func loadSampleReviews() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        self.reviewsByBusiness["biz_1"] = [
            Review(
                id: "rev_1",
                businessId: "biz_1",
                userId: "user_10",
                userName: "Dritan K.",
                rating: 5,
                comment: "Best Albanian food in New York! The tavë kosi was phenomenal and authentic.",
                createdAt: now - 86400000 * 2,
                ownerReply: "Faleminderit shumë Dritan! Looking forward to seeing you again soon.",
                ownerReplyAt: now - 86400000 * 1,
                likedBy: ["user_1", "user_2"]
            ),
            Review(
                id: "rev_2",
                businessId: "biz_1",
                userId: "user_11",
                userName: "Elira M.",
                rating: 5,
                comment: "Great atmosphere and friendly staff. Highly recommend the flia!",
                createdAt: now - 86400000 * 5,
                likedBy: ["user_1"]
            )
        ]
    }
    
    public func getReviews(businessId: String) -> [Review] {
        let list = reviewsByBusiness[businessId] ?? []
        return list.filter { $0.reportCount < reportThreshold }.sorted(by: { $0.createdAt > $1.createdAt })
    }
    
    public func addReview(businessId: String, review: Review) async -> Result<String, Error> {
        var newRev = review
        if newRev.id.isEmpty {
            newRev.id = "rev_" + UUID().uuidString.prefix(8)
        }
        await MainActor.run {
            var current = self.reviewsByBusiness[businessId] ?? []
            current.insert(newRev, at: 0)
            self.reviewsByBusiness[businessId] = current
            self.updateBusinessStats(businessId: businessId)
        }
        return .success(newRev.id)
    }
    
    public func reportReview(businessId: String, reviewId: String, userId: String) -> Result<Void, Error> {
        if var list = reviewsByBusiness[businessId], let idx = list.firstIndex(where: { $0.id == reviewId }) {
            if list[idx].reportedBy.contains(userId) {
                return .failure(NSError(domain: "ReviewRepo", code: 400, userInfo: [NSLocalizedDescriptionKey: "Already reported"]))
            }
            list[idx].reportedBy.append(userId)
            list[idx].reportCount += 1
            reviewsByBusiness[businessId] = list
            updateBusinessStats(businessId: businessId)
        }
        return .success(())
    }
    
    public func toggleLike(businessId: String, reviewId: String, userId: String) {
        if var list = reviewsByBusiness[businessId], let idx = list.firstIndex(where: { $0.id == reviewId }) {
            if list[idx].likedBy.contains(userId) {
                list[idx].likedBy.removeAll(where: { $0 == userId })
            } else {
                list[idx].likedBy.append(userId)
            }
            reviewsByBusiness[businessId] = list
        }
    }
    
    private func updateBusinessStats(businessId: String) {
        let reviews = getReviews(businessId: businessId)
        guard !reviews.isEmpty else { return }
        let avgRating = Double(reviews.reduce(0) { $0 + $1.rating }) / Double(reviews.count)
        if let idx = FirestoreService.shared.businesses.firstIndex(where: { $0.id == businessId }) {
            FirestoreService.shared.businesses[idx].rating = avgRating
            FirestoreService.shared.businesses[idx].reviewCount = reviews.count
        }
    }
}
