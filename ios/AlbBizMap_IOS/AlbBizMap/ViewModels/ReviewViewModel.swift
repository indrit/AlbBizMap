// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class ReviewViewModel: ObservableObject {
    @Published public var rating: Int = 0
    @Published public var comment: String = ""
    @Published public var isSubmitting: Bool = false
    @Published public var errorMessage: String? = nil
    
    public init() {}
    
    public func submitReview(businessId: String, userId: String, userName: String, photoData: [Data] = [], strings: AppStrings) async -> Bool {
        if rating < 1 {
            errorMessage = strings.pleaseSelectRating
            return false
        }
        if comment.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = strings.pleaseWriteReview
            return false
        }
        if userId.isEmpty {
            errorMessage = strings.loginRequiredForReview
            return false
        }
        
        let review = Review(
            businessId: businessId,
            userId: userId,
            userName: userName,
            rating: rating,
            comment: comment.trimmingCharacters(in: .whitespaces)
        )
        
        await MainActor.run { self.isSubmitting = true; self.errorMessage = nil }
        let res = await ReviewRepository.shared.addReview(businessId: businessId, review: review, photoData: photoData)
        await MainActor.run { self.isSubmitting = false }
        
        switch res {
        case .success: return true
        case .failure(let err):
            await MainActor.run { self.errorMessage = err.localizedDescription }
            return false
        }
    }
}
