// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct MyBusinessesScreen: View {
    @Environment(\.appStrings) private var strings
    
    @ObservedObject public var viewModel: MapViewModel
    public let onBackClick: () -> Void
    public let onBusinessClick: (String) -> Void
    public let onAddBusinessClick: () -> Void
    
    public init(viewModel: MapViewModel, onBackClick: @escaping () -> Void, onBusinessClick: @escaping (String) -> Void, onAddBusinessClick: @escaping () -> Void) {
        self.viewModel = viewModel
        self.onBackClick = onBackClick
        self.onBusinessClick = onBusinessClick
        self.onAddBusinessClick = onAddBusinessClick
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.myBusinesses)
                    .font(.title2).fontWeight(.bold)
                Spacer()
                Button(action: onAddBusinessClick) {
                    Image(systemName: "plus.circle.fill").font(.title2).foregroundColor(.meTontRed)
                }
            }
            .padding().background(Color.white)
            
            let uid = AuthManager.shared.currentUser?.uid ?? ""
            // Owner-scoped listener (FirestoreService.ownedBusinesses) rather than
            // filtering viewModel.businesses — the latter only ever holds active
            // businesses, so a business the owner deactivated via Edit Business'
            // Active Status toggle would otherwise vanish from here with no way
            // back to reactivate it.
            let myBizList = FirestoreService.shared.ownedBusinesses.filter { $0.ownerId == uid }
            
            if myBizList.isEmpty {
                VStack(spacing: 12) {
                    Spacer()
                    Image(systemName: "building.2").font(.system(size: 48)).foregroundColor(.gray)
                    Text(strings.noBusinessesYet).font(.headline)
                    Text(strings.noBusinessesYetSubtitle).font(.subheadline).foregroundColor(.gray)
                    Button(action: onAddBusinessClick) {
                        Text(strings.addBusiness)
                            .fontWeight(.bold)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 10)
                            .background(Color.meTontRed)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                    Spacer()
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(myBizList) { biz in
                            ZStack(alignment: .topTrailing) {
                                BusinessCardView(
                                    business: biz,
                                    isFavorite: viewModel.favoriteIds.contains(biz.id),
                                    onFavoriteToggle: {
                                        viewModel.toggleFavorite(businessId: biz.id, userId: uid)
                                    },
                                    onClick: {
                                        onBusinessClick(biz.id)
                                    }
                                )
                                // Owner-only visibility cue — the public map/list only
                                // ever show active businesses, so this only ever shows
                                // here, on the owner's own management screen.
                                if !biz.isActive {
                                    Text(strings.inactiveLabel)
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 3)
                                        .background(Color(red: 1, green: 0.651, blue: 0.149))
                                        .cornerRadius(8)
                                        .padding(.top, 8)
                                        .padding(.trailing, 20)
                                }
                            }
                        }
                    }
                    .padding(16)
                }
            }
        }
        .background(Color.meTontBackground)
    }
}
