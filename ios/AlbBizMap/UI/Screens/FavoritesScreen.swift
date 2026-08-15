// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct FavoritesScreen: View {
    @Environment(\.appStrings) private var strings
    
    @ObservedObject public var viewModel: MapViewModel
    public let onBackClick: () -> Void
    public let onBusinessClick: (String) -> Void
    
    public init(viewModel: MapViewModel, onBackClick: @escaping () -> Void, onBusinessClick: @escaping (String) -> Void) {
        self.viewModel = viewModel
        self.onBackClick = onBackClick
        self.onBusinessClick = onBusinessClick
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.myFavorites)
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            let favoriteBusinesses = viewModel.businesses.filter { viewModel.favoriteIds.contains($0.id) }
            
            if favoriteBusinesses.isEmpty {
                VStack(spacing: 12) {
                    Spacer()
                    Image(systemName: "heart.slash").font(.system(size: 48)).foregroundColor(.gray)
                    Text(strings.noFavoritesYet).foregroundColor(.gray)
                    Spacer()
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(favoriteBusinesses) { biz in
                            BusinessCardView(
                                business: biz,
                                isFavorite: true,
                                onFavoriteToggle: {
                                    viewModel.toggleFavorite(businessId: biz.id, userId: AuthManager.shared.currentUser?.uid ?? "")
                                },
                                onClick: {
                                    onBusinessClick(biz.id)
                                }
                            )
                        }
                    }
                    .padding(16)
                }
            }
        }
        .background(Color.meTontBackground)
    }
}
