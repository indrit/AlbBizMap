// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct BusinessListScreen: View {
    @Environment(\.appStrings) private var strings
    
    @ObservedObject public var viewModel: MapViewModel
    public let sortBy: String
    public let onBackClick: () -> Void
    public let onBusinessClick: (String) -> Void
    public let onNavigateToAuth: (() -> Void) -> Void
    
    public init(viewModel: MapViewModel, sortBy: String = "default", onBackClick: @escaping () -> Void, onBusinessClick: @escaping (String) -> Void, onNavigateToAuth: @escaping ((() -> Void)) -> Void) {
        self.viewModel = viewModel
        self.sortBy = sortBy
        self.onBackClick = onBackClick
        self.onBusinessClick = onBusinessClick
        self.onNavigateToAuth = onNavigateToAuth
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundColor(.meTontBlack)
                }
                Text(strings.directory)
                    .font(.title2)
                    .fontWeight(.bold)
                Spacer()
            }
            .padding()
            .background(Color.white)
            
            // Business list
            if viewModel.filteredBusinesses.isEmpty {
                VStack(spacing: 12) {
                    Spacer()
                    Image(systemName: "building.2.slash")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text(strings.noResults)
                        .font(.headline)
                        .foregroundColor(.gray)
                    Spacer()
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.filteredBusinesses) { biz in
                            BusinessCardView(
                                business: biz,
                                isFavorite: viewModel.favoriteIds.contains(biz.id),
                                onFavoriteToggle: {
                                    onNavigateToAuth {
                                        viewModel.toggleFavorite(businessId: biz.id, userId: AuthManager.shared.currentUser?.uid ?? "")
                                    }
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
        .background(Color.meTontBackground.edgesIgnoringSafeArea(.all))
    }
}
