// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albbiz.map.ui.screens.*
import com.albbiz.map.ui.theme.AlbBizMapTheme
import com.albbiz.map.viewmodel.AuthViewModel
import com.albbiz.map.ui.AppLanguage
import com.albbiz.map.ui.ProvideAppStrings
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Remove default splash screen
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
                        val currentUser by authViewModel.currentUser.collectAsState()

                        val currentUserId = currentUser?.uid ?: ""
                        val currentUserName = currentUser?.email?.substringBefore("@") ?: "User"

                        val startDestination = "splash"

                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
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

                            composable("auth") {
                                AuthScreen(
                                    onAuthSuccess = {
                                        navController.navigate("map") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    },
                                    viewModel = authViewModel
                                )
                            }

                            composable("map") {
                                MapScreen(
                                    onListClick = { navController.navigate("business_list") },
                                    onAddBusinessClick = { navController.navigate("add_business") },
                                    onProfileClick = { navController.navigate("profile") },
                                    onFavoritesClick = { navController.navigate("favorites") },
                                    onEventsClick = { navController.navigate("events") },
                                    onLogout = {
                                        navController.navigate("auth") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    currentUserName = currentUserName,

                                    onBusinessClick = { businessId ->
                                        navController.navigate("business_detail/$businessId")
                                    },
                                    viewModel = mapViewModel
                                )
                            }

                            composable("add_business") {
                                AddBusinessScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onBusinessAdded = { navController.popBackStack() }
                                )
                            }

                            composable("business_list") {
                                BusinessListScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onBusinessClick = { businessId ->
                                        navController.navigate("business_detail/$businessId")
                                    },
                                    viewModel = mapViewModel
                                )
                            }

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

                            composable("admin") {
                                AdminScreen(
                                    currentUserId = currentUserId,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable("favorites") {
                                FavoritesScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onBusinessClick = { businessId ->
                                        navController.navigate("business_detail/$businessId")
                                    },
                                    viewModel = mapViewModel
                                )
                            }

                            composable("events") {
                                EventsScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onAddEventClick = { navController.navigate("add_event") }
                                )
                            }

                            composable("add_event") {
                                AddEventScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onEventAdded = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "business_detail/{businessId}",
                                arguments = listOf(navArgument("businessId") { type = NavType.StringType })
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

                            composable(
                                route = "add_review/{businessId}",
                                arguments = listOf(navArgument("businessId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                                AddReviewScreen(
                                    businessId = businessId,
                                    userId = currentUserId,
                                    userName = currentUserName,
                                    onReviewSubmitted = { navController.popBackStack() }
                                )
                            }

                            composable("subscription") {
                                SubscriptionScreen(
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "edit_business/{businessId}",
                                arguments = listOf(navArgument("businessId") { type = NavType.StringType })
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
                        }
                    }
                }
            }
        }
    }
}