// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Matches Android's MapBusinessCard: a compact 220pt-wide horizontal card used for
// "Community Announcements" (events) and "Most Favorited Worldwide" rows in the
// map screen's bottom sheet.
public struct MapBusinessCard: View {
    public let business: Business
    public let onClick: () -> Void

    public init(business: Business, onClick: @escaping () -> Void) {
        self.business = business
        self.onClick = onClick
    }

    private var accentColor: Color {
        if business.isSponsored { return .tierGold }
        if business.isFeatured { return .tierSilver }
        if business.isPremium { return .tierBronze }
        return .meTontRed
    }

    public var body: some View {
        Button(action: onClick) {
            HStack(spacing: 10) {
                thumbnail
                    .frame(width: 44, height: 44)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 2) {
                    Text(business.name)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.meTontBlack)
                        .lineLimit(1)
                    Text(business.category)
                        .font(.system(size: 11))
                        .foregroundColor(accentColor)
                    HStack(spacing: 2) {
                        Image(systemName: "star.fill")
                            .font(.system(size: 10))
                            .foregroundColor(Color(red: 0xFF / 255.0, green: 0xC1 / 255.0, blue: 0x07 / 255.0))
                        Text(String(format: "%.1f", business.rating))
                            .font(.system(size: 11))
                            .foregroundColor(.meTontGrey)
                    }
                }
                Spacer(minLength: 0)
            }
            .padding(12)
            .frame(width: 220)
            .background(Color.white)
            .cornerRadius(14)
            .shadow(color: Color.black.opacity(0.08), radius: 2, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private var thumbnail: some View {
        if let urlString = business.photos.first, let url = URL(string: urlString) {
            AsyncImage(url: url) { phase in
                if let image = phase.image {
                    image.resizable().aspectRatio(contentMode: .fill)
                } else {
                    placeholderThumbnail
                }
            }
        } else {
            placeholderThumbnail
        }
    }

    private var placeholderThumbnail: some View {
        ZStack {
            accentColor.opacity(0.12)
            Image(systemName: BusinessCategory.allCases.first { $0.rawValue.lowercased() == business.category.lowercased() }?.iconName ?? "briefcase.fill")
                .font(.system(size: 18))
                .foregroundColor(accentColor)
        }
    }
}

// Small card for a community event, matching Android's inline event Card in the
// "Community Announcements" LazyRow (icon + title/category/location).
public struct EventAnnouncementCard: View {
    public let event: Event
    public let onClick: () -> Void

    public init(event: Event, onClick: @escaping () -> Void) {
        self.event = event
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            HStack(spacing: 10) {
                ZStack {
                    Circle().fill(Color.meTontRed.opacity(0.1))
                    Image(systemName: "calendar")
                        .font(.system(size: 16))
                        .foregroundColor(.meTontRed)
                }
                .frame(width: 40, height: 40)

                VStack(alignment: .leading, spacing: 2) {
                    Text(event.title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.meTontBlack)
                        .lineLimit(1)
                    Text(event.category)
                        .font(.system(size: 11))
                        .foregroundColor(.meTontRed)
                    HStack(spacing: 2) {
                        Image(systemName: "mappin")
                            .font(.system(size: 10))
                            .foregroundColor(.meTontGrey)
                        Text(event.locationName)
                            .font(.system(size: 11))
                            .foregroundColor(.meTontGrey)
                            .lineLimit(1)
                    }
                }
                Spacer(minLength: 0)
            }
            .padding(12)
            .frame(width: 220)
            .background(Color.white)
            .cornerRadius(14)
            .shadow(color: Color.black.opacity(0.08), radius: 2, x: 0, y: 1)
        }
        .buttonStyle(.plain)
    }
}
