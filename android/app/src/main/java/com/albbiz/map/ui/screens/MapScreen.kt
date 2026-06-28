// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens
import android.content.Intent
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.R
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessCategory
import com.albbiz.map.data.EventsRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext


val TIRANA_LOCATION = LatLng(41.3275, 19.8187)

fun loadMarkerFromAssets(context: Context, fileName: String): BitmapDescriptor? {
    return try {
        MapsInitializer.initialize(context)
        val inputStream = context.assets.open(fileName)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (bitmap != null) {
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        } else null
    } catch (e: Exception) {
        Log.e("AlbBizMap", "Error loading pin: ${e.message}")
        null
    }
}

data class BusinessClusterItem(
    val business: Business,
    private val pos: LatLng = business.location?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(0.0, 0.0)
) : ClusterItem {
    override fun getPosition(): LatLng = pos
    override fun getTitle(): String = business.name
    override fun getSnippet(): String = "${business.category} ⭐ ${business.rating}"
    override fun getZIndex(): Float = 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onListClick: () -> Unit = {},
    onAddBusinessClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onEventsClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    currentUserName: String = "",
    onBusinessClick: (String) -> Unit,
    onSeeMoreClick: (String) -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val businesses by viewModel.filteredBusinesses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nearMeBusinesses by viewModel.nearMe.collectAsState()
    val topRatedBusinesses by viewModel.topRated.collectAsState()

    val eventsRepository = remember { EventsRepository() }
    val announcements by eventsRepository.getEvents().collectAsState(initial = emptyList())

    var showCategoryFilter by remember { mutableStateOf(false) }
    var selectedBusiness by remember { mutableStateOf<Business?>(null) }
    var markerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val topPicksBusinesses by viewModel.topPicks.collectAsState()

    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST) {
            scope.launch {
                delay(400)
                markerIcon = loadMarkerFromAssets(context, "albanian_pin.png")
            }
        }
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) viewModel.startLocationUpdates(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(TIRANA_LOCATION, 12f)
    }

    var hasMovedToInitialLocation by remember { mutableStateOf(false) }
    LaunchedEffect(userLocation) {
        if (!hasMovedToInitialLocation) {
            userLocation?.let { location ->
                val isGoogleHQ = (location.latitude in 37.42..37.43) &&
                        (location.longitude in -122.09..-122.07)
                if (!isGoogleHQ) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 14f)
                    hasMovedToInitialLocation = true
                }
            }
        }
    }

    var selectedCategoryLabel by remember { mutableStateOf("All") }
    val categories = listOf(
        "All", "Restaurant", "Cafe", "Market",
        "Contractor", "Lawyer", "Dentist",
        "Barber", "Auto Shop", "Other"
    )

    val sheetState = rememberBottomSheetScaffoldState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                // ── RED HEADER ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MeTontRed),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.metont_nobackgroundcolor),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.15f),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.metont_nobackgroundcolor),
                            contentDescription = "MeTont Logo",
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text("MeTont", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Albanian Business Directory", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }

                // ── WELCOME BAR ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFC41A1C))
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${strings.welcomeUser}, $currentUserName",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── MENU ITEMS ─────────────────────────────────────────
                NavigationDrawerItem(
                    label = { Text(strings.profile, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onProfileClick() },
                    icon = { Icon(Icons.Default.AccountCircle, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.favorites, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onFavoritesClick() },
                    icon = { Icon(Icons.Default.Favorite, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.communityEvents, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onEventsClick() },
                    icon = { Icon(Icons.Default.Event, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.addBusiness, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onAddBusinessClick() },
                    icon = { Icon(Icons.Default.AddBusiness, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.listView, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onListClick() },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFEEEEEE))

                // ── LOGOUT ─────────────────────────────────────────────
                NavigationDrawerItem(
                    label = { Text(strings.logout, fontWeight = FontWeight.Bold, color = MeTontRed) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onLogout() },
                    icon = { Icon(Icons.Default.Logout, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Text(
                    "MeTont v1.0.0",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = MeTontGrey,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── TOP APP BAR WITH SEARCH ───────────────────────────────
            TopAppBar(
                title = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MeTontGrey,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.Black,
                                    fontSize = 14.sp
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = { keyboardController?.hide() }
                                ),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            strings.searchPlaceholder,
                                            color = MeTontGrey,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChange("") },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MeTontGrey,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            VerticalDivider(
                                modifier = Modifier.height(20.dp),
                                color = Color(0xFFE0E0E0)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showCategoryFilter = !showCategoryFilter },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Filter",
                                    tint = if (showCategoryFilter) MeTontRed else MeTontGrey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeTontRed)
            )

            // ── BOTTOM SHEET SCAFFOLD ─────────────────────────────────
            BottomSheetScaffold(
                scaffoldState = sheetState,
                sheetPeekHeight = 140.dp,
                sheetContainerColor = MaterialTheme.colorScheme.background,
                sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                sheetDragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(MeTontGrey.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    )
                },
                sheetContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 400.dp)
                            .padding(bottom = 24.dp)
                    ) {

                        // ── TOP PICKS ─────────────────────────────────────────────
                        if (topPicksBusinesses.isNotEmpty()) {
                            Text(
                                "Top Recommended",
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(topPicksBusinesses.take(3)) { business ->
                                    val categoryIcon = BusinessCategory.entries
                                        .find {
                                            it.name.equals(
                                                business.category,
                                                ignoreCase = true
                                            )
                                        }
                                        ?.icon ?: Icons.Default.Business
                                    TopPickCard(
                                        business = business,
                                        categoryIcon = categoryIcon,
                                        onClick = { onBusinessClick(business.id) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        // ── NEAR YOU ──────────────────────────────────
                        Text(
                            "Near You",
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (nearMeBusinesses.isEmpty()) {
                            Text(
                                "Enable location to see businesses near you",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(nearMeBusinesses.take(3)) { business ->
                                    val categoryIcon = BusinessCategory.entries
                                        .find { it.name.equals(business.category, ignoreCase = true) }
                                        ?.icon ?: Icons.Default.Business
                                    NearYouBusinessCard(
                                        business = business,
                                        categoryIcon = categoryIcon,
                                        onClick = { onBusinessClick(business.id) }
                                    )
                                }
                                item {
                                    SeeMoreCard(onClick = { onSeeMoreClick("nearMe") })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── COMMUNITY ANNOUNCEMENTS ───────────────────
                        Text(
                            "Community Announcements",
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (announcements.isEmpty()) {
                            Text(
                                "No upcoming events right now",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(announcements.take(3)) { event ->
                                    AnnouncementCard(
                                        title = event.title,
                                        category = event.category,
                                        locationName = event.locationName,
                                        onClick = { onEventsClick() }
                                    )
                                }
                                item {
                                    SeeMoreCard(onClick = { onEventsClick() })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── MOST FAVORITED WORLDWIDE ──────────────────
                        Text(
                            "Most Favorited Worldwide",
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (topRatedBusinesses.isEmpty()) {
                            Text(
                                "No businesses yet",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(topRatedBusinesses.take(3)) { business ->
                                    val categoryIcon = BusinessCategory.entries
                                        .find { it.name.equals(business.category, ignoreCase = true) }
                                        ?.icon ?: Icons.Default.Business
                                    NearYouBusinessCard(
                                        business = business,
                                        categoryIcon = categoryIcon,
                                        onClick = { onBusinessClick(business.id) }
                                    )
                                }
                                item {
                                    SeeMoreCard(onClick = { onSeeMoreClick("topRated") })
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // ── MAP ───────────────────────────────────────────
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false
                        ),
                        onMapClick = { selectedBusiness = null; keyboardController?.hide() }
                    ) {
                        val clusterItems = remember(businesses) {
                            businesses.mapNotNull { business ->
                                business.location?.let { BusinessClusterItem(business) }
                            }
                        }
                        Clustering<BusinessClusterItem>(
                            items = clusterItems,
                            onClusterItemClick = { item ->
                                selectedBusiness = item.business
                                true
                            }
                        )
                    }

                    // ── CATEGORY FILTERS (animated) ───────────────────
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showCategoryFilter,
                        modifier = Modifier.align(Alignment.TopCenter),
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { category ->
                                    FilterChip(
                                        selected = selectedCategoryLabel == category,
                                        onClick = {
                                            selectedCategoryLabel = category
                                            viewModel.onCategoryChange(
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
                    }

                    // ── SEARCH DROPDOWN ───────────────────────────────
                    if (searchQuery.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            if (businesses.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 250.dp)
                                ) {
                                    items(businesses) { business ->
                                        ListItem(
                                            headlineContent = {
                                                Text(business.name, fontWeight = FontWeight.Medium)
                                            },
                                            supportingContent = {
                                                Text(business.category, color = MeTontRed, fontSize = 12.sp)
                                            },
                                            leadingContent = {
                                                Icon(Icons.Default.Business, null, tint = MeTontRed)
                                            },
                                            modifier = Modifier.clickable {
                                                selectedBusiness = business
                                                business.location?.let { geoPoint ->
                                                    cameraPositionState.position =
                                                        CameraPosition.fromLatLngZoom(
                                                            LatLng(geoPoint.latitude, geoPoint.longitude),
                                                            15f
                                                        )
                                                }
                                                keyboardController?.hide()
                                            }
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    strings.noSearchResults,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MeTontGrey
                                )
                            }
                        }
                    }

                    // ── FAB BUTTONS ───────────────────────────────────
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { onAddBusinessClick() },
                            containerColor = MeTontRed,
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Add, null)
                        }
                        FloatingActionButton(
                            onClick = {
                                val target = userLocation ?: TIRANA_LOCATION
                                cameraPositionState.position =
                                    CameraPosition.fromLatLngZoom(target, 15f)
                            },
                            containerColor = Color.White,
                            contentColor = MeTontRed,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.MyLocation, null)
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MeTontRed
                        )
                    }

                    // ── BUSINESS DETAIL CARD ──────────────────────────
                    selectedBusiness?.let { biz ->
                        BusinessDetailCard(
                            business = biz,
                            isFavorite = favoriteIds.contains(biz.id),
                            onToggleFavorite = { viewModel.toggleFavorite(biz.id) },
                            onClose = { selectedBusiness = null },
                            onViewDetails = { onBusinessClick(biz.id) }
                        )
                    }
                }
            }
        }
    }
}

// ── REUSABLE COMPOSABLES ──────────────────────────────────────────────

@Composable
fun NearYouBusinessCard(
    business: Business,
    categoryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MeTontRed.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(categoryIcon, null, tint = MeTontRed, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                    Text(
                        " ${business.rating} (${business.reviewCount})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MeTontGrey
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(
    title: String,
    category: String,
    locationName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MeTontRed.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Event, null, tint = MeTontRed, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(category, style = MaterialTheme.typography.labelSmall, color = MeTontRed, fontSize = 11.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MeTontGrey, modifier = Modifier.size(12.dp))
                    Text(
                        " $locationName",
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                        color = MeTontGrey
                    )
                }
            }
        }
    }
}

@Composable
fun SeeMoreCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFE41E20)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "See More",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BusinessDetailCard(
    business: Business,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClose: () -> Unit,
    onViewDetails: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = business.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = business.category,
                        color = MeTontRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (business.isPremium) BadgeChip(strings.premium, Color(0xFFFFAA00))
                        if (business.isVerified) {
                            Spacer(Modifier.width(4.dp))
                            BadgeChip(strings.verified, MeTontRed)
                        }
                    }
                }
                Row {
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
                            tint = MeTontGrey
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            null,
                            tint = if (isFavorite) MeTontRed else MeTontGrey
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                )
            ) {
                Text(strings.viewDetailsAndRate, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BadgeChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TopPickCard(
    business: Business,
    categoryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val tierColor = when {
        business.isSponsored -> Color(0xFFC7BF3F) // Gold
        business.isFeatured -> Color(0xFFC0C0C0)  // Silver
        else -> Color(0xFFCD7F32)                  // Bronze
    }
    val tierLabel = when {
        business.isSponsored -> "Sponsored"
        business.isFeatured -> "Featured"
        else -> "Premium"
    }

    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = tierColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(categoryIcon, null, tint = tierColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = tierColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            tierLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = tierColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            " ${business.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeTontGrey,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
