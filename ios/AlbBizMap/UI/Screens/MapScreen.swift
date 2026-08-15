// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import MapKit

public struct MapScreen: View {
    @Environment(\.appStrings) private var strings
    
    @ObservedObject public var viewModel: MapViewModel
    @ObservedObject public var storiesViewModel: StoriesViewModel
    @ObservedObject public var authViewModel: AuthViewModel
    
    public let onOpenDrawer: () -> Void
    public let onListClick: (String) -> Void
    public let onAddBusinessClick: () -> Void
    public let onProfileClick: () -> Void
    public let onFavoritesClick: () -> Void
    public let onEventsClick: () -> Void
    public let onJobsClick: () -> Void
    public let onAddStoryClick: () -> Void
    public let onStoryClick: (Int) -> Void
    public let onBusinessClick: (String) -> Void
    
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 40.7128, longitude: -74.0060),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )
    
    public init(
        viewModel: MapViewModel,
        storiesViewModel: StoriesViewModel,
        authViewModel: AuthViewModel,
        onOpenDrawer: @escaping () -> Void,
        onListClick: @escaping (String) -> Void,
        onAddBusinessClick: @escaping () -> Void,
        onProfileClick: @escaping () -> Void,
        onFavoritesClick: @escaping () -> Void,
        onEventsClick: @escaping () -> Void,
        onJobsClick: @escaping () -> Void,
        onAddStoryClick: @escaping () -> Void,
        onStoryClick: @escaping (Int) -> Void,
        onBusinessClick: @escaping (String) -> Void
    ) {
        self.viewModel = viewModel
        self.storiesViewModel = storiesViewModel
        self.authViewModel = authViewModel
        self.onOpenDrawer = onOpenDrawer
        self.onListClick = onListClick
        self.onAddBusinessClick = onAddBusinessClick
        self.onProfileClick = onProfileClick
        self.onFavoritesClick = onFavoritesClick
        self.onEventsClick = onEventsClick
        self.onJobsClick = onJobsClick
        self.onAddStoryClick = onAddStoryClick
        self.onStoryClick = onStoryClick
        self.onBusinessClick = onBusinessClick
    }
    
    public var body: some View {
        ZStack(alignment: .top) {
            // Native MapKit Map
            Map(coordinateRegion: $region, annotationItems: viewModel.filteredBusinesses) { biz in
                MapAnnotation(coordinate: biz.location?.coordinate ?? CLLocationCoordinate2D(latitude: 40.7128, longitude: -74.0060)) {
                    Button(action: {
                        onBusinessClick(biz.id)
                    }) {
                        VStack(spacing: 2) {
                            ZStack {
                                Circle().fill(biz.isSponsored ? Color.meTontGold : Color.meTontRed)
                                    .frame(width: 36, height: 36)
                                Image(systemName: "mappin.circle.fill")
                                    .foregroundColor(.white)
                                    .font(.system(size: 24))
                            }
                            Text(biz.name)
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.white)
                                .cornerRadius(6)
                                .shadow(radius: 2)
                        }
                    }
                }
            }
            .edgesIgnoringSafeArea(.all)
            
            // Top Control Bar Overlay
            VStack(spacing: 8) {
                // Search Bar & Drawer Trigger
                HStack(spacing: 12) {
                    Button(action: onOpenDrawer) {
                        Image(systemName: "line.3.horizontal")
                            .font(.title2)
                            .foregroundColor(.meTontBlack)
                            .padding(10)
                            .background(Circle().fill(.white).shadow(radius: 3))
                    }
                    
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.gray)
                        TextField(strings.searchPlaceholder, text: $viewModel.searchQuery)
                        if !viewModel.searchQuery.isEmpty {
                            Button(action: { viewModel.searchQuery = "" }) {
                                Image(systemName: "xmark.circle.fill").foregroundColor(.gray)
                            }
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .background(RoundedRectangle(cornerRadius: 25).fill(.white).shadow(radius: 3))
                    
                    Button(action: { onListClick("default") }) {
                        Image(systemName: "list.bullet")
                            .font(.title3)
                            .foregroundColor(.meTontRed)
                            .padding(10)
                            .background(Circle().fill(.white).shadow(radius: 3))
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 50)
                
                // Story Bar
                StoryBarView(
                    stories: storiesViewModel.stories,
                    currentUserId: authViewModel.currentUser?.uid ?? "",
                    onAddStoryClick: onAddStoryClick,
                    onStoryClick: onStoryClick
                )
                
                // Category Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(BusinessCategory.allCases) { cat in
                            CategoryChipView(
                                category: cat,
                                isSelected: viewModel.selectedCategory == cat
                            ) {
                                if viewModel.selectedCategory == cat {
                                    viewModel.selectedCategory = nil
                                } else {
                                    viewModel.selectedCategory = cat
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
            }
        }
    }
}
