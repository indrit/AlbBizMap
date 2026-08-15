// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct AddReviewScreen: View {
    @Environment(\.appStrings) private var strings
    
    public let businessId: String
    public let onReviewSubmitted: () -> Void
    
    @StateObject private var viewModel = ReviewViewModel()
    
    public init(businessId: String, onReviewSubmitted: @escaping () -> Void) {
        self.businessId = businessId
        self.onReviewSubmitted = onReviewSubmitted
    }
    
    public var body: some View {
        VStack(spacing: 20) {
            Text(strings.rateThisBusiness)
                .font(.title2)
                .fontWeight(.bold)
            
            // Star rating picker
            HStack(spacing: 12) {
                ForEach(1...5, id: \.self) { star in
                    Image(systemName: star <= viewModel.rating ? "star.fill" : "star")
                        .font(.title)
                        .foregroundColor(star <= viewModel.rating ? .orange : .gray)
                        .onTapGesture {
                            viewModel.rating = star
                        }
                }
            }
            
            TextEditor(text: $viewModel.comment)
                .frame(height: 120)
                .padding(8)
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.gray.opacity(0.3)))
            
            if let err = viewModel.errorMessage {
                Text(err).foregroundColor(.red).font(.caption)
            }
            
            Button(action: {
                Task {
                    let user = AuthManager.shared.currentUser
                    let success = await viewModel.submitReview(
                        businessId: businessId,
                        userId: user?.uid ?? "",
                        userName: "\(user?.firstName ?? "User") \(user?.lastName ?? "")".trimmingCharacters(in: .whitespaces)
                    )
                    if success { onReviewSubmitted() }
                }
            }) {
                Text(viewModel.isSubmitting ? strings.submitting : strings.submitReview)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .foregroundColor(.white)
                    .background(Color.meTontRed)
                    .cornerRadius(10)
            }
        }
        .padding(24)
        .background(Color.white)
        .cornerRadius(20)
        .shadow(radius: 10)
        .padding(.horizontal, 24)
    }
}
