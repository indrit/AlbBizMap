// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.data.Business
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

val TIRANA_LOCATION = LatLng(41.3275, 19.8187)

// Load bitmap from assets
fun loadMarkerFromAssets(context: android.content.Context, fileName: String): BitmapDescriptor? {
    return try {
        val inputStream = context.assets.open(fileName)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (bitmap != null) {
            // Resize to marker size (120x120 for better visibility)
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        } else {
            android.util.Log.e("AlbBizMap", "Failed to decode bitmap from assets")
            null
        }
    } catch (e: Exception) {
        android.util.Log.e("AlbBizMap", "Error loading from assets: ${e.message}")
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun MapScreen(
    onListClick: () -> Unit = {},
    onAddBusinessClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: MapViewModel = viewModel()
){
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val businesses by viewModel.filteredBusinesses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedBusiness by remember { mutableStateOf<Business?>(null) }
    var showSearch by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
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
        if (hasLocationPermission) {
            viewModel.startLocationUpdates(context)
        }
    }

    var initialCameraSet by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(TIRANA_LOCATION, 12f)
    }

    LaunchedEffect(businesses) {
        if (!initialCameraSet && businesses.isNotEmpty()) {
            initialCameraSet = true
        }
    }

    var hasMovedToUserLocation by remember { mutableStateOf(false) }

    LaunchedEffect(userLocation) {
        val location = userLocation
        if (!hasMovedToUserLocation && !initialCameraSet && location != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 14f)
            hasMovedToUserLocation = true
            initialCameraSet = true
        }
    }

    // Load the custom Albanian pin from assets
    val markerIcon: BitmapDescriptor? = remember(context) {
        loadMarkerFromAssets(context, "albanian_pin.png")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AlbBizMap") },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onListClick) {
                        Icon(Icons.Default.List, contentDescription = "List View")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                ),
                onMapClick = {
                    selectedBusiness = null
                    keyboardController?.hide()
                }
            ) {
                businesses.forEach { business ->
                    business.location?.let { geoPoint ->
                        val position = LatLng(geoPoint.latitude, geoPoint.longitude)
                        Marker(
                            state = MarkerState(position = position),
                            title = business.name,
                            snippet = "${business.category} ⭐ ${business.rating}",
                            // Use custom pin for all businesses
                            icon = markerIcon,
                            onClick = {
                                selectedBusiness = business
                                true
                            }
                        )
                    }
                }
            }

            // FABs at BOTTOM LEFT
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { onAddBusinessClick() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }

                FloatingActionButton(
                    onClick = {
                        val target = userLocation ?: TIRANA_LOCATION
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 15f)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "My Location",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Search Bar
            if (showSearch) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search businesses...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        )
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Business Details Card
            selectedBusiness?.let { business ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = business.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = business.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${business.rating} (${business.reviewCount} reviews)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = business.description,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = business.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (business.isSponsored) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⭐ SPONSORED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}