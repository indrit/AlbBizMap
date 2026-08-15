// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct AdminScreen: View {
    @StateObject private var viewModel = AdminViewModel()
    public let currentUserId: String
    public let onBackClick: () -> Void
    
    public init(currentUserId: String, onBackClick: @escaping () -> Void) {
        self.currentUserId = currentUserId
        self.onBackClick = onBackClick
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text("Admin Verification Panel")
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            if viewModel.pendingClaims.isEmpty {
                VStack {
                    Spacer()
                    Image(systemName: "checkmark.shield.fill")
                        .font(.system(size: 50))
                        .foregroundColor(.green)
                    Text("No pending business claims")
                        .font(.headline)
                        .foregroundColor(.gray)
                    Spacer()
                }
            } else {
                ScrollView {
                    VStack(spacing: 14) {
                        ForEach(viewModel.pendingClaims) { claim in
                            VStack(alignment: .leading, spacing: 8) {
                                Text(claim.businessName)
                                    .font(.headline)
                                Text("Claimed by: \(claim.userName) (\(claim.userEmail))")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                                Text("Reason: \(claim.reason)")
                                    .font(.subheadline)
                                
                                HStack(spacing: 12) {
                                    Button(action: {
                                        Task { await viewModel.reject(claimId: claim.id) }
                                    }) {
                                        Text("Reject")
                                            .fontWeight(.semibold)
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 8)
                                            .background(Color.gray.opacity(0.15))
                                            .foregroundColor(.black)
                                            .cornerRadius(8)
                                    }
                                    
                                    Button(action: {
                                        Task { await viewModel.approve(claim: claim) }
                                    }) {
                                        Text("Approve Claim")
                                            .fontWeight(.semibold)
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 8)
                                            .background(Color.green)
                                            .foregroundColor(.white)
                                            .cornerRadius(8)
                                    }
                                }
                            }
                            .padding(14)
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(radius: 2)
                        }
                    }
                    .padding(16)
                }
            }
        }
        .background(Color.meTontBackground)
    }
}
