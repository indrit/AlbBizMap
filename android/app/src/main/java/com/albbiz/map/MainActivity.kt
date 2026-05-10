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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlbBizMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val mapViewModel: MapViewModel = viewModel()

                    // ← REAL AUTH: shared across whole app
                    val authViewModel: AuthViewModel = viewModel()
                    val currentUser by authViewModel.currentUser.collectAsState()

                    // Convenient shortcuts
                    val currentUserId = currentUser?.uid ?: ""
                    val currentUserName = currentUser?.email?.substringBefore("@") ?: "User"

                    // ← Start on auth screen if not logged in, map if logged in
                    val startDestination = if (authViewModel.isLoggedIn()) "map" else "auth"

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {

                        // ── AUTH ──────────────────────────────────────
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

                        // ── MAP ───────────────────────────────────────
                        composable("map") {
                            MapScreen(
                                onListClick = { navController.navigate("business_list") },
                                onAddBusinessClick = { navController.navigate("add_business") },
                                onBusinessClick = { businessId ->
                                    navController.navigate("business_detail/$businessId")
                                },
                                viewModel = mapViewModel
                            )
                        }

                        // ── ADD BUSINESS ──────────────────────────────
                        composable("add_business") {
                            AddBusinessScreen(
                                onBackClick = { navController.popBackStack() },
                                onBusinessAdded = { navController.popBackStack() }
                            )
                        }

                        // ── BUSINESS LIST ─────────────────────────────
                        composable("business_list") {
                            BusinessListScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        // ── BUSINESS DETAIL ───────────────────────────
                        composable(
                            route = "business_detail/{businessId}",
                            arguments = listOf(navArgument("businessId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                            val business = mapViewModel.getBusinessById(businessId)

                            if (business != null) {
                                BusinessDetailScreen(
                                    business = business,
                                    currentUserId = currentUserId, // ← REAL user ID
                                    onWriteReviewClick = {
                                        if (currentUserId.isEmpty()) {
                                            // Not logged in → send to auth
                                            navController.navigate("auth")
                                        } else {
                                            navController.navigate("add_review/$businessId")
                                        }
                                    },
                                    onEditClick = {
                                        navController.navigate("edit_business/$businessId")
                                    },
                                   onBackClick = { navController.popBackStack() }
                                )
                            } else {
                                Text("Business not found.")
                            }
                        }

                        // ── ADD REVIEW ────────────────────────────────
                        composable(
                            route = "add_review/{businessId}",
                            arguments = listOf(navArgument("businessId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                            AddReviewScreen(
                                businessId = businessId,
                                userId = currentUserId,     // ← REAL user ID
                                userName = currentUserName, // ← REAL user name
                                onReviewSubmitted = { navController.popBackStack() }
                            )
                        }

                        // ── EDIT BUSINESS ─────────────────────────────
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