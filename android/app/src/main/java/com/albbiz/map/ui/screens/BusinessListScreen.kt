// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.ui.LocalAppStrings
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessListScreen(
    onBackClick: () -> Unit,
    onBusinessClick: (String) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val businesses by viewModel.filteredBusinesses.collectAsState()
    val allBusinesses by viewModel.businesses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LocalAppStrings.current.directory) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── DISCOVERY SECTIONS (Only show if not searching) ──────
            if (searchQuery.isEmpty()) {
                item {
                    DiscoveryRow(
                        title = LocalAppStrings.current.featured,
                        businesses = allBusinesses.filter { it.isFeatured || it.isSponsored },
                        onBusinessClick = onBusinessClick
                    )
                }
                
                item {
                    DiscoveryRow(
                        title = LocalAppStrings.current.recentlyAdded,
                        businesses = allBusinesses.sortedByDescending { it.id }.take(5), // Simplified recent
                        onBusinessClick = onBusinessClick
                    )
                }
                
                item {
                    DiscoveryRow(
                        title = LocalAppStrings.current.topRated,
                        businesses = allBusinesses.sortedByDescending { it.rating }.take(5),
                        onBusinessClick = onBusinessClick
                    )
                }
                
                item {
                    Text(
                        LocalAppStrings.current.allBusinesses,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── FILTERS ───────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text(LocalAppStrings.current.searchPlaceholder) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )
            }

            item {
                val categories = listOf("All", "Restaurant", "Cafe", "Market", "Lawyer", "Contractor", "Other")
                var selectedCategory by remember { mutableStateOf("All") }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                                viewModel.onCategoryChange(if (category == "All") "" else category)
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }

            // ── BUSINESS LIST ─────────────────────────────────────
            if (businesses.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(LocalAppStrings.current.noResults, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(businesses) { business ->
                    BusinessListItem(
                        business = business,
                        userLocation = userLocation,
                        isFavorite = favoriteIds.contains(business.id),
                        onToggleFavorite = { viewModel.toggleFavorite(business.id) },
                        onClick = { onBusinessClick(business.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoveryRow(
    title: String,
    businesses: List<Business>,
    onBusinessClick: (String) -> Unit
) {
    if (businesses.isEmpty()) return
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(businesses) { business ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onBusinessClick(business.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            business.name,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            business.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                            Text(" ${business.rating}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessListItem(
    business: Business,
    userLocation: LatLng?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(business.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    // Badges
                    Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (business.isVerified) BadgeChip(LocalAppStrings.current.verified, Color(0xFF2196F3))
                        if (business.isAlbanianOwned) BadgeChip(LocalAppStrings.current.verified, Color(0xFFE41E20))
                        if (business.isPremium) BadgeChip(LocalAppStrings.current.verified, Color(0xFFFFAA00))
                    }
                }
                
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            Text(business.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Text(" ${business.rating} (${business.reviewCount} reviews)", style = MaterialTheme.typography.bodySmall)
            }

            Text(business.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)

            // Distance
            if (userLocation != null && business.location != null) {
                val distance = BusinessRepository().calculateDistance(
                    GeoPoint(userLocation.latitude, userLocation.longitude),
                    GeoPoint(business.location.latitude, business.location.longitude)
                )
                Text(
                    text = "📏 ${if (distance < 1.0) "${(distance * 1000).toInt()} m" else "%.1f km".format(distance)} away",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val uri = Uri.parse("google.navigation:q=${business.location?.latitude},${business.location?.longitude}&mode=d")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(LocalAppStrings.current.getDirections)
            }
        }
    }
}
