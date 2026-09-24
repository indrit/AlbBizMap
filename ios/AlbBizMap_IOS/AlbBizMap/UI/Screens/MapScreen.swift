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
    
    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 41.3275, longitude: 19.8187),
            span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
        )
    )
    @State private var showSearch: Bool = false
    // Recenters the camera on the user's real location the first time it
    // becomes available, then leaves it alone -- same one-shot behavior as
    // Android's `hasMovedToInitialLocation` flag, so panning around the map
    // afterward doesn't keep getting yanked back to the user's position.
    @State private var hasMovedToInitialLocation: Bool = false
    
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
        VStack(spacing: 0) {
            topAppBar
            
            ZStack(alignment: .top) {
                // Native MapKit Map (iOS 17+ MapContentBuilder API)
                Map(position: $cameraPosition) {
                    // Businesses with no resolved location are skipped rather than
                    // pinned at a fallback coordinate -- a fallback like Apple Park
                    // or NYC looks like a real, legitimate pin instead of the "this
                    // business has no location" signal it should be. Matches
                    // Android's MapScreen, which builds its cluster items from
                    // `businesses.mapNotNull { it.location?.let { ... } }` -- i.e.
                    // it never renders a marker for a business with no location either.
                    ForEach(viewModel.filteredBusinesses.filter { $0.location != nil }) { biz in
                        Annotation(biz.name, coordinate: biz.location!.coordinate) {
                            Button(action: {
                                onBusinessClick(biz.id)
                            }) {
                                VStack(spacing: 2) {
                                    ZStack {
                                        Circle().fill(biz.isEffectivelySponsored ? Color.meTontGold : Color.meTontRed)
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
                }
                .edgesIgnoringSafeArea(.bottom)
                
                VStack(spacing: 0) {
                    if showSearch {
                        searchBar
                    } else {
                        categoryFilterBar
                    }
                }

                // FAB buttons — matches Android's Add Business + My Location FABs,
                // stacked above the bottom sheet's peek height (140).
                //
                // IMPORTANT: this used to be a VStack directly given
                // `.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)`.
                // That expanded the VStack's own frame to the ENTIRE screen, and in practice
                // that full expanded frame was intercepting touches everywhere on screen (not
                // just where the two visible circular buttons are drawn) because it sits above
                // the bottom sheet in z-order — this was silently swallowing every tap on the
                // story bar, business cards, and the sheet's drag handle, with zero errors or
                // logs, which matches exactly what was being reported.
                //
                // Fix: use a hit-testing-disabled Color.clear to claim the layout space, and
                // place the buttons in a `.overlay` on top of it. `.overlay` content is a
                // separate layer that does NOT inherit `.allowsHitTesting(false)` from its
                // base view, so only the two actual button circles remain tappable — the rest
                // of the screen is no longer blocked by this layer at all.
                Color.clear
                    .allowsHitTesting(false)
                    .overlay(alignment: .bottomTrailing) {
                        VStack(spacing: 12) {
                            Button(action: onAddBusinessClick) {
                                Image(systemName: "plus")
                                    .font(.system(size: 20, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(width: 52, height: 52)
                                    .background(Color.meTontRed)
                                    .clipShape(Circle())
                                    .shadow(color: .black.opacity(0.25), radius: 4, x: 0, y: 2)
                            }
                            Button(action: {
                                // Falls back to Tirana when location isn't available yet,
                                // matching Android's TIRANA_LOCATION fallback.
                                let target = LocationManager.shared.userLocation
                                    ?? CLLocationCoordinate2D(latitude: 41.3275, longitude: 19.8187)
                                withAnimation {
                                    cameraPosition = .region(
                                        MKCoordinateRegion(
                                            center: target,
                                            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                                        )
                                    )
                                }
                            }) {
                                Image(systemName: "location.fill")
                                    .font(.system(size: 18, weight: .semibold))
                                    .foregroundColor(.meTontRed)
                                    .frame(width: 52, height: 52)
                                    .background(Color.white)
                                    .clipShape(Circle())
                                    .shadow(color: .black.opacity(0.25), radius: 4, x: 0, y: 2)
                            }
                        }
                        .padding(.trailing, 16)
                        .padding(.bottom, 140)
                    }
            }
        }
        .edgesIgnoringSafeArea(.bottom)
        // Switched from a hand-rolled DragGesture + .offset() sheet to the system's
        // own native sheet with presentationDetents (the same mechanism Apple Maps
        // itself uses for its bottom card). After several rounds of chasing distinct
        // custom-gesture bugs one at a time in that implementation (drag-release
        // flicker, per-frame render cost, a dead zone on reversal) and still not
        // getting a fully smooth feel, this replaces the whole class of problems at
        // once: dragging, snapping and the handle are all implemented natively in
        // UIKit, not hand-tuned SwiftUI gesture code.
        //
        // isPresented: .constant(true) keeps it permanently shown (never dismissible)
        // -- matches the old sheet's behavior of always being present on this screen.
        // presentationBackgroundInteraction(.enabled) is what keeps the map and FAB
        // buttons tappable/pannable regardless of the sheet's current height, same as
        // before. Heights (140 / 45% / 92%) match the old peekHeight/mediumFraction/
        // largeFraction exactly, so the snap behavior itself is unchanged.
        .sheet(isPresented: .constant(true)) {
            MapBottomSheetContent(
                viewModel: viewModel,
                storiesViewModel: storiesViewModel,
                authViewModel: authViewModel,
                onAddStoryClick: onAddStoryClick,
                onStoryClick: onStoryClick,
                onBusinessClick: onBusinessClick,
                onEventsClick: onEventsClick,
                onMostFavoritedSeeMore: { onListClick("mostFavorited") }
            )
            .presentationDetents([.height(140), .fraction(0.45), .fraction(0.92)])
            .presentationDragIndicator(.visible)
            .presentationBackgroundInteraction(.enabled)
            .presentationCornerRadius(20)
            .presentationBackground(Color.white)
            .interactiveDismissDisabled(true)
        }
        .onReceive(LocationManager.shared.$userLocation) { newLocation in
            recenterOnUserLocationIfNeeded(newLocation)
        }
        .onAppear {
            recenterOnUserLocationIfNeeded(LocationManager.shared.userLocation)
        }
    }

    // Moves the camera to the user's real location the first time it's known,
    // matching Android's MapScreen LaunchedEffect(userLocation) behavior: only
    // acts once (guarded by hasMovedToInitialLocation) so it doesn't fight the
    // user panning the map afterward, and ignores the Simulator's default
    // "Apple" preset location (Apple Park) the same way Android ignores the
    // Android Emulator's default Google HQ location -- neither is a real
    // tester location, so recentering on it isn't useful.
    private func recenterOnUserLocationIfNeeded(_ location: CLLocationCoordinate2D?) {
        guard !hasMovedToInitialLocation, let location else { return }
        let isAppleParkSimulatorDefault = (37.32...37.35).contains(location.latitude)
            && (-122.02...(-122.00)).contains(location.longitude)
        guard !isAppleParkSimulatorDefault else { return }

        hasMovedToInitialLocation = true
        withAnimation {
            cameraPosition = .region(
                MKCoordinateRegion(
                    center: location,
                    span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                )
            )
        }
    }
    
    // Solid red top app bar, matching Android's TopAppBar (hamburger, title, search toggle).
    private var topAppBar: some View {
        HStack(spacing: 12) {
            Button(action: onOpenDrawer) {
                Image(systemName: "line.3.horizontal")
                    .font(.title3)
                    .foregroundColor(.white)
            }
            
            Text(strings.appName)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.white)
            
            Spacer()
            
            Button(action: {
                showSearch.toggle()
                if !showSearch { viewModel.searchQuery = "" }
            }) {
                Image(systemName: showSearch ? "xmark" : "magnifyingglass")
                    .font(.title3)
                    .foregroundColor(.white)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }
    
    // Flat white shadowed bar of category filters, matching Android's Card + FilterChip row.
    private var categoryFilterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                categoryChip(title: strings.all, isSelected: viewModel.selectedCategory == nil) {
                    viewModel.selectedCategory = nil
                }
                ForEach(BusinessCategory.allCases) { cat in
                    categoryChip(title: cat.displayName, isSelected: viewModel.selectedCategory == cat) {
                        viewModel.selectedCategory = (viewModel.selectedCategory == cat) ? nil : cat
                    }
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 8)
        }
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.12), radius: 4, x: 0, y: 2)
    }
    
    private func categoryChip(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 12, weight: .medium))
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(isSelected ? Color.meTontRed : Color.meTontLightGrey)
                .foregroundColor(isSelected ? .white : .meTontBlack)
                .cornerRadius(16)
        }
    }
    
    private var searchBar: some View {
        VStack(spacing: 0) {
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.meTontRed)
                TextField(strings.searchPlaceholder, text: $viewModel.searchQuery)
                if !viewModel.searchQuery.isEmpty {
                    Button(action: { viewModel.searchQuery = "" }) {
                        Image(systemName: "xmark.circle.fill").foregroundColor(.gray)
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(Color.white)
            
            if !viewModel.searchQuery.isEmpty {
                if viewModel.filteredBusinesses.isEmpty {
                    Text(strings.noSearchResults)
                        .font(.subheadline)
                        .foregroundColor(.meTontGrey)
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.white)
                } else {
                    ScrollView {
                        VStack(spacing: 0) {
                            ForEach(viewModel.filteredBusinesses.prefix(8)) { biz in
                                Button(action: {
                                    onBusinessClick(biz.id)
                                    showSearch = false
                                    viewModel.searchQuery = ""
                                }) {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(biz.name)
                                            .foregroundColor(.meTontBlack)
                                            .fontWeight(.medium)
                                        Text(BusinessCategory.displayName(for: biz.category))
                                            .font(.caption)
                                            .foregroundColor(.meTontRed)
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 10)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                Divider().padding(.leading, 16)
                            }
                        }
                    }
                    .background(Color.white)
                    .frame(maxHeight: 300)
                }
            }
        }
        .shadow(color: Color.black.opacity(0.12), radius: 4, x: 0, y: 2)
    }
}
