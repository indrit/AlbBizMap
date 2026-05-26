// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onBusinessClick: (String) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val businesses by viewModel.businesses.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val favoriteBusinesses = businesses.filter { it.id in favoriteIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (favoriteBusinesses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "You haven't saved any businesses yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteBusinesses) { business ->
                    // Reuse the BusinessListItem component from BusinessListScreen
                    // or navigate to details on click
                    BusinessListItemWrapper(
                        business = business,
                        userLocation = userLocation,
                        onClick = { onBusinessClick(business.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BusinessListItemWrapper(
    business: com.albbiz.map.data.Business,
    userLocation: com.google.android.gms.maps.model.LatLng?,
    onClick: () -> Unit
) {
    // Note: We'll use the existing BusinessListItem component logic
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium
    ) {
        BusinessListItem(business = business, userLocation = userLocation)
    }
}
