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
                topBar

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
                }
                .padding(.horizontal, 16)

                // ── PHOTOS (hero pager + thumbnail strip) ──────────────────
                // Mirrors Android's gallery: a swipeable hero photo with a
                // page counter and a fullscreen button, a thumbnail strip
                // underneath that jumps the pager, and tapping either opens a
                // full-bleed swipeable viewer.
                if !business.photos.isEmpty {
                    BusinessPhotoGallery(photos: business.photos)
                }

                VStack(alignment: .leading, spacing: 12) {
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
                        // Public like counter (business.likedBy / likeCount) — separate
                        // from the favorite/bookmark heart above, which is a personal
                        // saved-list and uses toggleFavorite instead. Mirrors Android's
                        // dedicated Like OutlinedButton in this same action row.
                        let isBusinessLiked = !currentUserId.isEmpty && business.likedBy.contains(currentUserId)
                        Button(action: {
                            let likeAction: () -> Void = {
                                mapViewModel.toggleLike(businessId: business.id, userId: currentUserId)
                            }
                            if AuthManager.shared.isLoggedIn {
                                likeAction()
                            } else {
                                onNavigateToAuth {
                                    mapViewModel.toggleLike(businessId: business.id, userId: AuthManager.shared.currentUser?.uid ?? "")
                                }
                            }
                        }) {
                            HStack(spacing: 4) {
                                Image(systemName: isBusinessLiked ? "hand.thumbsup.fill" : "hand.thumbsup")
                                Text("\(business.likeCount)")
                                    .fontWeight(.bold)
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(isBusinessLiked ? Color.meTontRed : Color.gray, lineWidth: 1)
                            )
                            .foregroundColor(isBusinessLiked ? .meTontRed : .gray)
                        }

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

                // Recent Reviews — mirrors Android's DetailReviewItem list.
                ReviewsSection(
                    businessId: business.id,
                    currentUserId: currentUserId,
                    currentUserName: currentUserName,
                    onNavigateToAuth: onNavigateToAuth
                )
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }
        }
        .background(Color.white)
    }

    // Firebase only ever gives a single displayName, so Auth's own
    // firstName/lastName split (see AuthManager.UserProfile) is recombined
    // here, falling back to the email prefix — same fallback Android uses
    // via FirebaseAuth's displayName/email in DetailReviewItem.
    private var currentUserName: String {
        if let user = AuthManager.shared.currentUser {
            let full = [user.firstName, user.lastName].filter { !$0.isEmpty }.joined(separator: " ")
            if !full.isEmpty { return full }
            if !user.email.isEmpty { return String(user.email.split(separator: "@").first ?? "") }
        }
        return ""
    }

    private var topBar: some View {
        HStack {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left")
                    .font(.headline)
                    .foregroundColor(.meTontBlack)
            }
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
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

// MARK: - Photo gallery

// Hero pager + thumbnail strip, matching Android's FullScreenPhotoViewer
// pattern: tap the hero photo, the fullscreen icon, or a thumbnail to browse;
// tap either the hero or a thumbnail's enlarge affordance to go full-bleed.
private struct BusinessPhotoGallery: View {
    let photos: [String]
    @State private var currentIndex: Int = 0
    @State private var showFullScreen: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            ZStack(alignment: .bottomLeading) {
                TabView(selection: $currentIndex) {
                    ForEach(photos.indices, id: \.self) { index in
                        galleryImage(photos[index])
                            .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .frame(height: 240)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .onTapGesture { showFullScreen = true }

                if photos.count > 1 {
                    Text("\(currentIndex + 1)/\(photos.count)")
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(Capsule().fill(Color.black.opacity(0.6)))
                        .padding(12)
                }

                Button(action: { showFullScreen = true }) {
                    Image(systemName: "arrow.up.left.and.arrow.down.right")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white)
                        .padding(8)
                        .background(Circle().fill(Color.black.opacity(0.5)))
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(12)
            }

            if photos.count > 1 {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(photos.indices, id: \.self) { index in
                            galleryImage(photos[index])
                                .frame(width: 72, height: 72)
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(currentIndex == index ? Color.meTontRed : Color.clear, lineWidth: 2)
                                )
                                .onTapGesture {
                                    withAnimation { currentIndex = index }
                                }
                        }
                    }
                }
            }
        }
        .padding(.horizontal, 16)
        .fullScreenCover(isPresented: $showFullScreen) {
            FullScreenPhotoViewer(photos: photos, startIndex: currentIndex)
        }
    }

    private func galleryImage(_ urlString: String) -> some View {
        Group {
            if let url = URL(string: urlString) {
                AsyncImage(url: url) { img in
                    img.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color.meTontRed.opacity(0.15)
                }
            } else {
                Color.meTontRed.opacity(0.15)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .clipped()
    }
}

// Full-bleed swipeable gallery opened by tapping the hero photo, the
// fullscreen icon, or a thumbnail — mirrors Android's FullScreenPhotoViewer
// dialog (black background, swipe between photos, counter top-trailing,
// close top-leading) instead of repeating the same images in a grid.
struct FullScreenPhotoViewer: View {
    let photos: [String]
    let startIndex: Int
    @Environment(\.dismiss) private var dismiss
    @State private var currentIndex: Int = 0

    var body: some View {
        ZStack {
            Color.black.edgesIgnoringSafeArea(.all)

            TabView(selection: $currentIndex) {
                ForEach(photos.indices, id: \.self) { index in
                    Group {
                        if let url = URL(string: photos[index]) {
                            AsyncImage(url: url) { img in
                                img.resizable().aspectRatio(contentMode: .fit)
                            } placeholder: {
                                ProgressView().tint(.white)
                            }
                        }
                    }
                    .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))

            VStack {
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.white)
                            .padding(10)
                            .background(Circle().fill(Color.black.opacity(0.5)))
                    }
                    Spacer()
                    if photos.count > 1 {
                        Text("\(currentIndex + 1)/\(photos.count)")
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundColor(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Capsule().fill(Color.black.opacity(0.5)))
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                Spacer()
            }
        }
        .onAppear { currentIndex = startIndex }
    }
}
