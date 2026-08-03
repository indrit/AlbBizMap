// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontLightRed
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
    // Takes the action that was blocked by the login gate, so the caller (MainActivity)
    // can remember it and run it automatically the moment login succeeds, instead of
    // the user having to retap the same button after coming back from the login screen.
    onNavigateToAuth: (() -> Unit) -> Unit = {},
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
    val selectedCategories by viewModel.listSelectedCategories.collectAsState()
    val selectedCountries by viewModel.listSelectedCountries.collectAsState()
    val selectedCities by viewModel.listSelectedCities.collectAsState()
    val availableCountries by viewModel.availableCountries.collectAsState()
    val availableCities by viewModel.availableCities.collectAsState()
    // Country/City used to always be expanded, which pushed the actual business
    // list three filter rows down. Now they're tucked behind this toggle (the
    // "Filters" button next to "All Businesses") and only take up space when the
    // user actually wants them — same idea as the collapsible filter panels in
    // most directory/marketplace apps.
    var showLocationFilters by remember { mutableStateOf(false) }

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
            "mostFavorited" -> filteredBusinesses.sortedByDescending { it.likeCount }
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
        },
        // Only present while the Country/City panel is open — its job is just to
        // collapse that panel back (the actual filtering already happened live as
        // chips were tapped, same as Category), giving the user a clear "I'm done"
        // action instead of having to scroll back up to the Filters button.
        bottomBar = {
            if (showLocationFilters) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { showLocationFilters = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeTontRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text(strings.applyFilters, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 8.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                strings.allBusinesses,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            // Only worth showing once there's actually a Country/City
                            // to filter by — matches the same availableCountries guard
                            // the collapsible section below uses.
                            if (availableCountries.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { showLocationFilters = !showLocationFilters },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (showLocationFilters) MeTontRed else Color.Black
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (showLocationFilters) MeTontRed else Color(0xFFDADCE0)
                                    )
                                ) {
                                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val activeCount = selectedCountries.size + selectedCities.size
                                    Text(
                                        if (activeCount > 0) "Filters ($activeCount)" else "Filters",
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
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
                // Multi-select, same as Country/City below — "All" clears the set,
                // each other chip toggles independently so e.g. Restaurant + Cafe
                // can both be active at once.
                item {
                    val categories = listOf(
                        "Restaurant", "Cafe", "Market",
                        "Lawyer", "Contractor", "Other"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategories.isEmpty(),
                            onClick = { viewModel.onListCategoryClearAll() },
                            label = { Text(strings.allFilterOption, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MeTontRed,
                                selectedLabelColor = Color.White
                            )
                        )
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategories.any { it.equals(category, ignoreCase = true) },
                                onClick = { viewModel.onListCategoryToggle(category) },
                                label = { Text(category, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeTontRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // ── LOCATION FILTERS ──────────────────────────────
                // Separate section from Category above — multi-select on both:
                // Country and City are each a set, so e.g. USA + UK, or Boston +
                // Toronto, can all be selected at once (OR within each group, AND
                // between the two groups and Category/search). Options come from
                // availableCountries/Cities in the ViewModel, i.e. whatever's
                // actually in the currently loaded business list, not a fixed list.
                if (showLocationFilters && availableCountries.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                strings.countryLabel.removeSuffix(" *"),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MeTontGrey
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedCountries.isEmpty(),
                                    onClick = { viewModel.onListCountryClearAll() },
                                    label = { Text(strings.allFilterOption, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MeTontRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                availableCountries.forEach { country ->
                                    FilterChip(
                                        selected = selectedCountries.any { it.equals(country, ignoreCase = true) },
                                        onClick = { viewModel.onListCountryToggle(country) },
                                        label = { Text(country, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MeTontRed,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (availableCities.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    strings.cityLabel.removeSuffix(" *"),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MeTontGrey
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedCities.isEmpty(),
                                        onClick = { viewModel.onListCityClearAll() },
                                        label = { Text(strings.allFilterOption, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MeTontRed,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    availableCities.forEach { city ->
                                        FilterChip(
                                            selected = selectedCities.any { it.equals(city, ignoreCase = true) },
                                            onClick = { viewModel.onListCityToggle(city) },
                                            label = { Text(city, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MeTontRed,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
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
                                val favoriteAction: () -> Unit = { viewModel.toggleFavorite(business.id) }
                                AuthGate.requireLogin(
                                    onNotLoggedIn = { onNavigateToAuth(favoriteAction) },
                                    action = favoriteAction
                                )
                            },
                            onToggleLike = {
                                val favoriteAction: () -> Unit = { viewModel.toggleFavorite(business.id) }
                                AuthGate.requireLogin(
                                    onNotLoggedIn = { onNavigateToAuth(favoriteAction) },
                                    action = favoriteAction
                                )
                            },
                            // BusinessListItem's own like icon does a second, redundant
                            // AuthGate check internally around onToggleLike — this was
                            // never wired up before, so a guest tapping that icon
                            // silently hit the default no-op instead of the prompt.
                            onNavigateToAuth = onNavigateToAuth,
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
                        .clickable { onBusinessClick(business.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF5D9D9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        val photoUrl = business.photos.firstOrNull()
                        if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = business.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                    .background(MeTontLightRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, null, tint = MeTontRed, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
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
                                    tint = MeTontRed,
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
}

// Realtor-style compact row card: photo thumbnail on the left, details on the
// right, favorite heart overlaid on the photo corner. Previously this card
// never showed a photo at all (business.photos was unreferenced here) and
// carried a like button, share button, and a full-width "Get Directions"
// button inline — those secondary actions already exist on the business
// detail screen (share/favorite in its top bar, like button and tappable
// address further down), so dropping them here keeps this list row a fast,
// glanceable directory entry rather than duplicating the detail screen.
@Composable
fun BusinessListItem(
    business: Business,
    userLocation: LatLng?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleLike: () -> Unit = {},
    onNavigateToAuth: (() -> Unit) -> Unit = {},
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF5D9D9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            // ── PHOTO + FAVORITE OVERLAY ──────────────────────────
            Box(modifier = Modifier.size(84.dp)) {
                val photoUrl = business.photos.firstOrNull()
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = business.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MeTontLightRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, null, tint = MeTontRed, modifier = Modifier.size(28.dp))
                    }
                }
                IconButton(
                    // onToggleFavorite is already wrapped in an AuthGate check by the
                    // caller (BusinessListScreen), same as the rest of this codebase's
                    // pattern — invoking it directly here, no need to gate twice.
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = MeTontRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── DETAILS ────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        business.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = MeTontRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            " ${business.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Text(
                    business.category,
                    color = MeTontRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                if (business.isVerified || business.isAlbanianOwned ||
                    business.isSponsored || business.isFeatured || business.isPremium
                ) {
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
                            tint = MeTontGrey
                        )
                        Text(
                            " ${if (distance < 1.0) "${(distance * 1000).toInt()} m" else "%.1f km".format(distance)} away",
                            style = MaterialTheme.typography.bodySmall,
                            color = MeTontGrey
                        )
                    }
                }
            }
        }
    }
}