// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.ui.theme.TierBronze
import com.albbiz.map.ui.theme.TierGold
import com.albbiz.map.ui.theme.TierSilver
import com.google.android.gms.maps.model.LatLng
import com.albbiz.map.utils.AuthGate
import com.google.firebase.firestore.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessListScreen(
    onBackClick: () -> Unit,
    onBusinessClick: (String) -> Unit,
    onNavigateToAuth: () -> Unit = {},
    viewModel: MapViewModel = viewModel(),
    sortBy: String = "default"
) {
    val allBusinesses by viewModel.businesses.collectAsState()
    // Own independent copy of search state, not the map's — see the comment above
    // listSearchQuery in MapViewModel for why these used to be (wrongly) shared.
    val searchQuery by viewModel.listSearchQuery.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val strings = LocalAppStrings.current

    val filteredBusinesses by viewModel.listFilteredBusinesses.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val businesses = remember(filteredBusinesses, sortBy, userLocation) {
        when (sortBy) {
            "nearMe" -> {
                val location = userLocation
                if (location != null) {
                    val userPoint = com.google.firebase.firestore.GeoPoint(
                        location.latitude, location.longitude
                    )
                    filteredBusinesses.sortedBy {
                        it.location?.let { gp ->
                            com.albbiz.map.data.BusinessRepository().calculateDistance(userPoint, gp)
                        } ?: Double.MAX_VALUE
                    }
                } else filteredBusinesses
            }
            "topRated" -> filteredBusinesses.sortedByDescending { it.rating }
            else -> filteredBusinesses
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.directory,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MeTontRed
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                //.background(Color(0xFFFFF8F0))
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── DISCOVERY SECTIONS ────────────────────────────
                if (searchQuery.isEmpty()) {
                    item {
                        DiscoveryRow(
                            title = strings.featured,
                            businesses = allBusinesses.filter { it.isFeatured || it.isSponsored },
                            onBusinessClick = onBusinessClick
                        )
                    }
                    item {
                        DiscoveryRow(
                            title = strings.recentlyAdded,
                            businesses = allBusinesses.sortedByDescending { it.id }.take(5),
                            onBusinessClick = onBusinessClick
                        )
                    }
                    item {
                        DiscoveryRow(
                            title = strings.topRated,
                            businesses = allBusinesses.sortedByDescending { it.rating }.take(5),
                            onBusinessClick = onBusinessClick
                        )
                    }
                    item {
                        Text(
                            strings.allBusinesses,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 8.dp
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                // ── SEARCH BAR ────────────────────────────────────
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onListSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text(strings.searchPlaceholder) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = MeTontRed)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        singleLine = true
                    )
                }

                // ── CATEGORY FILTERS ──────────────────────────────
                item {
                    val categories = listOf(
                        "All", "Restaurant", "Cafe", "Market",
                        "Lawyer", "Contractor", "Other"
                    )
                    var selectedCategory by remember { mutableStateOf("All") }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    viewModel.onListCategoryChange(
                                        if (category == "All") "" else category
                                    )
                                },
                                label = { Text(category, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeTontRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // ── BUSINESS LIST ─────────────────────────────────
                if (businesses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MeTontRed.copy(alpha = 0.4f)
                                )
                                Text(
                                    strings.noResults,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MeTontGrey
                                )
                            }
                        }
                    }
                } else {
                    items(businesses) { business ->
                        BusinessListItem(
                            business = business,
                            userLocation = userLocation,
                            isFavorite = favoriteIds.contains(business.id),
                            // Was calling viewModel.toggleFavorite directly with no
                            // login check — MapViewModel.toggleFavorite silently no-ops
                            // for a guest (no uid to save against), so tapping this used
                            // to just do nothing with zero feedback. Same AuthGate
                            // pattern already used for this same action on the business
                            // detail screen.
                            onToggleFavorite = {
                                AuthGate.requireLogin(
                                    onNotLoggedIn = onNavigateToAuth,
                                    action = { viewModel.toggleFavorite(business.id) }
                                )
                            },
                            onToggleLike = {
                                AuthGate.requireLogin(
                                    onNotLoggedIn = onNavigateToAuth,
                                    action = { viewModel.toggleFavorite(business.id) }
                                )
                            },
                            onClick = { onBusinessClick(business.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(businesses) { business ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clickable { onBusinessClick(business.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Category color bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    MeTontRed,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            business.name,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            business.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MeTontRed,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                " ${business.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MeTontGrey
                            )
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
    onToggleLike: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        business.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        business.category,
                        color = MeTontRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (business.isVerified) BadgeChip(strings.verified, Color(0xFF2196F3))
                        if (business.isAlbanianOwned) BadgeChip(strings.albanianOwned, MeTontRed)
                        when {
                            business.isSponsored -> BadgeChip(strings.sponsored, TierGold)
                            business.isFeatured -> BadgeChip(strings.featured2, TierSilver)
                            business.isPremium -> BadgeChip(strings.premium, TierBronze)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ── LIKE BUTTON ───────────────────────────────────────
                    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    val isLiked = currentUserId != null && currentUserId in business.likedBy

                    IconButton(onClick = {
                        AuthGate.requireLogin(
                            onNotLoggedIn = onNavigateToAuth,
                            action = { onToggleLike() }
                        )
                    }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                            contentDescription = "Like",
                            tint = if (isLiked) MeTontRed else MeTontGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        "${business.likeCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MeTontGrey
                    )
                    // ── SHARE BUTTON ──────────────────────────────────────
                    IconButton(onClick = {
                        val shareText = "Check out ${business.name} on MeTont!\n${business.category} • ${business.address}"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MeTontGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // ── FAVORITE BUTTON ───────────────────────────────────
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            null,
                            tint = if (isFavorite) MeTontRed else MeTontGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    " ${business.rating} (${business.reviewCount} reviews)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MeTontGrey
                )
            }

            Text(
                business.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = Color.Black.copy(alpha = 0.7f)
            )

            // Distance
            if (userLocation != null && business.location != null) {
                val distance = BusinessRepository().calculateDistance(
                    GeoPoint(userLocation.latitude, userLocation.longitude),
                    GeoPoint(business.location.latitude, business.location.longitude)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = MeTontRed
                    )
                    Text(
                        " ${if (distance < 1.0) "${(distance * 1000).toInt()} m" else "%.1f km".format(distance)} away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val uri = Uri.parse(
                        "google.navigation:q=${business.location?.latitude},${business.location?.longitude}&mode=d"
                    )
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Directions,
                    null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.getDirections, fontWeight = FontWeight.Bold)
            }
        }
    }
}