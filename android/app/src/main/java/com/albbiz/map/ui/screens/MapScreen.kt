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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.data.Business
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onListClick: () -> Unit = {},
    onAddBusinessClick: () -> Unit = {},
    onBusinessClick: (String) -> Unit, // Navigation callback
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val businesses by viewModel.filteredBusinesses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
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
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.startLocationUpdates(context)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(TIRANA_LOCATION, 12f)
    }

    var hasMovedToInitialLocation by remember { mutableStateOf(false) }
    LaunchedEffect(userLocation) {
        if (!hasMovedToInitialLocation) {
            userLocation?.let { location ->
                val isGoogleHQ = (location.latitude in 37.42..37.43) && (location.longitude in -122.09..-122.07)
                if (!isGoogleHQ) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 14f)
                    hasMovedToInitialLocation = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text("AlbBizMap", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Add My Business") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onAddBusinessClick() },
                    icon = { Icon(Icons.Default.AddBusiness, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("List View") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onListClick() },
                    icon = { Icon(Icons.Default.List, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AlbBizMap") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null) }
                    },
                    actions = {
                        IconButton(onClick = {
                            showSearch = !showSearch
                            if (!showSearch) viewModel.onSearchQueryChange("")
                        }) {
                            Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, null)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
                    onMapClick = { selectedBusiness = null; keyboardController?.hide() }
                ) {
                    businesses.forEach { business ->
                        business.location?.let { geo ->
                            Marker(
                                state = MarkerState(position = LatLng(geo.latitude, geo.longitude)),
                                title = business.name,
                                snippet = "${business.category} ⭐ ${business.rating}",
                                icon = markerIcon,
                                onClick = { selectedBusiness = business; true }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloatingActionButton(onClick = { onAddBusinessClick() }) { Icon(Icons.Default.Add, null) }
                    FloatingActionButton(onClick = {
                        val target = userLocation ?: TIRANA_LOCATION
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 15f)
                    }) { Icon(Icons.Default.LocationOn, null) }
                }

                if (showSearch) {
                    Card(
                        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                placeholder = { Text("Search businesses...") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true,
                                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                            )

                            if (searchQuery.isNotEmpty()) {
                                if (businesses.isNotEmpty()) {
                                    HorizontalDivider()
                                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                                        items(businesses) { business ->
                                            ListItem(
                                                headlineContent = { Text(business.name) },
                                                supportingContent = { Text(business.category) },
                                                leadingContent = { Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary) },
                                                modifier = Modifier.clickable {
                                                    selectedBusiness = business
                                                    business.location?.let { geoPoint ->
                                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(geoPoint.latitude, geoPoint.longitude), 15f)
                                                    }
                                                    keyboardController?.hide()
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Text("No results for \"$searchQuery\"", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                selectedBusiness?.let { biz ->
                    BusinessDetailCard(
                        business = biz,
                        onClose = { selectedBusiness = null },
                        onViewDetails = { onBusinessClick(biz.id) } // Navigation trigger
                    )
                }
            }
        }
    }
}

@Composable
fun BusinessDetailCard(
    business: Business,
    onClose: () -> Unit,
    onViewDetails: () -> Unit // Navigation trigger
) {
    val context = LocalContext.current

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
                    Text(text = business.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // ... (rest of your existing UI code stays the same) ...

            Spacer(modifier = Modifier.height(16.dp))

            // ADDED NAVIGATION BUTTON
            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Details & Rate")
            }
        }
    }
}