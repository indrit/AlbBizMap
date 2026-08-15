// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct BusinessCardView: View {
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
            HStack(spacing: 12) {
                // Thumbnail photo
                if let photoUrl = business.photos.first, let url = URL(string: photoUrl) {
                    AsyncImage(url: url) { img in
                        img.resizable().aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Rectangle().fill(Color.meTontRed.opacity(0.1))
                    }
                    .frame(width: 84, height: 84)
                    .cornerRadius(10)
                } else {
                    ZStack {
                        RoundedRectangle(cornerRadius: 10).fill(Color.meTontRed.opacity(0.1))
                        Image(systemName: "building.2.fill")
                            .font(.title2)
                            .foregroundColor(.meTontRed)
                    }
                    .frame(width: 84, height: 84)
                }
                
                // Info
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(business.name)
                            .font(.headline)
                            .foregroundColor(.meTontBlack)
                            .lineLimit(1)
                        
                        if business.isVerified {
                            Image(systemName: "checkmark.seal.fill")
                                .foregroundColor(.blue)
                                .font(.caption)
                        }
                        
                        Spacer()
                        
                        Button(action: onFavoriteToggle) {
                            Image(systemName: isFavorite ? "heart.fill" : "heart")
                                .foregroundColor(isFavorite ? .meTontRed : .gray)
                        }
                    }
                    
                    Text(business.category)
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundColor(.meTontRed)
                    
                    Text(business.description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                    
                    HStack(spacing: 8) {
                        HStack(spacing: 2) {
                            Image(systemName: "star.fill")
                                .foregroundColor(.orange)
                                .font(.caption2)
                            Text(String(format: "%.1f", business.rating))
                                .font(.caption2)
                                .fontWeight(.bold)
                            Text("(\(business.reviewCount))")
                                .font(.caption2)
                                .foregroundColor(.gray)
                        }
                        
                        if business.isAlbanianOwned {
                            Text("🇦🇱 Albanian Owned")
                                .font(.system(size: 10))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.meTontLightRed)
                                .foregroundColor(.meTontRed)
                                .cornerRadius(4)
                        }
                    }
                }
            }
            .padding(12)
            .background(Color.white)
            .cornerRadius(14)
            .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(PlainButtonStyle())
    }
}
