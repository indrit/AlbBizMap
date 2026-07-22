// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.albbiz.map.ui.AppLanguage
import com.albbiz.map.ui.ProvideAppStrings
import com.albbiz.map.ui.screens.*
import com.albbiz.map.ui.theme.AlbBizMapTheme
import com.albbiz.map.utils.AuthGate
import com.albbiz.map.viewmodel.AuthViewModel
import com.albbiz.map.viewmodel.StoriesViewModel
import com.google.android.gms.maps.MapsInitializer

// The navigation debounce below closes one real gap (rapid taps firing overlapping
// navigate()/popBackStack() calls before the previous one settled). It turned out not
// to be the whole story, though: a System Trace of an actual blank-screen freeze showed
// the main thread stuck for 5+ seconds inside Maps SDK's own
// PhoenixGoogleMapActivityEnvironment.getMap(), which was dynamically loading and
// verifying dex classes from Play Services' MapsCoreDynamite module — called 91 times
// across one ~35s session. That module should be loaded once and cached for the app's
// whole lifetime; calling MapsInitializer.initialize() this early, synchronously,
// before any GoogleMap composable can possibly be created, is what locks that module
// preference in up front instead of leaving it to whichever screen happens to create
// the first GoogleMap.
private var lastNavigationTime = 0L
private const val NAVIGATION_DEBOUNCE_MS = 500L

private fun NavController.navigateSafe(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    val now = System.currentTimeMillis()
    if (now - lastNavigationTime < NAVIGATION_DEBOUNCE_MS) return
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        lastNavigationTime = now
        navigate(route, builder)
    }
}

private fun NavController.popBackStackSafe(): Boolean {
    val now = System.currentTimeMillis()
    if (now - lastNavigationTime < NAVIGATION_DEBOUNCE_MS) return false
    lastNavigationTime = now
    return popBackStack()
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Must happen before setContent — see the comment above navigateSafe for why.
        // Logging the callback's actual renderer because LATEST can silently fall back
        // to LEGACY on some devices/Play Services versions — if that's happening here,
        // the map is still SurfaceView-backed under the hood, which is the other prime
        // suspect for freezes when something animates over the map (e.g. a drawer scrim).
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { renderer ->
            android.util.Log.d("AlbBizMap", "Maps renderer initialized: $renderer")
        }

        setContent {
            var currentLanguage by remember { mutableStateOf(AppLanguage.EN) }

            AlbBizMapTheme {
                ProvideAppStrings(language = currentLanguage) {
                    Surface(
                        // testTagsAsResourceId lets UiAutomator (used by the Baseline
                        // Profile macrobenchmark test) find composables tagged with
                        // Modifier.testTag() elsewhere in the app via By.res("tag").
                        // Without this, UiAutomator can only find things by visible
                        // text/contentDescription, which breaks across languages.
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics { testTagsAsResourceId = true },
                        color = Color.Transparent
                    ) {
                        val navController = rememberNavController()
                        val mapViewModel: MapViewModel = viewModel()
                        val authViewModel: AuthViewModel = viewModel()
                        val storiesViewModel: StoriesViewModel = viewModel()
                        val currentUser by authViewModel.currentUser.collectAsState()

                        val currentUserId = currentUser?.uid ?: ""

                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                            // ── SPLASH ────────────────────────────────────────────
                            // Always lands on "map" now, logged in or not — browsing the
                            // map, business listings, business detail pages, events, and
                            // job postings is public. Individual actions that need an
                            // account (writing a review, adding/editing a business,
                            // posting a job or story, favoriting, viewing your profile)
                            // are gated one at a time via AuthGate.requireLogin at the
                            // point where the user actually tries to do them, instead of
                            // blocking everyone at the door before they've seen anything.
                            composable("splash") {
                                SplashScreen(
                                    onSplashFinished = {
                                        navController.navigateSafe("map") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // ── AUTH ──────────────────────────────────────────────
                            composable("auth") {
                                AuthScreen(
                                    onAuthSuccess = {
                                        navController.navigateSafe("map") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    },
                                    currentLanguage = currentLanguage,
                                    onLanguageChange = { currentLanguage = it },
                                    viewModel = authViewModel
                                )
                            }

                            // ── MAP ───────────────────────────────────────────────
                            // Every other logged-in screen (Favorites, Profile, Events,
                            // Add Business, List View, Admin, business detail,
                            // Subscription, Add Event, Write a Review, Edit Business,
                            // Add Story, Story Viewer) renders as an overlay inside this
                            // one NavHost entry instead of as a separate destination. A
                            // System Trace of the reported "blank screen" bug found the
                            // real cost: navigating to any of these as their own NavHost
                            // destination fully disposes the "map" composable (and with
                            // it GoogleMap's underlying SurfaceView), which then has to
                            // be torn down and reconstructed from scratch — a genuinely
                            // expensive, synchronous main-thread operation — every
                            // single time the user comes back. That reconstruction was
                            // landing exactly in the window where rapid taps (hamburger
                            // clicks, story taps, etc.) arrived. Keeping "map" as one
                            // NavHost entry for the entire logged-in session, and
                            // toggling everything else on top of it as plain Compose
                            // state, means GoogleMap is created once at login and never
                            // rebuilt again until logout.
                            composable("map") {
                                var showFavoritesOverlay by remember { mutableStateOf(false) }
                                var showProfileOverlay by remember { mutableStateOf(false) }
                                // Same reasoning as Favorites/Profile above — Events,
                                // Add Business and List View are all reached straight
                                // from the hamburger drawer, so they're just as likely
                                // to get bounced into and out of rapidly. Keeping them
                                // as overlays here too means the map never gets rebuilt
                                // for any drawer item, not just two of them.
                                var showEventsOverlay by remember { mutableStateOf(false) }
                                var showAddBusinessOverlay by remember { mutableStateOf(false) }
                                var showBusinessListOverlay by remember { mutableStateOf(false) }
                                var showJobsOverlay by remember { mutableStateOf(false) }
                                // Admin is one level deeper (reached from inside the
                                // Profile overlay), but it's still the same underlying
                                // cost — navigating to it as a real NavHost destination
                                // disposes "map" (and GoogleMap) just like Favorites used
                                // to. Same fix: it's a Compose flag layered on top of
                                // Profile instead of a nav route, so "back" from Admin
                                // just closes this flag and Profile is still sitting
                                // there underneath, exactly as if Admin had been popped
                                // back to Profile in a real nav stack.
                                var showAdminOverlay by remember { mutableStateOf(false) }
                                // "View Full Profile" on a map marker's preview sheet,
                                // and business taps from the Favorites/List View
                                // overlays, all used to navigate to a real
                                // "business_detail/{id}" NavHost destination — which
                                // disposes "map" (and GoogleMap) exactly like the
                                // drawer items used to, just reached by a different
                                // button. Same fix: hold the tapped business's id as
                                // local state and render it as an overlay instead.
                                var selectedBusinessId by remember { mutableStateOf<String?>(null) }
                                // The remaining pieces reached from inside these
                                // overlays (Subscription, Add Event, Write a Review,
                                // Edit Business) plus Add Story / Story Viewer off the
                                // map itself — same cost, same fix. Stories in
                                // particular get tapped through quickly in normal use,
                                // so they're just as exposed as the drawer items were.
                                var showSubscriptionOverlay by remember { mutableStateOf(false) }
                                var showAddEventOverlay by remember { mutableStateOf(false) }
                                var showAddReviewOverlay by remember { mutableStateOf(false) }
                                var showEditBusinessOverlay by remember { mutableStateOf(false) }
                                var showAddStoryOverlay by remember { mutableStateOf(false) }
                                var selectedStoryIndex by remember { mutableStateOf<Int?>(null) }

                                // System back button closes whichever overlay is open,
                                // innermost/most-recently-opened first, instead of
                                // falling through to MapScreen's own back handling
                                // (which is for the drawer, not these overlays).
                                BackHandler(
                                    enabled = showFavoritesOverlay || showProfileOverlay ||
                                        showEventsOverlay || showAddBusinessOverlay ||
                                        showBusinessListOverlay || showJobsOverlay || showAdminOverlay ||
                                        selectedBusinessId != null || showSubscriptionOverlay ||
                                        showAddEventOverlay || showAddReviewOverlay ||
                                        showEditBusinessOverlay || showAddStoryOverlay ||
                                        selectedStoryIndex != null
                                ) {
                                    when {
                                        showAddReviewOverlay -> showAddReviewOverlay = false
                                        showEditBusinessOverlay -> showEditBusinessOverlay = false
                                        showSubscriptionOverlay -> showSubscriptionOverlay = false
                                        showAddEventOverlay -> showAddEventOverlay = false
                                        selectedBusinessId != null -> selectedBusinessId = null
                                        showAdminOverlay -> showAdminOverlay = false
                                        selectedStoryIndex != null -> selectedStoryIndex = null
                                        showAddStoryOverlay -> showAddStoryOverlay = false
                                        else -> {
                                            showFavoritesOverlay = false
                                            showProfileOverlay = false
                                            showEventsOverlay = false
                                            showAddBusinessOverlay = false
                                            if (showBusinessListOverlay) mapViewModel.resetListFilters()
                                            showBusinessListOverlay = false
                                            showJobsOverlay = false
                                        }
                                    }
                                }

                                Box(modifier = Modifier.fillMaxSize()) {
                                    MapScreen(
                                        onListClick = { showBusinessListOverlay = true },
                                        onAddBusinessClick = {
                                            AuthGate.requireLogin(
                                                onNotLoggedIn = { navController.navigateSafe("auth") },
                                                action = { showAddBusinessOverlay = true }
                                            )
                                        },
                                        // Profile and Favorites are both inherently
                                        // account-bound — there's no meaningful "guest"
                                        // version of either (an empty favorites list, a
                                        // blank profile with a logout button that does
                                        // nothing), so tapping them sends a guest
                                        // straight to login instead of opening a
                                        // screen with nothing real to show.
                                        onProfileClick = {
                                            AuthGate.requireLogin(
                                                onNotLoggedIn = { navController.navigateSafe("auth") },
                                                action = { showProfileOverlay = true }
                                            )
                                        },
                                        onFavoritesClick = {
                                            AuthGate.requireLogin(
                                                onNotLoggedIn = { navController.navigateSafe("auth") },
                                                action = { showFavoritesOverlay = true }
                                            )
                                        },
                                        onEventsClick = { showEventsOverlay = true },
                                        onJobsClick = { showJobsOverlay = true },
                                        onAddStoryClick = {
                                            AuthGate.requireLogin(
                                                onNotLoggedIn = { navController.navigateSafe("auth") },
                                                action = { showAddStoryOverlay = true }
                                            )
                                        },
                                        onStoryClick = { index: Int -> selectedStoryIndex = index },
                                        onLogout = {
                                            showFavoritesOverlay = false
                                            showProfileOverlay = false
                                            showEventsOverlay = false
                                            showAddBusinessOverlay = false
                                            mapViewModel.resetListFilters()
                                            showBusinessListOverlay = false
                                            showJobsOverlay = false
                                            showAdminOverlay = false
                                            selectedBusinessId = null
                                            showSubscriptionOverlay = false
                                            showAddEventOverlay = false
                                            showAddReviewOverlay = false
                                            showEditBusinessOverlay = false
                                            showAddStoryOverlay = false
                                            selectedStoryIndex = null
                                            navController.navigateSafe("auth") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        onBusinessClick = { businessId ->
                                            selectedBusinessId = businessId
                                        },
                                        viewModel = mapViewModel,
                                        storiesViewModel = storiesViewModel,
                                        authViewModel = authViewModel
                                    )

                                    if (showFavoritesOverlay) {
                                        FavoritesScreen(
                                            onBackClick = { showFavoritesOverlay = false },
                                            onBusinessClick = { businessId ->
                                                selectedBusinessId = businessId
                                            },
                                            viewModel = mapViewModel
                                        )
                                    }

                                    if (showProfileOverlay) {
                                        UserProfileScreen(
                                            onBackClick = { showProfileOverlay = false },
                                            onLogout = {
                                                showFavoritesOverlay = false
                                                showProfileOverlay = false
                                                showEventsOverlay = false
                                                showAddBusinessOverlay = false
                                                mapViewModel.resetListFilters()
                                                showBusinessListOverlay = false
                                                showJobsOverlay = false
                                                showAdminOverlay = false
                                                selectedBusinessId = null
                                                showSubscriptionOverlay = false
                                                showAddEventOverlay = false
                                                showAddReviewOverlay = false
                                                showEditBusinessOverlay = false
                                                showAddStoryOverlay = false
                                                selectedStoryIndex = null
                                                navController.navigateSafe("auth") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            },
                                            onUpgradeClick = { showSubscriptionOverlay = true },
                                            onAdminClick = { showAdminOverlay = true },
                                            currentLanguage = currentLanguage,
                                            onLanguageChange = { currentLanguage = it },
                                            viewModel = authViewModel
                                        )
                                    }

                                    // Drawn after (on top of) Profile so it visually
                                    // layers above it, matching "Admin pushed on top of
                                    // Profile" from the old nav-stack behavior.
                                    if (showAdminOverlay) {
                                        AdminScreen(
                                            currentUserId = currentUserId,
                                            onBackClick = { showAdminOverlay = false }
                                        )
                                    }

                                    if (showEventsOverlay) {
                                        EventsScreen(
                                            onBackClick = { showEventsOverlay = false },
                                            onAddEventClick = {
                                                AuthGate.requireLogin(
                                                    onNotLoggedIn = { navController.navigateSafe("auth") },
                                                    action = { showAddEventOverlay = true }
                                                )
                                            }
                                        )
                                    }

                                    // Drawn after Events so it layers above it, same as
                                    // Admin layers above Profile.
                                    if (showAddEventOverlay) {
                                        AddEventScreen(
                                            onBackClick = { showAddEventOverlay = false },
                                            onEventAdded = { showAddEventOverlay = false }
                                        )
                                    }

                                    if (showJobsOverlay) {
                                        JobsScreen(
                                            onBackClick = { showJobsOverlay = false },
                                            onBusinessClick = { businessId ->
                                                selectedBusinessId = businessId
                                            },
                                            viewModel = mapViewModel
                                        )
                                    }

                                    if (showAddBusinessOverlay) {
                                        AddBusinessScreen(
                                            onBackClick = { showAddBusinessOverlay = false },
                                            onBusinessAdded = { showAddBusinessOverlay = false }
                                        )
                                    }

                                    if (showBusinessListOverlay) {
                                        BusinessListScreen(
                                            onBackClick = {
                                                mapViewModel.resetListFilters()
                                                showBusinessListOverlay = false
                                            },
                                            onBusinessClick = { businessId ->
                                                selectedBusinessId = businessId
                                            },
                                            onNavigateToAuth = { navController.navigateSafe("auth") },
                                            viewModel = mapViewModel,
                                            sortBy = "default"
                                        )
                                    }

                                    if (showAddStoryOverlay) {
                                        AddStoryScreen(
                                            onBackClick = { showAddStoryOverlay = false },
                                            onStoryPosted = { showAddStoryOverlay = false },
                                            mapViewModel = mapViewModel,
                                            storiesViewModel = storiesViewModel
                                        )
                                    }

                                    // Stories get tapped through quickly in normal use,
                                    // same reasoning as everything else here — it needs
                                    // to sit on top of the map without leaving it.
                                    selectedStoryIndex?.let { storyIndex ->
                                        val stories by storiesViewModel.stories.collectAsState()
                                        val isLoading by storiesViewModel.isLoading.collectAsState()

                                        when {
                                            stories.isNotEmpty() -> {
                                                StoryViewerScreen(
                                                    stories = stories,
                                                    initialIndex = storyIndex.coerceIn(0, stories.lastIndex),
                                                    onClose = { selectedStoryIndex = null },
                                                    onBusinessClick = { businessId ->
                                                        selectedStoryIndex = null
                                                        selectedBusinessId = businessId
                                                    },
                                                    storiesViewModel = storiesViewModel
                                                )
                                            }
                                            isLoading -> {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator()
                                                }
                                            }
                                            else -> {
                                                // Stories finished loading but came back empty (or
                                                // list changed out from under us, e.g. expired) —
                                                // bail out instead of rendering a blank overlay.
                                                LaunchedEffect(Unit) {
                                                    selectedStoryIndex = null
                                                }
                                            }
                                        }
                                    }

                                    // Drawn after Favorites/List View/Story Viewer so it
                                    // layers above whichever of those a business can be
                                    // opened from.
                                    selectedBusinessId?.let { businessId ->
                                        val business = mapViewModel.getBusinessById(businessId)
                                        if (business != null) {
                                            BusinessDetailScreen(
                                                business = business,
                                                currentUserId = currentUserId,
                                                onWriteReviewClick = {
                                                    if (currentUserId.isEmpty()) {
                                                        navController.navigateSafe("auth")
                                                    } else {
                                                        showAddReviewOverlay = true
                                                    }
                                                },
                                                onEditClick = { showEditBusinessOverlay = true },
                                                onBackClick = { selectedBusinessId = null },
                                                onUpgradeClick = { showSubscriptionOverlay = true },
                                                onNavigateToAuth = { navController.navigateSafe("auth") },
                                                mapViewModel = mapViewModel
                                            )

                                            // Layered above Business Detail so "back"
                                            // closes them first and reveals it again.
                                            if (showAddReviewOverlay) {
                                                AddReviewScreen(
                                                    businessId = businessId,
                                                    onReviewSubmitted = { showAddReviewOverlay = false }
                                                )
                                            }

                                            if (showEditBusinessOverlay) {
                                                EditBusinessScreen(
                                                    business = business,
                                                    onBackClick = { showEditBusinessOverlay = false },
                                                    onBusinessUpdated = { showEditBusinessOverlay = false }
                                                )
                                            }
                                        } else {
                                            Text("Business not found.")
                                        }
                                    }

                                    // Reachable from both Profile and Business Detail,
                                    // so it's drawn last — above either one.
                                    if (showSubscriptionOverlay) {
                                        SubscriptionScreen(
                                            onBackClick = { showSubscriptionOverlay = false }
                                        )
                                    }
                                }
                            }

                            // Add Event, Business Detail, Add Review, Subscription, Edit
                            // Business, Add Story and Story Viewer all used to be real
                            // NavHost destinations here. They're now overlays rendered
                            // inside the "map" destination above (see the comments
                            // there), so GoogleMap never gets disposed and rebuilt for
                            // any of them, no matter how they're reached.
                        }
                    }
                }
            }
        }
    }
}
