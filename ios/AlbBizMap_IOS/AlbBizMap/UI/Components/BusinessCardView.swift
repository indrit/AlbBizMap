// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import CoreLocation

// Matches Android's shared BusinessListItem composable (BusinessListScreen.kt) —
// used the same way here across My Favorites, Business List, and My Businesses:
// 84pt photo with a favorite-heart overlay, name + rating, category, tier/status
// badges, and distance from the user when available.
public struct BusinessCardView: View {
    @Environment(\.appStrings) private var strings

    public let business: Business
    public let isFavorite: Bool
    public let onFavoriteToggle: () -> Void
    public let onClick: () -> Void

    public init(business: Business, isFavorite: Bool, onFavoriteToggle: @escaping () -> Void, onClick: @escaping () -> Void) {
        self.business = business
        self.isFavorite = isFavorite
        self.onFavoriteToggle = onFavoriteToggle
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            HStack(alignment: .top, spacing: 12) {
                photo

                VStack(alignment: .leading, spacing: 2) {
                    HStack(alignment: .top, spacing: 4) {
                        Text(business.name)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.meTontBlack)
                            .lineLimit(1)
                        Spacer(minLength: 4)
                        HStack(spacing: 2) {
                            Image(systemName: "star.fill")
                                .font(.system(size: 11))
                                .foregroundColor(.meTontRed)
                            Text(String(format: "%.1f", business.rating))
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.meTontBlack)
                        }
                    }

                    Text(BusinessCategory.displayName(for: business.category))
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.meTontRed)

                    if business.isVerified || business.isAlbanianOwned || business.isEffectivelySponsored || business.isEffectivelyFeatured || business.isEffectivelyPremium {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 4) {
                                if business.isVerified {
                                    badgeChip(strings.verified, color: Color(red: 0x21 / 255.0, green: 0x96 / 255.0, blue: 0xF3 / 255.0))
                                }
                                if business.isAlbanianOwned {
                                    badgeChip(strings.albanianOwned, color: .meTontRed)
                                }
                                if business.isEffectivelySponsored {
                                    badgeChip(strings.sponsored, color: .tierGold)
                                } else if business.isEffectivelyFeatured {
                                    badgeChip(strings.featured2, color: .tierSilver)
                                } else if business.isEffectivelyPremium {
                                    badgeChip(strings.premium, color: .tierBronze)
                                }
                            }
                        }
                        .padding(.top, 2)
                    }

                    if let distanceText = distanceText {
                        HStack(spacing: 2) {
                            Image(systemName: "location.fill")
                                .font(.system(size: 10))
                                .foregroundColor(.meTontGrey)
                            Text(distanceText)
                                .font(.system(size: 12))
                                .foregroundColor(.meTontGrey)
                        }
                        .padding(.top, 2)
                    }
                }
            }
            .padding(10)
            .background(Color.white)
            .cornerRadius(14)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color(red: 0xF5 / 255.0, green: 0xD9 / 255.0, blue: 0xD9 / 255.0), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }

    private var photo: some View {
        ZStack(alignment: .topTrailing) {
            Group {
                if let photoUrl = business.photos.first, let url = URL(string: photoUrl) {
                    AsyncImage(url: url) { phase in
                        if let image = phase.image {
                            image.resizable().aspectRatio(contentMode: .fill)
                        } else {
                            placeholderPhoto
                        }
                    }
                } else {
                    placeholderPhoto
                }
            }
            .frame(width: 84, height: 84)
            .clipShape(RoundedRectangle(cornerRadius: 12))

            Button(action: onFavoriteToggle) {
                Image(systemName: isFavorite ? "heart.fill" : "heart")
                    .font(.system(size: 12))
                    .foregroundColor(.meTontRed)
                    .frame(width: 24, height: 24)
                    .background(Circle().fill(Color.white.opacity(0.9)))
            }
            .padding(2)
        }
    }

    private var placeholderPhoto: some View {
        ZStack {
            Color.meTontLightRed
            Image(systemName: "building.2.fill")
                .font(.system(size: 24))
                .foregroundColor(.meTontRed)
        }
    }

    private func badgeChip(_ label: String, color: Color) -> some View {
        Text(label)
            .font(.system(size: 10, weight: .bold))
            .foregroundColor(color)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(color.opacity(0.1))
            .cornerRadius(6)
    }

    private var distanceText: String? {
        guard let userLocation = LocationManager.shared.userLocation, let bizLocation = business.location else { return nil }
        let distanceKm = CLLocation(latitude: userLocation.latitude, longitude: userLocation.longitude)
            .distance(from: CLLocation(latitude: bizLocation.latitude, longitude: bizLocation.longitude)) / 1000.0
        if distanceKm < 1.0 {
            return "\(Int(distanceKm * 1000)) m away"
        } else {
            return String(format: "%.1f km away", distanceKm)
        }
    }
}
