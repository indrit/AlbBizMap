// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.data.Business
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.List
import com.albbiz.map.ui.LocalAppStrings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem

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
    onBusinessClick: (String) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val businesses by viewModel.filteredBusinesses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedBusiness by remember { mutableStateOf<Business?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var markerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "AlbBizMap",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(LocalAppStrings.current.profile) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onProfileClick() },
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(LocalAppStrings.current.favorites) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onFavoritesClick() },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(LocalAppStrings.current.communityEvents) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onEventsClick() },
                    icon = { Icon(Icons.Default.Event, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(LocalAppStrings.current.addBusiness) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onAddBusinessClick() },
                    icon = { Icon(Icons.Default.AddBusiness, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(LocalAppStrings.current.listView) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onListClick() },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(LocalAppStrings.current.appName) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showSearch = !showSearch
                            if (!showSearch) viewModel.onSearchQueryChange("")
                        }) {
                            Icon(
                                if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                null
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
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

                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryLabel == category,
                                onClick = {
                                    selectedCategoryLabel = category
                                    viewModel.onCategoryChange(if (category == "All") "" else category)
                                },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                if (showSearch) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(top = 72.dp, start = 16.dp, end = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                placeholder = {  Text(LocalAppStrings.current.searchPlaceholder) },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true,
                                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                            )

                            if (searchQuery.isNotEmpty()) {
                                if (businesses.isNotEmpty()) {
                                    HorizontalDivider()
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 250.dp)
                                    ) {
                                        items(businesses) { business ->
                                            ListItem(
                                                headlineContent = { Text(business.name) },
                                                supportingContent = { Text(business.category) },
                                                leadingContent = {
                                                    Icon(
                                                        Icons.Default.Business,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
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
                                        LocalAppStrings.current.noSearchResults,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloatingActionButton(onClick = { onAddBusinessClick() }) {
                        Icon(Icons.Default.Add, null)
                    }
                    FloatingActionButton(onClick = {
                        val target = userLocation ?: TIRANA_LOCATION
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 15f)
                    }) {
                        Icon(Icons.Default.LocationOn, null)
                    }
                }

                if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

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

@Composable
fun BusinessDetailCard(
    business: Business,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClose: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 500.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = business.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        if (business.isPremium) BadgeChip(LocalAppStrings.current.premium, Color(0xFFFFAA00))
                        if (business.isVerified) {
                            Spacer(Modifier.width(4.dp))
                            BadgeChip(LocalAppStrings.current.verified, Color(0xFF2196F3))
                        }
                    }
                }
                
                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(LocalAppStrings.current.viewDetailsAndRate)
            }
        }
    }
}

@Composable
fun BadgeChip(label: String, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
