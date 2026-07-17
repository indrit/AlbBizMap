// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map

import android.os.Bundle
import androidx.activity.ComponentActivity
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

// This is the whole fix for the blank-screen bug: rapid taps (hamburger items, FABs,
// list rows, back navigation) were firing navigate()/popBackStack() multiple times
// before the previous one had settled, pushing duplicate/overlapping entries onto the
// back stack. That churn was what tore MapScreen's GoogleMap surface down mid-init and
// produced the blank/white screen — not anything about how or where the map itself was
// rendered. A simple time-based debounce plus a "destination fully resumed" check on
// every single navigate() and popBackStack() call in this file closes that gap at the
// source, everywhere, without needing to change how any screen (including the map) is
// built.
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
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentLanguage by remember { mutableStateOf(AppLanguage.EN) }

            AlbBizMapTheme {
                ProvideAppStrings(language = currentLanguage) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
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
                            composable("map") {
                                MapScreen(
                                    onListClick = {
                                        navController.navigateSafe("business_list?sortBy=default")
                                    },
                                    onAddBusinessClick = {
                                        navController.navigateSafe("add_business")
                                    },
                                    onProfileClick = {
                                        navController.navigateSafe("profile")
                                    },
                                    onFavoritesClick = {
                                        navController.navigateSafe("favorites")
                                    },
                                    onEventsClick = {
                                        navController.navigateSafe("events")
                                    },
                                    onAddStoryClick = {
                                        navController.navigateSafe("add_story")
                                    },
                                    onStoryClick = { index: Int ->
                                        navController.navigateSafe("story_viewer/$index")
                                    },
                                    onLogout = {
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
                            }

                            // ── ADD BUSINESS ──────────────────────────────────────
                            composable("add_business") {
                                AddBusinessScreen(
                                    onBackClick = { navController.popBackStackSafe() },
                                    onBusinessAdded = { navController.popBackStackSafe() }
                                )
                            }

                            // ── BUSINESS LIST ─────────────────────────────────────
                            composable(
                                route = "business_list?sortBy={sortBy}",
                                arguments = listOf(
                                    navArgument("sortBy") {
                                        type = NavType.StringType
                                        defaultValue = "default"
                                    }
                                )
                            ) { backStackEntry ->
                                val sortBy = backStackEntry.arguments?.getString("sortBy") ?: "default"
                                BusinessListScreen(
                                    onBackClick = { navController.popBackStackSafe() },
                                    onBusinessClick = { businessId ->
                                        navController.navigateSafe("business_detail/$businessId")
                                    },
                                    onNavigateToAuth = { navController.navigateSafe("auth") },
                                    viewModel = mapViewModel,
                                    sortBy = sortBy
                                )
                            }

                            // ── PROFILE ───────────────────────────────────────────
                            composable("profile") {
                                UserProfileScreen(
                                    onBackClick = { navController.popBackStackSafe() },
                                    onLogout = {
                                        navController.navigateSafe("auth") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onUpgradeClick = { navController.navigateSafe("subscription") },
                                    onAdminClick = { navController.navigateSafe("admin") },
                                    currentLanguage = currentLanguage,
                                    onLanguageChange = { currentLanguage = it },
                                    viewModel = authViewModel
                                )
                            }
                            // ── ADMIN ─────────────────────────────────────────────
                            composable("admin") {
                                AdminScreen(
                                    currentUserId = currentUserId,
                                    onBackClick = { navController.popBackStackSafe() }
                                )
                            }

                            // ── FAVORITES ─────────────────────────────────────────
                            composable("favorites") {
                                FavoritesScreen(
                                    onBackClick = { navController.popBackStackSafe() },
                                    onBusinessClick = { businessId ->
                                        navController.navigateSafe("business_detail/$businessId")
                                    },
                                    viewModel = mapViewModel
                                )
                            }

                            // ── EVENTS ────────────────────────────────────────────
                            composable("events") {
                                EventsScreen(
                                    onBackClick = { navController.popBackStackSafe() },
                                    onAddEventClick = { navController.navigateSafe("add_event") }
                                )
                            }

                            // ── ADD EVENT ─────────────────────────────────────────
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
