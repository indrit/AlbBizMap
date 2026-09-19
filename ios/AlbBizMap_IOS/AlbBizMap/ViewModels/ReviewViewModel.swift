// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class ReviewViewModel: ObservableObject {
    @Published public var rating: Int = 5
    @Published public var comment: String = ""
    @Published public var isSubmitting: Bool = false
    @Published public var errorMessage: String? = nil
    
    public init() {}
    
    public func submitReview(businessId: String, userId: String, userName: String) async -> Bool {
        if rating < 1 {
            errorMessage = "Please select a rating"
            return false
        }
        if comment.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "Please write a review"
            return false
        }
        
        let review = Review(
            businessId: businessId,
            userId: userId,
            userName: userName,
            rating: rating,
            comment: comment
        )
        
        await MainActor.run { self.isSubmitting = true; self.errorMessage = nil }
        let res = await ReviewRepository.shared.addReview(businessId: businessId, review: review)
        await MainActor.run { self.isSubmitting = false }
        
        switch res {
        case .success: return true
        case .failure(let err):
            await MainActor.run { self.errorMessage = err.localizedDescription }
            return false
        }
    }
}
