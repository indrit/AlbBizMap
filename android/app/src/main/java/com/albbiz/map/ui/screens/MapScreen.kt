// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.R
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessCategory
import com.albbiz.map.data.EventsRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.ui.theme.TierBronze
import com.albbiz.map.ui.theme.TierGold
import com.albbiz.map.ui.theme.TierSilver
import com.albbiz.map.viewmodel.AuthViewModel
import com.albbiz.map.viewmodel.StoriesViewModel
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

fun loadMarkerFromResource(context: Context, resId: Int): BitmapDescriptor? {
    return try {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        if (bitmap != null) {
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        } else null
    } catch (e: Exception) {
        Log.e("AlbBizMap", "Error loading resource pin: ${e.message}")
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
    onListClick: (String) -> Unit = {},
    onAddBusinessClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onEventsClick: () -> Unit = {},
    onJobsClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onBusinessClick: (String) -> Unit,
    onAddStoryClick: () -> Unit = {},
    onStoryClick: (Int) -> Unit = {},
    viewModel: MapViewModel = viewModel(),
    storiesViewModel: StoriesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val strings = LocalAppStrings.current

    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserName = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@") ?: "User"

    val businesses by viewModel.filteredBusinesses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nearMeBusinesses by viewModel.nearMe.collectAsState()
    val mostFavoritedBusinesses by viewModel.mostFavorited.collectAsState()
    val topPicksBusinesses by viewModel.topPicks.collectAsState()

    val eventsRepository = remember { EventsRepository() }
    val eventsFlow = remember { eventsRepository.getEvents() }
    val announcements by eventsFlow.collectAsState(initial = emptyList())

    val stories by storiesViewModel.stories.collectAsState()
    val groupedStories = remember(stories) {
        stories.groupBy { it.businessId ?: it.userId }
    }

    var selectedSheetBusiness by remember { mutableStateOf<Business?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    
    // TIER MARKERS
    var markerFree by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var markerPremium by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var markerFeatured by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var markerSponsored by remember { mutableStateOf<BitmapDescriptor?>(null) }

    var mapReady by remember { mutableStateOf(false) }

    val sheetState = rememberBottomSheetScaffoldState()

    LaunchedEffect(selectedSheetBusiness) {
        try {
            if (selectedSheetBusiness != null) {
                sheetState.bottomSheetState.expand()
            } else {
                sheetState.bottomSheetState.partialExpand()
            }
        } catch (e: Exception) {}
    }

    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST) {
            scope.launch {
                delay(400)
                markerFree = loadMarkerFromAssets(context, "albanian_pin.png")
                markerPremium = loadMarkerFromResource(context, R.drawable.metont_bronze)
                markerFeatured = loadMarkerFromResource(context, R.drawable.metont_silver)
                markerSponsored = loadMarkerFromResource(context, R.drawable.metont_gold)
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
        val alreadyKnownLocation = viewModel.userLocation.value
        position = when {
            viewModel.hasMovedToInitialLocation ->
                userLocation?.let { CameraPosition.fromLatLngZoom(it, 14f) }
                    ?: CameraPosition.fromLatLngZoom(TIRANA_LOCATION, 12f)
            alreadyKnownLocation != null -> {
                viewModel.hasMovedToInitialLocation = true
                CameraPosition.fromLatLngZoom(alreadyKnownLocation, 14f)
            }
            else -> CameraPosition.fromLatLngZoom(TIRANA_LOCATION, 12f)
        }
    }

    LaunchedEffect(userLocation) {
        if (!viewModel.hasMovedToInitialLocation) {
            userLocation?.let { location ->
                val isGoogleHQ = (location.latitude in 37.42..37.43) &&
                        (location.longitude in -122.09..-122.07)
                if (!isGoogleHQ) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 14f)
                    viewModel.hasMovedToInitialLocation = true
                }
            }
        }
    }

    var isDrawerBusy by remember { mutableStateOf(false) }

    val openDrawer: () -> Unit = {
        if (!isDrawerBusy) {
            scope.launch {
                isDrawerBusy = true
                try {
                    drawerState.open()
                } catch (e: Exception) {
                } finally {
                    isDrawerBusy = false
                }
            }
        }
    }

    val closeDrawer: () -> Unit = {
        if (!isDrawerBusy) {
            scope.launch {
                isDrawerBusy = true
                try {
                    drawerState.close()
                } catch (e: Exception) {
                } finally {
                    isDrawerBusy = false
                }
            }
        }
    }

    BackHandler(enabled = isDrawerBusy || drawerState.isOpen) {
        closeDrawer()
    }

    var selectedCategoryLabel by remember { mutableStateOf("All") }
    val categories = listOf(
        "All", "Restaurant", "Cafe", "Market",
        "Contractor", "Lawyer", "Dentist",
        "Barber", "Auto Shop", "Other"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                // RED HEADER
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
                        modifier = Modifier.fillMaxSize().alpha(0.15f),
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
                        Text(strings.appName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(strings.appTagline, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }

                // WELCOME BAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFC41A1C))
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${strings.welcomeUser}, ${currentUserName.substringBefore(" ")}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text(strings.profile, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); if (mapReady) onProfileClick() },
                    icon = { Icon(Icons.Default.AccountCircle, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.favorites, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); if (mapReady) onFavoritesClick() },
                    icon = { Icon(Icons.Default.Favorite, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawerFavoritesItem")
                )
                NavigationDrawerItem(
                    label = { Text(strings.communityEvents, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); if (mapReady) onEventsClick() },
                    icon = { Icon(Icons.Default.Event, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.jobs, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); if (mapReady) onJobsClick() },
                    icon = { Icon(Icons.Default.Work, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.addBusiness, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); if (mapReady) onAddBusinessClick() },
                    icon = { Icon(Icons.Default.AddBusiness, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.listView, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); if (mapReady) onListClick("default") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFEEEEEE))

                NavigationDrawerItem(
                    label = { Text(strings.logout, fontWeight = FontWeight.Bold, color = MeTontRed) },
                    selected = false,
                    onClick = { closeDrawer(); if (mapReady) onLogout() },
                    icon = { Icon(Icons.Default.Logout, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Text(
                    "MeTont v1.2.0",
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

            // TOP APP BAR
            TopAppBar(
                title = { Text(strings.appName, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = openDrawer,
                        enabled = !isDrawerBusy,
                        modifier = Modifier.testTag("mapHamburgerButton")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) viewModel.onSearchQueryChange("")
                    }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeTontRed)
            )

            // BOTTOM SHEET SCAFFOLD
            BottomSheetScaffold(
                scaffoldState = sheetState,
                sheetPeekHeight = 120.dp,
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
                          .verticalScroll(rememberScrollState())
                          .navigationBarsPadding()
                  ) {
                    if (selectedSheetBusiness != null) {
                        val biz = selectedSheetBusiness!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            TextButton(
                                onClick = { selectedSheetBusiness = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MeTontRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.back, color = MeTontRed, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(biz.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(biz.category, style = MaterialTheme.typography.bodySmall, color = MeTontRed, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                Text(" ${biz.rating} (${biz.reviewCount} reviews)", style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (biz.address.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = MeTontRed, modifier = Modifier.size(14.dp))
                                    Text(" ${biz.address}", style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (biz.description.isNotBlank()) {
                                Text(biz.description, style = MaterialTheme.typography.bodySmall, color = Color.Black.copy(alpha = 0.7f), maxLines = 2)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    try {
                                        val uri = Uri.parse("google.navigation:q=${biz.location?.latitude},${biz.location?.longitude}&mode=d")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                                    } catch (e: Exception) {
                                        Toast.makeText(context, strings.googleMapsNotInstalled, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MeTontRed, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Directions, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.getDirections, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { if (mapReady) onBusinessClick(biz.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(strings.viewFullProfile, color = Color.Black, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                    } else {
                        // CAROUSELS
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 400.dp)
                                .padding(bottom = 24.dp)
                        ) {
                            // ── STORIES BAR ───────────────────────────────────────
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(MeTontRed.copy(alpha = 0.1f))
                                                .border(2.dp, MeTontRed, CircleShape)
                                                .clickable { if (mapReady) onAddStoryClick() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Add Story",
                                                tint = MeTontRed,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Text(
                                            "Your Story",
                                            fontSize = 10.sp,
                                            color = MeTontGrey,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }

                                items(groupedStories.entries.toList()) { (_, storyGroup) ->
                                    val hasViewed = storiesViewModel.hasViewedAllStories(storyGroup)
                                    val firstStory = storyGroup.first()
                                    val displayName = firstStory.businessName ?: firstStory.userName

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.clickable {
                                            val allStories = stories
                                            val clickedStory = storyGroup.first()
                                            val index = allStories.indexOfFirst { it.id == clickedStory.id }
                                            if (mapReady) onStoryClick(index.coerceAtLeast(0))
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .border(
                                                    width = 2.5.dp,
                                                    color = if (hasViewed) MeTontGrey.copy(alpha = 0.4f) else MeTontRed,
                                                    shape = CircleShape
                                                )
                                                .padding(3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = CircleShape,
                                                color = when (firstStory.type) {
                                                    "community" -> Color(0xFF2196F3)
                                                    "business" -> MeTontRed
                                                    "new_business" -> Color(0xFF4CAF50)
                                                    else -> MeTontGrey
                                                }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                                        fontSize = 22.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (firstStory.type) {
                                                            "community" -> Color(0xFF2196F3)
                                                            "business" -> MeTontRed
                                                            else -> MeTontGrey
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            displayName.take(8),
                                            fontSize = 10.sp,
                                            color = if (hasViewed) MeTontGrey.copy(alpha = 0.5f) else Color.Black,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFF0F0F0)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (topPicksBusinesses.isNotEmpty()) {
                                var topRecommendedExpanded by remember { mutableStateOf(false) }
                                val visibleTopPicks = if (topRecommendedExpanded) topPicksBusinesses else topPicksBusinesses.take(2)
                                Text(
                                    "Top Recommended",
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    visibleTopPicks.forEach { business ->
                                        FeaturedPickCard(
                                            business = business,
                                            onClick = { selectedSheetBusiness = business }
                                        )
                                    }
                                }
                                if (topPicksBusinesses.size > 2) {
                                    SeeMoreButton(
                                        expanded = topRecommendedExpanded,
                                        onClick = { topRecommendedExpanded = !topRecommendedExpanded },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Text(
                                "Near You",
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            if (nearMeBusinesses.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.LocationOff, null, tint = MeTontGrey.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(strings.noBusinessesNearYou, style = MaterialTheme.typography.bodySmall, color = MeTontGrey, fontWeight = FontWeight.Medium)
                                    Text(strings.appGrowingMessage, style = MaterialTheme.typography.labelSmall, color = MeTontGrey.copy(alpha = 0.7f))
                                }
                            } else {
                                var nearYouExpanded by remember { mutableStateOf(false) }
                                val visibleNearMe = if (nearYouExpanded) nearMeBusinesses else nearMeBusinesses.take(2)
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    visibleNearMe.forEach { business ->
                                        FeaturedPickCard(
                                            business = business,
                                            onClick = { selectedSheetBusiness = business }
                                        )
                                    }
                                }
                                if (nearMeBusinesses.size > 2) {
                                    SeeMoreButton(
                                        expanded = nearYouExpanded,
                                        onClick = { nearYouExpanded = !nearYouExpanded },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(strings.communityAnnouncements, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (announcements.isEmpty()) {
                                Text(strings.noUpcomingEventsShort, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                            } else {
                                val visibleAnnouncements = announcements.take(6)
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(visibleAnnouncements) { event ->
                                        Card(
                                            modifier = Modifier.width(220.dp).shadow(2.dp, RoundedCornerShape(14.dp)).clickable { if (mapReady) onEventsClick() },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MeTontRed.copy(alpha = 0.1f)) {
                                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Event, null, tint = MeTontRed, modifier = Modifier.size(20.dp)) }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(event.title, maxLines = 1, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                                                    Text(event.category, style = MaterialTheme.typography.labelSmall, color = MeTontRed, fontSize = 11.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.LocationOn, null, tint = MeTontGrey, modifier = Modifier.size(12.dp))
                                                        Text(" ${event.locationName}", maxLines = 1, style = MaterialTheme.typography.labelSmall, color = MeTontGrey)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (announcements.size > 6) {
                                    SeeMoreNavButton(
                                        onClick = { if (mapReady) onEventsClick() },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(strings.mostFavoritedWorldwide, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (mostFavoritedBusinesses.isEmpty()) {
                                Text(strings.noBusinessesYetHome, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                            } else {
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(mostFavoritedBusinesses.take(6)) { business ->
                                        MapBusinessCard(
                                            business = business,
                                            onClick = { selectedSheetBusiness = business }
                                        )
                                    }
                                }
                                if (mostFavoritedBusinesses.size > 6) {
                                    SeeMoreNavButton(
                                        onClick = { if (mapReady) onListClick("mostFavorited") },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
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
                    val currentOnMapLoaded by rememberUpdatedState { mapReady = true }
                    val stableOnMapLoaded = remember { { currentOnMapLoaded() } }

                    val currentOnMapClick by rememberUpdatedState<(LatLng) -> Unit> {
                        selectedSheetBusiness = null
                        keyboardController?.hide()
                    }
                    val stableOnMapClick = remember { { latLng: LatLng -> currentOnMapClick(latLng) } }

                    // MAP
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = remember(hasLocationPermission) { MapProperties(isMyLocationEnabled = hasLocationPermission) },
                        uiSettings = remember { MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false) },
                        onMapLoaded = stableOnMapLoaded,
                        onMapClick = stableOnMapClick
                    ) {
                        val clusterItems = remember(businesses) {
                            businesses.mapNotNull { business ->
                                business.location?.let { BusinessClusterItem(business) }
                            }
                        }

                        Clustering<BusinessClusterItem>(
                            items = clusterItems,
                            onClusterItemClick = { item ->
                                selectedSheetBusiness = item.business
                                true
                            },
                            clusterItemContent = { item ->
                                val business = item.business
                                val icon = when {
                                    business.isSponsored -> markerSponsored
                                    business.isFeatured -> markerFeatured
                                    business.isPremium -> markerPremium
                                    else -> markerFree
                                }
                                // Clustering doesn't directly take BitmapDescriptor for content,
                                // but we can use Marker inside. However, maps-compose Clustering
                                // uses standard markers if content is null.
                                // For custom tier markers, it's best to handle them in Clustering renderer
                                // or use custom clusterItemContent with Image.
                                val iconRes = when {
                                    business.isSponsored -> R.drawable.metont_gold
                                    business.isFeatured -> R.drawable.metont_silver
                                    business.isPremium -> R.drawable.metont_bronze
                                    else -> null
                                }
                                
                                if (iconRes != null) {
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else {
                                    // Default asset marker
                                    Image(
                                        painter = painterResource(id = R.drawable.metont_nobackgroundcolor), // Fallback
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        alpha = 0.8f
                                    )
                                }
                            }
                        )
                    }

                    if (!mapReady) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MeTontRed)
                        }
                    }

                    // CATEGORY FILTERS
                    Card(
                        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategoryLabel == category,
                                    onClick = {
                                        selectedCategoryLabel = category
                                        viewModel.onCategoryChange(if (category == "All") "" else category)
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

                    // SEARCH BAR
                    if (showSearch) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(top = 64.dp, start = 16.dp, end = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChange(it) },
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    placeholder = { Text(strings.searchPlaceholder) },
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MeTontRed) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MeTontRed, cursorColor = MeTontRed),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                                )
                                if (searchQuery.isNotEmpty()) {
                                    if (businesses.isNotEmpty()) {
                                        HorizontalDivider()
                                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                                            items(businesses) { business ->
                                                ListItem(
                                                    headlineContent = { Text(business.name, fontWeight = FontWeight.Medium) },
                                                    supportingContent = { Text(business.category, color = MeTontRed, fontSize = 12.sp) },
                                                    leadingContent = { Icon(Icons.Default.Business, null, tint = MeTontRed) },
                                                    modifier = Modifier.clickable {
                                                        selectedSheetBusiness = business
                                                        business.location?.let { geoPoint ->
                                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(geoPoint.latitude, geoPoint.longitude), 15f)
                                                        }
                                                        keyboardController?.hide()
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        Text(strings.noSearchResults, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MeTontGrey)
                                    }
                                }
                            }
                        }
                    }

                    // FAB BUTTONS
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(onClick = { if (mapReady) onAddBusinessClick() }, containerColor = MeTontRed, contentColor = Color.White, shape = CircleShape) {
                            Icon(Icons.Default.Add, null)
                        }
                        FloatingActionButton(
                            onClick = {
                                val target = userLocation ?: TIRANA_LOCATION
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 15f)
                            },
                            containerColor = Color.White,
                            contentColor = MeTontRed,
                            shape = CircleShape
                        ) { Icon(Icons.Default.MyLocation, null) }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MeTontRed)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeChip(label: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
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
private fun SeeMoreButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            if (expanded) "See less" else "See more",
            color = MeTontRed,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            null,
            tint = MeTontRed,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SeeMoreNavButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            "See more",
            color = MeTontRed,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            null,
            tint = MeTontRed,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun FeaturedPickCard(
    business: Business,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5D9D9))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val photoUrl = business.photos.firstOrNull()
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = business.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFFBEAEA)),
                        contentAlignment = Alignment.Center
                    ) {
                        val categoryIcon = BusinessCategory.entries
                            .find { it.name.equals(business.category, ignoreCase = true) }
                            ?.icon ?: Icons.Default.Business
                        Icon(categoryIcon, null, tint = MeTontRed, modifier = Modifier.size(36.dp))
                    }
                }
                val tierColor = when {
                    business.isSponsored -> TierGold
                    business.isFeatured -> TierSilver
                    business.isPremium -> TierBronze
                    else -> null
                }
                val tierLabel = when {
                    business.isSponsored -> strings.sponsored
                    business.isFeatured -> strings.featured2
                    business.isPremium -> strings.premium
                    else -> null
                }
                if (tierColor != null && tierLabel != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(tierColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tierLabel.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    business.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    business.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MeTontRed,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text(" ${business.rating}", style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                }
            }
        }
    }
}

@Composable
private fun MapBusinessCard(
    business: Business,
    tierColor: Color? = null,
    tierLabel: String? = null,
    onClick: () -> Unit
) {
    val accentColor = tierColor ?: MeTontRed

    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val photoUrl = business.photos.firstOrNull()
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = business.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                val categoryIcon = BusinessCategory.entries
                    .find { it.name.equals(business.category, ignoreCase = true) }
                    ?.icon ?: Icons.Default.Business
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(categoryIcon, null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
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
                    color = accentColor,
                    fontSize = 11.sp
                )
                if (tierLabel != null) {
                    Surface(color = accentColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            tierLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                    Text(" ${business.rating}", style = MaterialTheme.typography.labelSmall, color = MeTontGrey)
                }
            }
        }
    }
}
