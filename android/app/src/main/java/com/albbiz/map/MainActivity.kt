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
import androidx.lifecycle.viewmodel.compose.viewModel
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
                                        navController.navigate(destination) {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // ── AUTH ──────────────────────────────────────────────
                            composable("auth") {
                                AuthScreen(
                                    onAuthSuccess = {
                                        navController.navigate("map") {
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
                                        navController.navigate("business_list?sortBy=default")
                                    },
                                    onAddBusinessClick = {
                                        navController.navigate("add_business")
                                    },
                                    onProfileClick = {
                                        navController.navigate("profile")
                                    },
                                    onFavoritesClick = {
                                        navController.navigate("favorites")
                                    },
                                    onEventsClick = {
                                        navController.navigate("events")
                                    },
                                    onAddStoryClick = {
                                        navController.navigate("add_story")
                                    },
                                    onStoryClick = { index: Int ->
                                        navController.navigate("story_viewer/$index")
                                    },
                                    onLogout = {
                                        navController.navigate("auth") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onBusinessClick = { businessId ->
                                        navController.navigate("business_detail/$businessId")
                                    },
                                    viewModel = mapViewModel,
                                    storiesViewModel = storiesViewModel,
                                    authViewModel = authViewModel
                                )
                            }

                            // ── ADD BUSINESS ──────────────────────────────────────
                            composable("add_business") {
                                AddBusinessScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onBusinessAdded = { navController.popBackStack() }
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
                                    onBackClick = { navController.popBackStack() },
                                    onBusinessClick = { businessId ->
                                        navController.navigate("business_detail/$businessId")
                                    },
                                    onNavigateToAuth = { navController.navigate("auth") },
                                    viewModel = mapViewModel,
                                    sortBy = sortBy
                                )
                            }

                            // ── PROFILE ───────────────────────────────────────────
                            composable("profile") {
                                UserProfileScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onLogout = {
                                        navController.navigate("auth") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onUpgradeClick = { navController.navigate("subscription") },
                                    onAdminClick = { navController.navigate("admin") },
                                    currentLanguage = currentLanguage,
                                    onLanguageChange = { currentLanguage = it },
                                    viewModel = authViewModel
                                )
                            }
                            // ── ADMIN ─────────────────────────────────────────────
                            composable("admin") {
                                AdminScreen(
                                    currentUserId = currentUserId,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // ── FAVORITES ─────────────────────────────────────────
                            composable("favorites") {
                                FavoritesScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onBusinessClick = { businessId ->
                                        navController.navigate("business_detail/$businessId")
                                    },
                                    viewModel = mapViewModel
                                )
                            }

                            // ── EVENTS ────────────────────────────────────────────
                            composable("events") {
                                EventsScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onAddEventClick = { navController.navigate("add_event") }
                                )
                            }

                            // ── ADD EVENT ─────────────────────────────────────────
                            composable("add_event") {
                                AddEventScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onEventAdded = { navController.popBackStack() }
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
                                                navController.navigate("auth")
                                            } else {
                                                navController.navigate("add_review/$businessId")
                                            }
                                        },
                                        onEditClick = {
                                            navController.navigate("edit_business/$businessId")
                                        },
                                        onBackClick = { navController.popBackStack() },
                                        onUpgradeClick = { navController.navigate("subscription") },
                                        onNavigateToAuth = { navController.navigate("auth") },
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
                                    onReviewSubmitted = { navController.popBackStack() }
                                )
                            }

                            // ── SUBSCRIPTION ──────────────────────────────────────
                            composable("subscription") {
                                SubscriptionScreen(
                                    onBackClick = { navController.popBackStack() }
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
                                        onBackClick = { navController.popBackStack() },
                                        onBusinessUpdated = { navController.popBackStack() }
                                    )
                                } else {
                                    Text("Business not found.")
                                }
                            }

                            // ── ADD STORY ─────────────────────────────────────────
                            composable("add_story") {
                                AddStoryScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onStoryPosted = { navController.popBackStack() },
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
                                            onClose = { navController.popBackStack() },
                                            onBusinessClick = { businessId ->
                                                navController.navigate("business_detail/$businessId")
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
                                            navController.popBackStack()
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