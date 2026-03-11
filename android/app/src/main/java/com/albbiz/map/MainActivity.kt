// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map

import com.albbiz.map.ui.screens.UserProfileScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.albbiz.map.ui.screens.AddBusinessScreen
import com.albbiz.map.ui.screens.AuthScreen
import com.albbiz.map.ui.screens.BusinessListScreen
import com.albbiz.map.ui.screens.MapScreen
import com.albbiz.map.ui.theme.AlbBizMapTheme
import com.google.firebase.auth.FirebaseAuth

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
                    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                        "map"
                    } else {
                        "auth"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("auth") {
                            AuthScreen(
                                onAuthSuccess = {
                                    navController.navigate("map") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("map") {
                            MapScreen(
                                onListClick = {
                                    navController.navigate("business_list")
                                },
                                onAddBusinessClick = {
                                    navController.navigate("add_business")
                                },
                                onProfileClick = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                        composable("add_business") {
                            AddBusinessScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onBusinessAdded = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("business_list") {
                            BusinessListScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("profile") {
                            UserProfileScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onLogout = {
                                    navController.navigate("auth") {
                                        popUpTo("map") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}