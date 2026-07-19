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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albbiz.map.ui.AppLanguage
import com.albbiz.map.ui.ProvideAppStrings
import com.albbiz.map.ui.screens.*
import com.albbiz.map.ui.theme.AlbBizMapTheme
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
                            composable("splash") {
                                SplashScreen(
                                    onSplashFinished = {
                                        val destination = if (authViewModel.isLoggedIn()) "map" else "auth"
                                        navController.navigateSafe(destination) {
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
                            // Favorites and Profile render as overlays inside this same
                            // NavHost entry instead of as separate destinations. A System
                            // Trace of the reported "blank screen on rapid hamburger
                            // clicks" bug found the real cost: navigating to "favorites"
                            // or "profile" as a NavHost destination fully disposes the
                            // "map" composable (and with it GoogleMap's underlying
                            // SurfaceView), which then has to be torn down and
                            // reconstructed from scratch — a genuinely expensive,
                            // synchronous main-thread operation — every single time the
                            // user comes back. That reconstruction was landing exactly in
                            // the window where rapid taps arrived. Keeping "map" as one
                            // NavHost entry for the whole logged-in session, and just
                            // toggling every hamburger-drawer destination (Favorites,
                            // Profile, Events, Add Business, List View) on top of it as
                            // plain Compose state, means GoogleMap is created once and
                            // never rebuilt for any of these round trips. Deeper
                            // destinations reached from within those overlays (Admin,
                            // Subscription, Add Event, business detail, etc.) stay as
                            // regular NavHost destinations — they're not reached directly
                            // from the drawer, so they're not part of the rapid-tap
                            // pattern this is fixing.
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

                                // System back button closes whichever overlay is open
                                // (innermost first) instead of falling through to
                                // MapScreen's own back handling (which is for the
                                // drawer, not these overlays).
                                BackHandler(
                                    enabled = showFavoritesOverlay || showProfileOverlay ||
                                        showEventsOverlay || showAddBusinessOverlay ||
                                        showBusinessListOverlay || showAdminOverlay
                                ) {
                                    if (showAdminOverlay) {
                                        showAdminOverlay = false
                                    } else {
                                        showFavoritesOverlay = false
                                        showProfileOverlay = false
                                        showEventsOverlay = false
                                        showAddBusinessOverlay = false
                                        showBusinessListOverlay = false
                                    }
                                }

                                Box(modifier = Modifier.fillMaxSize()) {
                                    MapScreen(
                                        onListClick = { showBusinessListOverlay = true },
                                        onAddBusinessClick = { showAddBusinessOverlay = true },
                                        onProfileClick = { showProfileOverlay = true },
                                        onFavoritesClick = { showFavoritesOverlay = true },
                                        onEventsClick = { showEventsOverlay = true },
                                        onAddStoryClick = {
                                            navController.navigateSafe("add_story")
                                        },
                                        onStoryClick = { index: Int ->
                                            navController.navigateSafe("story_viewer/$index")
                                        },
                                        onLogout = {
                                            showFavoritesOverlay = false
                                            showProfileOverlay = false
                                            showEventsOverlay = false
                                            showAddBusinessOverlay = false
                                            showBusinessListOverlay = false
                                            showAdminOverlay = false
                                            navController.navigateSafe("auth") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        onBusinessClick = { businessId ->
                                            navController.navigateSafe("business_detail/$businessId")
                                        },
                                        viewModel = mapViewModel,
                                        storiesViewModel = storiesViewModel,
                                        authViewModel = authViewModel
                                    )

                                    if (showFavoritesOverlay) {
                                        FavoritesScreen(
                                            onBackClick = { showFavoritesOverlay = false },
                                            onBusinessClick = { businessId ->
                                                navController.navigateSafe("business_detail/$businessId")
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
                                                showBusinessListOverlay = false
                                                showAdminOverlay = false
                                                navController.navigateSafe("auth") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            },
                                            onUpgradeClick = { navController.navigateSafe("subscription") },
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
                                            onAddEventClick = { navController.navigateSafe("add_event") }
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
                                            onBackClick = { showBusinessListOverlay = false },
                                            onBusinessClick = { businessId ->
                                                navController.navigateSafe("business_detail/$businessId")
                                            },
                                            onNavigateToAuth = { navController.navigateSafe("auth") },
                                            viewModel = mapViewModel,
                                            sortBy = "default"
                                        )
                                    }
                                }
                            }

                            // ── ADD EVENT ─────────────────────────────────────────
                            // Popping back here lands on "map" with the Events overlay
                            // flag still set, so Events reappears — same pattern as
                            // Admin returning to the Profile overlay above.
                            composable("add_event") {
                                AddEventScreen(
                                    onBackClick = { navController.popBackStackSafe() },
                                    onEventAdded = { navController.popBackStackSafe() }
                                )
                            }

                            // ── BUSINESS DETAIL ───────────────────────────────────
                            composable(
                                route = "business_detail/{businessId}",
                                arguments = listOf(
                                    navArgument("businessId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                                val business = mapViewModel.getBusinessById(businessId)

                                if (business != null) {
                                    BusinessDetailScreen(
                                        business = business,
                                        currentUserId = currentUserId,
                                        onWriteReviewClick = {
                                            if (currentUserId.isEmpty()) {
                                                navController.navigateSafe("auth")
                                            } else {
                                                navController.navigateSafe("add_review/$businessId")
                                            }
                                        },
                                        onEditClick = {
                                            navController.navigateSafe("edit_business/$businessId")
                                        },
                                        onBackClick = { navController.popBackStackSafe() },
                                        onUpgradeClick = { navController.navigateSafe("subscription") },
                                        onNavigateToAuth = { navController.navigateSafe("auth") },
                                        mapViewModel = mapViewModel
                                    )
                                } else {
                                    Text("Business not found.")
                                }
                            }

                            // ── ADD REVIEW ────────────────────────────────────────
                            composable(
                                route = "add_review/{businessId}",
                                arguments = listOf(
                                    navArgument("businessId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                                AddReviewScreen(
                                    businessId = businessId,
                                    onReviewSubmitted = { navController.popBackStackSafe() }
                                )
                            }

                            // ── SUBSCRIPTION ──────────────────────────────────────
                            composable("subscription") {
                                SubscriptionScreen(
                                    onBackClick = { navController.popBackStackSafe() }
                                )
                            }

                            // ── EDIT BUSINESS ─────────────────────────────────────
                            composable(
                                route = "edit_business/{businessId}",
                                arguments = listOf(
                                    navArgument("businessId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                                val business = mapViewModel.getBusinessById(businessId)

                                if (business != null) {
                                    EditBusinessScreen(
                                        business = business,
                                        onBackClick = { navController.popBackStackSafe() },
                                        onBusinessUpdated = { navController.popBackStackSafe() }
                                    )
                                } else {
                                    Text("Business not found.")
                                }
                            }

                            // ── ADD STORY ─────────────────────────────────────────
                            composable("add_story") {
                                AddStoryScreen(
                                    onBackClick = { navController.popBackStackSafe() },
                                    onStoryPosted = { navController.popBackStackSafe() },
                                    mapViewModel = mapViewModel
                                )
                            }

                            // ── STORY VIEWER ──────────────────────────────────────
                            composable(
                                route = "story_viewer/{storyIndex}",
                                arguments = listOf(
                                    navArgument("storyIndex") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val storyIndex = backStackEntry.arguments?.getInt("storyIndex") ?: 0
                                val stories by storiesViewModel.stories.collectAsState()
                                val isLoading by storiesViewModel.isLoading.collectAsState()

                                when {
                                    stories.isNotEmpty() -> {
                                        StoryViewerScreen(
                                            stories = stories,
                                            initialIndex = storyIndex.coerceIn(0, stories.lastIndex),
                                            onClose = { navController.popBackStackSafe() },
                                            onBusinessClick = { businessId ->
                                                navController.navigateSafe("business_detail/$businessId")
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
                                        // Stories finished loading but came back empty (or list changed
                                        // out from under us, e.g. expired) — bail out instead of
                                        // rendering a blank screen.
                                        LaunchedEffect(Unit) {
                                            navController.popBackStackSafe()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
