// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct ContentView: View {
    @StateObject private var mapViewModel = MapViewModel()
    @StateObject private var authViewModel = AuthViewModel()
    @StateObject private var storiesViewModel = StoriesViewModel()
    
    @State private var currentLanguage: AppLanguage = .en
    
    // Navigation overlays state (matching MainActivity.kt architecture)
    @State private var isSplashScreenActive: Bool = true
    @State private var isDrawerOpen: Bool = false
    
    @State private var showFavoritesOverlay: Bool = false
    @State private var showProfileOverlay: Bool = false
    @State private var showEventsOverlay: Bool = false
    @State private var showAddBusinessOverlay: Bool = false
    @State private var showBusinessListOverlay: Bool = false
    @State private var showMyBusinessesOverlay: Bool = false
    @State private var showMyEventsOverlay: Bool = false
    @State private var showJobsOverlay: Bool = false
    @State private var showAdminOverlay: Bool = false
    @State private var showSubscriptionOverlay: Bool = false
    @State private var showAddEventOverlay: Bool = false
    @State private var showAddReviewOverlay: Bool = false
    @State private var showEditBusinessOverlay: Bool = false
    @State private var showAddStoryOverlay: Bool = false
    
    @State private var selectedBusinessId: String? = nil
    @State private var selectedStoryIndex: Int? = nil
    @State private var businessListSortBy: String = "default"
    
    @State private var showAuthOverlay: Bool = false
    @State private var showLoginPrompt: Bool = false
    @State private var pendingLoginAction: (() -> Void)? = nil
    
    public init() {}
    
    public var body: some View {
        let appStrings = AppStrings.forLanguage(currentLanguage)
        
        ZStack {
            if isSplashScreenActive {
                SplashScreen {
                    withAnimation {
                        isSplashScreenActive = false
                    }
                }
            } else {
                ZStack {
                    // Base Map View (Persistent map instance in background)
                    MapScreen(
                        viewModel: mapViewModel,
                        storiesViewModel: storiesViewModel,
                        authViewModel: authViewModel,
                        onOpenDrawer: { isDrawerOpen = true },
                        onListClick: { sortMode in
                            businessListSortBy = sortMode
                            showBusinessListOverlay = true
                        },
                        onAddBusinessClick: {
                            requireLoginOrPrompt { showAddBusinessOverlay = true }
                        },
                        onProfileClick: {
                            requireLoginOrPrompt { showProfileOverlay = true }
                        },
                        onFavoritesClick: {
                            requireLoginOrPrompt { showFavoritesOverlay = true }
                        },
                        onEventsClick: { showEventsOverlay = true },
                        onJobsClick: { showJobsOverlay = true },
                        onAddStoryClick: {
                            requireLoginOrPrompt { showAddStoryOverlay = true }
                        },
                        onStoryClick: { index in selectedStoryIndex = index },
                        onBusinessClick: { bizId in selectedBusinessId = bizId }
                    )
                    
                    // Overlays
                    if showFavoritesOverlay {
                        FavoritesScreen(
                            viewModel: mapViewModel,
                            onBackClick: { showFavoritesOverlay = false },
                            onBusinessClick: { bizId in selectedBusinessId = bizId }
                        )
                    }
                    
                    if showProfileOverlay {
                        UserProfileScreen(
                            viewModel: authViewModel,
                            mapViewModel: mapViewModel,
                            onBackClick: { showProfileOverlay = false },
                            onLogout: {
                                closeAllOverlays()
                                showAuthOverlay = true
                            },
                            onAdminClick: { showAdminOverlay = true },
                            onMyBusinessesClick: { showMyBusinessesOverlay = true },
                            onMyEventsClick: {
                                showProfileOverlay = false
                                showMyEventsOverlay = true
                            },
                            currentLanguage: currentLanguage,
                            onLanguageChange: { currentLanguage = $0 }
                        )
                    }
                    
                    if showAdminOverlay {
                        AdminScreen(
                            currentUserId: authViewModel.currentUser?.uid ?? "",
                            onBackClick: { showAdminOverlay = false }
                        )
                    }
                    
                    if showMyBusinessesOverlay {
                        MyBusinessesScreen(
                            viewModel: mapViewModel,
                            onBackClick: { showMyBusinessesOverlay = false },
                            onBusinessClick: { bizId in selectedBusinessId = bizId },
                            onAddBusinessClick: {
                                showMyBusinessesOverlay = false
                                showAddBusinessOverlay = true
                            }
                        )
                    }
                    
                    if showEventsOverlay {
                        EventsScreen(
                            onBackClick: { showEventsOverlay = false },
                            onAddEventClick: {
                                requireLoginOrPrompt { showAddEventOverlay = true }
                            }
                        )
                    }
                    
                    if showMyEventsOverlay {
                        MyEventsScreen(
                            onBackClick: { showMyEventsOverlay = false },
                            onAddEventClick: {
                                showMyEventsOverlay = false
                                requireLoginOrPrompt { showAddEventOverlay = true }
                            }
                        )
                    }
                    
                    if showAddEventOverlay {
                        AddEventScreen(
                            onBackClick: { showAddEventOverlay = false },
                            onEventAdded: { showAddEventOverlay = false }
                        )
                    }
                    
                    if showJobsOverlay {
                        JobsScreen(
                            viewModel: mapViewModel,
                            onBackClick: { showJobsOverlay = false },
                            onBusinessClick: { bizId in selectedBusinessId = bizId }
                        )
                    }
                    
                    if showAddBusinessOverlay {
                        AddBusinessScreen(
                            onBackClick: { showAddBusinessOverlay = false },
                            onBusinessAdded: { showAddBusinessOverlay = false }
                        )
                    }
                    
                    if showBusinessListOverlay {
                        BusinessListScreen(
                            viewModel: mapViewModel,
                            sortBy: businessListSortBy,
                            onBackClick: {
                                mapViewModel.resetListFilters()
                                showBusinessListOverlay = false
                            },
                            onBusinessClick: { bizId in selectedBusinessId = bizId },
                            onNavigateToAuth: { action in
                                pendingLoginAction = action
                                showLoginPrompt = true
                            }
                        )
                    }
                    
                    if showAddStoryOverlay {
                        AddStoryScreen(
                            onBackClick: { showAddStoryOverlay = false },
                            onStoryPosted: { showAddStoryOverlay = false },
                            mapViewModel: mapViewModel,
                            storiesViewModel: storiesViewModel
                        )
                    }
                    
                    if let storyIdx = selectedStoryIndex {
                        StoryViewerScreen(
                            stories: storiesViewModel.stories,
                            initialIndex: storyIdx,
                            onClose: { selectedStoryIndex = nil },
                            onBusinessClick: { bizId in
                                selectedStoryIndex = nil
                                selectedBusinessId = bizId
                            },
                            storiesViewModel: storiesViewModel
                        )
                    }
                    
                    if let bizId = selectedBusinessId {
                        if let biz = mapViewModel.businesses.first(where: { $0.id == bizId }) {
                            BusinessDetailScreen(
                                business: biz,
                                currentUserId: authViewModel.currentUser?.uid ?? "",
                                onWriteReviewClick: {
                                    requireLoginOrPrompt { showAddReviewOverlay = true }
                                },
                                onEditClick: { showEditBusinessOverlay = true },
                                onBackClick: { selectedBusinessId = nil },
                                onUpgradeClick: { showSubscriptionOverlay = true },
                                onNavigateToAuth: { action in
                                    pendingLoginAction = action
                                    showLoginPrompt = true
                                },
                                mapViewModel: mapViewModel
                            )
                            
                            if showAddReviewOverlay {
                                AddReviewScreen(
                                    businessId: bizId,
                                    onReviewSubmitted: { showAddReviewOverlay = false }
                                )
                            }
                            
                            if showEditBusinessOverlay {
                                EditBusinessScreen(
                                    business: biz,
                                    onBackClick: { showEditBusinessOverlay = false },
                                    onBusinessUpdated: { showEditBusinessOverlay = false }
                                )
                            }
                        }
                    }
                    
                    if showSubscriptionOverlay {
                        if let bizId = selectedBusinessId, let biz = mapViewModel.businesses.first(where: { $0.id == bizId }) {
                            SubscriptionScreen(
                                business: biz,
                                onBackClick: { showSubscriptionOverlay = false }
                            )
                        }
                    }
                    
                    if showAuthOverlay {
                        AuthScreen(
                            onAuthSuccess: {
                                showAuthOverlay = false
                                pendingLoginAction?()
                                pendingLoginAction = nil
                            },
                            currentLanguage: currentLanguage,
                            onLanguageChange: { currentLanguage = $0 },
                            viewModel: authViewModel
                        )
                    }
                    
                    AuthGatePrompt(
                        isPresented: showLoginPrompt,
                        onConfirm: {
                            showLoginPrompt = false
                            showAuthOverlay = true
                        },
                        onDismiss: {
                            showLoginPrompt = false
                            pendingLoginAction = nil
                        }
                    )
                    
                    // Side Drawer
                    DrawerMenuView(
                        isOpen: isDrawerOpen,
                        onClose: { isDrawerOpen = false },
                        onNavigate: { route in
                            switch route {
                            case "businessList": showBusinessListOverlay = true
                            case "addBusiness": requireLoginOrPrompt { showAddBusinessOverlay = true }
                            case "favorites": requireLoginOrPrompt { showFavoritesOverlay = true }
                            case "events": showEventsOverlay = true
                            case "jobs": showJobsOverlay = true
                            case "profile": requireLoginOrPrompt { showProfileOverlay = true }
                            default: break
                            }
                        },
                        currentLanguage: currentLanguage,
                        currentUserName: {
                            let fullName = [authViewModel.currentUser?.firstName, authViewModel.currentUser?.lastName]
                                .compactMap { $0 }
                                .filter { !$0.isEmpty }
                                .joined(separator: " ")
                            if !fullName.isEmpty { return fullName }
                            if let email = authViewModel.currentUser?.email, !email.isEmpty {
                                return String(email.split(separator: "@").first ?? "User")
                            }
                            return "User"
                        }(),
                        onLogout: {
                            closeAllOverlays()
                            authViewModel.logout()
                            showAuthOverlay = true
                        }
                    )
                }
            }
        }
        // Business Detail's own overlays (Write Review, Edit Business) only
        // got reset by its explicit back-arrow tap. Every other way of
        // opening a business — map pin, list, favorites, a story, a
        // recommendation card — just sets selectedBusinessId directly, so a
        // sheet left open would silently reappear on the next business
        // opened that way. Resetting on every change (including a switch to
        // a different business, not just back to none) closes that gap.
        .onChange(of: selectedBusinessId) { _, _ in
            showAddReviewOverlay = false
            showEditBusinessOverlay = false
        }
        .environment(\.appStrings, appStrings)
    }
    
    private func requireLoginOrPrompt(action: @escaping () -> Void) {
        AuthManager.shared.requireLogin(
            onNotLoggedIn: {
                pendingLoginAction = action
                showLoginPrompt = true
            },
            action: action
        )
    }
    
    private func closeAllOverlays() {
        showFavoritesOverlay = false
        showProfileOverlay = false
        showEventsOverlay = false
        showAddBusinessOverlay = false
        showBusinessListOverlay = false
        showMyBusinessesOverlay = false
        showMyEventsOverlay = false
        showJobsOverlay = false
        showAdminOverlay = false
        showSubscriptionOverlay = false
        showAddEventOverlay = false
        showAddReviewOverlay = false
        showEditBusinessOverlay = false
        showAddStoryOverlay = false
        selectedBusinessId = nil
        selectedStoryIndex = nil
    }
}
