// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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

    // Read reactively from AuthViewModel instead of a value passed down through
    // NavHost's route builder — that builder only runs once (cached via remember),
    // so a plain String parameter here would freeze at its first-composition value
    // and never reflect later profile changes.
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserName = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@") ?: "User"

    val businesses by viewModel.filteredBusinesses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nearMeBusinesses by viewModel.nearMe.collectAsState()
    val topRatedBusinesses by viewModel.topRated.collectAsState()

    val eventsRepository = remember { EventsRepository() }
    // Must remember() the Flow itself, not just the repository — see EventsScreen.kt
    // for why calling .getEvents() fresh inline here would otherwise tear down and
    // reattach a live Firestore listener on every recomposition of this screen.
    val eventsFlow = remember { eventsRepository.getEvents() }
    val announcements by eventsFlow.collectAsState(initial = emptyList())

    // Stories
    val stories by storiesViewModel.stories.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val groupedStories = remember(stories) {
        stories.groupBy { it.businessId ?: it.userId }
    }

    var selectedSheetBusiness by remember { mutableStateOf<Business?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var markerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    val sheetState = rememberBottomSheetScaffoldState()

    LaunchedEffect(selectedSheetBusiness) {
        try {
            if (selectedSheetBusiness != null) {
                sheetState.bottomSheetState.expand()
            } else {
                sheetState.bottomSheetState.partialExpand()
            }
        } catch (e: Exception) {
            // Interrupted mid-animation (e.g. a drag gesture, or racing another
            // animation like the camera move). SheetState doesn't expose a public
            // snapTo() to force the target state here, but this LaunchedEffect
            // re-runs on every selectedSheetBusiness change anyway, so simply
            // swallowing the exception is enough to stop it from crashing the
            // screen instead of leaving it uncaught.
        }
    }

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
        // If we've already done the one-time move-to-location this session (e.g. we're
        // returning from Profile, not launching fresh), seed the camera directly at the
        // user's last known location instead of the Tirana default — avoids both a
        // jarring flash back to the default position AND re-running the animation below.
        position = if (viewModel.hasMovedToInitialLocation) {
            userLocation?.let { CameraPosition.fromLatLngZoom(it, 14f) }
                ?: CameraPosition.fromLatLngZoom(TIRANA_LOCATION, 12f)
        } else {
            CameraPosition.fromLatLngZoom(TIRANA_LOCATION, 12f)
        }
    }

    // Snap directly to the user's location instead of animate()-ing to it — animate()
    // is interruptible and throws if a gesture or recomposition cuts it off mid-flight,
    // which was a real source of exceptions here before. A direct position assignment
    // can't be interrupted, so there's nothing to catch.
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

    // Instead of cancelling an in-flight drawer animation and retrying (which broke on
    // rapid taps — a cancelled coroutine can't run further suspend cleanup code, so the
    // recovery call threw too), simply ignore clicks while an animation is already
    // running. The IconButton below also disables itself while this is true, so rapid
    // taps are dropped at the source instead of racing each other.
    var isDrawerBusy by remember { mutableStateOf(false) }

    val openDrawer: () -> Unit = {
        if (!isDrawerBusy) {
            scope.launch {
                isDrawerBusy = true
                try {
                    drawerState.open()
                } catch (e: Exception) {
                    // Interrupted (e.g. by a gesture) — nothing to recover, the drawer
                    // will settle on whatever state it's already in.
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
                    // Same as above — ignore, don't attempt further suspend calls here.
                } finally {
                    isDrawerBusy = false
                }
            }
        }
    }

    // ModalNavigationDrawer only auto-intercepts the system back button once the drawer
    // has FULLY settled at Open (drawerState.isOpen). While it's still mid-animation
    // opening — exactly the window where a quick back-tap right after the hamburger tap
    // lands — that interception isn't active yet, so the back press falls through to
    // Navigation's own back handling and pops the nav back stack instead of just closing
    // the drawer. Covering isDrawerBusy too closes that gap for the whole
    // opening/open/closing duration, not just once settled.
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
                        Text("MeTont", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Albanian Business Directory", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
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
                    onClick = { closeDrawer(); onProfileClick() },
                    icon = { Icon(Icons.Default.AccountCircle, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.favorites, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); onFavoritesClick() },
                    icon = { Icon(Icons.Default.Favorite, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.communityEvents, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); onEventsClick() },
                    icon = { Icon(Icons.Default.Event, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.addBusiness, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); onAddBusinessClick() },
                    icon = { Icon(Icons.Default.AddBusiness, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(strings.listView, fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { closeDrawer(); onListClick() },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null, tint = MeTontRed) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFEEEEEE))

                NavigationDrawerItem(
                    label = { Text(strings.logout, fontWeight = FontWeight.Bold, color = MeTontRed) },
                    selected = false,
                    onClick = { closeDrawer(); onLogout() },
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

            // TOP APP BAR
            TopAppBar(
                title = { Text(strings.appName, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = openDrawer, enabled = !isDrawerBusy) {
                        Icon(Icons.Default.Menu, null, tint = Color.White)
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
                  Column(modifier = Modifier.navigationBarsPadding()) {
                    if (selectedSheetBusiness != null) {
                        // BUSINESS DETAIL IN SHEET
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
                                Text("Back", color = MeTontRed, fontWeight = FontWeight.Medium)
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
                                    val uri = Uri.parse("google.navigation:q=${biz.location?.latitude},${biz.location?.longitude}&mode=d")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MeTontRed, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Directions, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Get Directions", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { onBusinessClick(biz.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View Full Profile", color = Color.Black, fontWeight = FontWeight.Medium)
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
                                // Add Story circle
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
                                                .clickable { onAddStoryClick() },
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

                                // Story circles
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
                                            onStoryClick(index.coerceAtLeast(0))
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
                                                    "sponsored" -> TierGold
                                                    "community" -> Color(0xFF2196F3)
                                                    "business" -> MeTontRed
                                                    "new_business" -> Color(0xFF4CAF50) // Green
                                                    else -> MeTontGrey
                                                }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                                        fontSize = 22.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (firstStory.type) {
                                                            "sponsored" -> TierGold
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
                                        if (firstStory.isSponsored) {
                                            Text(
                                                "Sponsored",
                                                fontSize = 8.sp,
                                                color = TierGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFF0F0F0)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // TOP RECOMMENDED
                            val sponsored = businesses.filter { it.isSponsored || it.isFeatured }
                            if (sponsored.isNotEmpty()) {
                                Text(
                                    "Top Recommended",
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(sponsored) { business ->
                                        val categoryIcon = BusinessCategory.entries
                                            .find { it.name.equals(business.category, ignoreCase = true) }
                                            ?.icon ?: Icons.Default.Business
                                        val tierColor = when {
                                            business.isSponsored -> TierGold
                                            business.isFeatured -> TierSilver
                                            else -> TierBronze
                                        }
                                        Card(
                                            modifier = Modifier
                                                .width(220.dp)
                                                .shadow(2.dp, RoundedCornerShape(14.dp))
                                                .clickable { selectedSheetBusiness = business },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = tierColor.copy(alpha = 0.15f)) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(categoryIcon, null, tint = tierColor, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(business.name, maxLines = 1, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                                                    Text(business.category, style = MaterialTheme.typography.labelSmall, color = tierColor, fontSize = 11.sp)
                                                    Surface(color = tierColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                                        Text(
                                                            when { business.isSponsored -> "Sponsored"; business.isFeatured -> "Featured"; else -> "Premium" },
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = tierColor,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                                                        Text(" ${business.rating}", style = MaterialTheme.typography.labelSmall, color = MeTontGrey)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // NEAR YOU
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
                                    Text("No businesses near you yet", style = MaterialTheme.typography.bodySmall, color = MeTontGrey, fontWeight = FontWeight.Medium)
                                    Text("MeTont is growing — check back soon!", style = MaterialTheme.typography.labelSmall, color = MeTontGrey.copy(alpha = 0.7f))
                                }
                            } else {
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(nearMeBusinesses) { business ->
                                        val categoryIcon = BusinessCategory.entries
                                            .find { it.name.equals(business.category, ignoreCase = true) }
                                            ?.icon ?: Icons.Default.Business
                                        Card(
                                            modifier = Modifier.width(220.dp).shadow(2.dp, RoundedCornerShape(14.dp)).clickable { selectedSheetBusiness = business },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MeTontRed.copy(alpha = 0.1f)) {
                                                    Box(contentAlignment = Alignment.Center) { Icon(categoryIcon, null, tint = MeTontRed, modifier = Modifier.size(20.dp)) }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(business.name, maxLines = 1, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                                                    Text(business.category, style = MaterialTheme.typography.labelSmall, color = MeTontRed, fontSize = 11.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                                                        Text(" ${business.rating} (${business.reviewCount})", style = MaterialTheme.typography.labelSmall, color = MeTontGrey)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // COMMUNITY ANNOUNCEMENTS
                            Text("Community Announcements", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (announcements.isEmpty()) {
                                Text("No upcoming events right now", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                            } else {
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(announcements) { event ->
                                        Card(
                                            modifier = Modifier.width(220.dp).shadow(2.dp, RoundedCornerShape(14.dp)).clickable { onEventsClick() },
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
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // MOST FAVORITED WORLDWIDE
                            Text("Most Favorited Worldwide", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (topRatedBusinesses.isEmpty()) {
                                Text("No businesses yet", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                            } else {
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(topRatedBusinesses) { business ->
                                        val categoryIcon = BusinessCategory.entries
                                            .find { it.name.equals(business.category, ignoreCase = true) }
                                            ?.icon ?: Icons.Default.Business
                                        Card(
                                            modifier = Modifier.width(220.dp).shadow(2.dp, RoundedCornerShape(14.dp)).clickable { selectedSheetBusiness = business },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MeTontRed.copy(alpha = 0.1f)) {
                                                    Box(contentAlignment = Alignment.Center) { Icon(categoryIcon, null, tint = MeTontRed, modifier = Modifier.size(20.dp)) }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(business.name, maxLines = 1, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                                                    Text(business.category, style = MaterialTheme.typography.labelSmall, color = MeTontRed, fontSize = 11.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                                                        Text(" ${business.rating} (${business.reviewCount})", style = MaterialTheme.typography.labelSmall, color = MeTontGrey)
                                                    }
                                                }
                                            }
                                        }
                                    }
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
                    // MAP
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                        onMapClick = {
                            selectedSheetBusiness = null
                            keyboardController?.hide()
                        }
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
                            }
                        )
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
                        FloatingActionButton(onClick = { onAddBusinessClick() }, containerColor = MeTontRed, contentColor = Color.White, shape = CircleShape) {
                            Icon(Icons.Default.Add, null)
                        }
                        FloatingActionButton(
                            onClick = {
                                // Snap directly instead of animate()-ing — no risk of an
                                // interrupted-animation exception from a second tap, and
                                // no job bookkeeping needed either.
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
