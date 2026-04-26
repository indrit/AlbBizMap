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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albbiz.map.ui.screens.*
import com.albbiz.map.ui.theme.AlbBizMapTheme

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

                    NavHost(
                        navController = navController,
                        startDestination = "map"
                    ) {
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

                        composable("add_business") {
                            AddBusinessScreen(
                                onBackClick = { navController.popBackStack() },
                                onBusinessAdded = { navController.popBackStack() }
                            )
                        }

                        composable("business_list") {
                            BusinessListScreen(
                                onBackClick = { navController.popBackStack() }
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
                                    currentUserId = "CURRENT_USER_ID", // TODO: replace with real auth
                                    onWriteReviewClick = {
                                        navController.navigate("add_review/$businessId")
                                    },
                                    onEditClick = {
                                        navController.navigate("edit_business/$businessId")
                                    }
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
                                userId = "CURRENT_USER_ID", // TODO: Replace with actual Auth logic
                                userName = "CURRENT_USER_NAME", // TODO: Replace with actual Auth logic
                                onReviewSubmitted = { navController.popBackStack() }
                            )
                        }

                        // ← NEW: Edit business route
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