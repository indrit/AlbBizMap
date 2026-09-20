// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Matches Android's FeaturedPickCard: a tall photo card used for "Top Recommended"
// and "Near You" in the map screen's bottom sheet. 150pt photo with a tier badge
// overlay, then name / category / rating below.
public struct FeaturedPickCard: View {
    @Environment(\.appStrings) private var strings
    public let business: Business
    public let onClick: () -> Void

    public init(business: Business, onClick: @escaping () -> Void) {
        self.business = business
        self.onClick = onClick
    }

    private var tierColor: Color? {
        if business.isSponsored { return .tierGold }
        if business.isFeatured { return .tierSilver }
        if business.isPremium { return .tierBronze }
        return nil
    }

    private var tierLabel: String? {
        if business.isSponsored { return strings.sponsored }
        if business.isFeatured { return strings.featured2 }
        if business.isPremium { return strings.premium }
        return nil
    }

    public var body: some View {
        Button(action: onClick) {
            VStack(spacing: 0) {
                ZStack(alignment: .topLeading) {
                    photo
                        .frame(height: 150)
                        .frame(maxWidth: .infinity)
                        .clipped()

                    if let tierColor, let tierLabel {
                        HStack(spacing: 4) {
                            Image(systemName: "star.fill")
                                .font(.system(size: 10))
                            Text(tierLabel.uppercased())
                                .font(.system(size: 10, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(tierColor)
                        .cornerRadius(6)
                        .padding(10)
                    }
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(business.name)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.meTontBlack)
                        .lineLimit(1)
                    Text(business.category.uppercased())
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(.meTontRed)
                    HStack(spacing: 2) {
                        Image(systemName: "star.fill")
                            .font(.system(size: 12))
                            .foregroundColor(Color(red: 0xFF / 255.0, green: 0xC1 / 255.0, blue: 0x07 / 255.0))
                        Text(String(format: "%.1f", business.rating))
                            .font(.system(size: 13))
                            .foregroundColor(.meTontGrey)
                    }
                    .padding(.top, 2)
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .background(Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color(red: 0xF5 / 255.0, green: 0xD9 / 255.0, blue: 0xD9 / 255.0), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private var photo: some View {
        if let urlString = business.photos.first, let url = URL(string: urlString) {
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

    private var placeholderPhoto: some View {
        ZStack {
            Color(red: 0xFB / 255.0, green: 0xEA / 255.0, blue: 0xEA / 255.0)
            Image(systemName: BusinessCategory.allCases.first { $0.rawValue.lowercased() == business.category.lowercased() }?.iconName ?? "briefcase.fill")
                .font(.system(size: 36))
                .foregroundColor(.meTontRed)
        }
    }
}
