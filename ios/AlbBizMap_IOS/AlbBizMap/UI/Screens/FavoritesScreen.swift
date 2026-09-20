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
            topAppBar

            let favoriteBusinesses = viewModel.businesses.filter { viewModel.favoriteIds.contains($0.id) }

            if favoriteBusinesses.isEmpty {
                emptyState
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("\(favoriteBusinesses.count) saved \(favoriteBusinesses.count == 1 ? "business" : "businesses")")
                            .font(.system(size: 13))
                            .foregroundColor(.meTontGrey)

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

    // Matches Android's red TopAppBar (FavoritesScreen.kt).
    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundColor(.white)
            }
            Text(strings.myFavorites)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Spacer()
            ZStack {
                Circle()
                    .fill(Color.meTontRed.opacity(0.1))
                    .frame(width: 80, height: 80)
                Image(systemName: "heart.fill")
                    .font(.system(size: 32))
                    .foregroundColor(.meTontRed.opacity(0.5))
            }
            Text(strings.noFavoritesYet)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.meTontGrey)
            Text("Tap the heart icon on any business to save it here")
                .font(.system(size: 13))
                .foregroundColor(.meTontGrey.opacity(0.7))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Spacer()
        }
    }
}
