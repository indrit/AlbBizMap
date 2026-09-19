// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct BusinessDetailScreen: View {
    @Environment(\.appStrings) private var strings
    
    public let business: Business
    public let currentUserId: String
    public let onWriteReviewClick: () -> Void
    public let onEditClick: () -> Void
    public let onBackClick: () -> Void
    public let onUpgradeClick: () -> Void
    public let onNavigateToAuth: (@escaping () -> Void) -> Void
    @ObservedObject public var mapViewModel: MapViewModel
    
    public init(
        business: Business,
        currentUserId: String,
        onWriteReviewClick: @escaping () -> Void,
        onEditClick: @escaping () -> Void,
        onBackClick: @escaping () -> Void,
        onUpgradeClick: @escaping () -> Void,
        onNavigateToAuth: @escaping (@escaping () -> Void) -> Void,
        mapViewModel: MapViewModel
    ) {
        self.business = business
        self.currentUserId = currentUserId
        self.onWriteReviewClick = onWriteReviewClick
        self.onEditClick = onEditClick
        self.onBackClick = onBackClick
        self.onUpgradeClick = onUpgradeClick
        self.onNavigateToAuth = onNavigateToAuth
        self.mapViewModel = mapViewModel
    }
    
    public var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Header Image Banner
                ZStack(alignment: .topLeading) {
                    if let photoUrl = business.photos.first, let url = URL(string: photoUrl) {
                        AsyncImage(url: url) { img in
                            img.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color.meTontRed.opacity(0.15)
                        }
                        .frame(height: 220)
                        .clipped()
                    } else {
                        Rectangle()
                            .fill(Color.meTontRed.opacity(0.15))
                            .frame(height: 220)
                    }
                    
                    Button(action: onBackClick) {
                        Image(systemName: "chevron.left")
                            .font(.headline)
                            .foregroundColor(.meTontBlack)
                            .padding(10)
                            .background(Circle().fill(.white).shadow(radius: 3))
                    }
                    .padding(.top, 50)
                    .padding(.leading, 16)
                }
                
                // Business Info Section
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(business.name)
                                .font(.title2)
                                .fontWeight(.bold)
                            Text(business.category)
                                .font(.subheadline)
                                .foregroundColor(.meTontRed)
                                .fontWeight(.semibold)
                        }
                        Spacer()
                        
                        let isFav = mapViewModel.favoriteIds.contains(business.id)
                        Button(action: {
                            if AuthManager.shared.isLoggedIn {
                                mapViewModel.toggleFavorite(businessId: business.id, userId: currentUserId)
                            } else {
                                onNavigateToAuth {
                                    mapViewModel.toggleFavorite(businessId: business.id, userId: AuthManager.shared.currentUser?.uid ?? "")
                                }
                            }
                        }) {
                            Image(systemName: isFav ? "heart.fill" : "heart")
                                .font(.title2)
                                .foregroundColor(isFav ? .meTontRed : .gray)
                        }
                    }
                    
                    // Badges
                    HStack(spacing: 8) {
                        if business.isVerified {
                            badgeView(text: strings.verified, icon: "checkmark.seal.fill", color: .blue)
                        }
                        if business.isAlbanianOwned {
                            badgeView(text: strings.albanianOwned, icon: "flag.fill", color: .meTontRed)
                        }
                        if business.isPremium {
                            badgeView(text: strings.premium, icon: "crown.fill", color: .orange)
                        }
                    }
                    
                    Text(business.description)
                        .font(.body)
                        .foregroundColor(.secondary)
                    
                    if !business.longDescription.isEmpty {
                        Text(business.longDescription)
                            .font(.subheadline)
                            .foregroundColor(.meTontBlack)
                    }
                    
                    Divider()
                    
                    // Contact Info
                    VStack(alignment: .leading, spacing: 10) {
                        if !business.address.isEmpty {
                            infoRow(icon: "mappin.circle.fill", text: "\(business.address), \(business.city)")
                        }
                        if !business.phone.isEmpty {
                            infoRow(icon: "phone.fill", text: business.phone)
                        }
                        if !business.email.isEmpty {
                            infoRow(icon: "envelope.fill", text: business.email)
                        }
                        if !business.website.isEmpty {
                            infoRow(icon: "globe", text: business.website)
                        }
                    }
                    
                    Divider()
                    
                    // Action Buttons
                    HStack(spacing: 12) {
                        Button(action: onWriteReviewClick) {
                            HStack {
                                Image(systemName: "square.and.pencil")
                                Text(strings.writeReview)
                            }
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.meTontRed)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                        }
                        
                        if business.ownerId == currentUserId {
                            Button(action: onEditClick) {
                                HStack {
                                    Image(systemName: "pencil")
                                    Text(strings.editBusiness)
                                }
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(Color.gray.opacity(0.15))
                                .foregroundColor(.meTontBlack)
                                .cornerRadius(10)
                            }
                        }
                    }
                    
                    if !business.isPremium && business.ownerId == currentUserId {
                        Button(action: onUpgradeClick) {
                            HStack {
                                Image(systemName: "crown.fill")
                                    .foregroundColor(.orange)
                                Text(strings.upgradeToPremium)
                                    .fontWeight(.bold)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.orange.opacity(0.15))
                            .foregroundColor(.orange)
                            .cornerRadius(10)
                        }
                        .padding(.top, 4)
                    }
                }
                .padding(16)
            }
        }
        .edgesIgnoringSafeArea(.top)
        .background(Color.white)
    }
    
    private func badgeView(text: String, icon: String, color: Color) -> some View {
        HStack(spacing: 4) {
            Image(systemName: icon).font(.caption2)
            Text(text).font(.caption2).fontWeight(.semibold)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(color.opacity(0.12))
        .foregroundColor(color)
        .cornerRadius(6)
    }
    
    private func infoRow(icon: String, text: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .foregroundColor(.meTontRed)
                .frame(width: 20)
            Text(text)
                .font(.subheadline)
        }
    }
}
