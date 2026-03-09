// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.Business
import com.albbiz.map.data.BusinessRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = BusinessRepository()

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses

    private val _filteredBusinesses = MutableStateFlow<List<Business>>(emptyList())
    val filteredBusinesses: StateFlow<List<Business>> = _filteredBusinesses

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private var locationCallback: LocationCallback? = null

    init {
        Log.d("AlbBizMap", "ViewModel initialized")

        // TEMPORARY: Add hardcoded test businesses immediately
        addHardcodedBusinesses()

        // Then try to load from Firestore
        testFirestoreAndLoad()
    }

    private fun addHardcodedBusinesses() {
        Log.d("AlbBizMap", "Adding hardcoded businesses for testing")
        val testBusinesses = listOf(
            Business(
                id = "test-1",
                name = "Bakllava e Nenes",
                description = "Traditional Albanian bakery with fresh baklava daily",
                category = "Food & Bakery",
                address = "Tirana, Albania",
                phone = "+355 69 123 4567",
                email = "info@bakllava.al",
                website = "https://bakllava.al",
                location = GeoPoint(41.3275, 19.8187),
                ownerId = "test-owner",
                isSponsored = true,
                rating = 4.8,
                reviewCount = 42,
                isActive = true
            ),
            Business(
                id = "test-2",
                name = "Bar Kafe Tirana",
                description = "Best coffee in the city center",
                category = "Cafe",
                address = "Tirana, Albania",
                location = GeoPoint(41.3280, 19.8190),
                ownerId = "test-owner-2",
                isSponsored = false,
                rating = 4.5,
                reviewCount = 28,
                isActive = true
            ),
            Business(
                id = "test-3",
                name = "Albanian Motors",
                description = "Car repair and maintenance",
                category = "Automotive",
                address = "Tirana, Albania",
                location = GeoPoint(41.3260, 19.8170),
                ownerId = "test-owner-3",
                isSponsored = false,
                rating = 4.2,
                reviewCount = 15,
                isActive = true
            )
        )
        _businesses.value = testBusinesses
        _filteredBusinesses.value = testBusinesses
        _isLoading.value = false
        Log.d("AlbBizMap", "Added ${testBusinesses.size} hardcoded businesses")
    }

    private fun testFirestoreAndLoad() {
        viewModelScope.launch {
            Log.d("AlbBizMap", "Testing Firestore connection...")
            val connected = repository.testConnection()
            if (connected) {
                Log.d("AlbBizMap", "Firestore connected, loading real businesses...")
                loadBusinesses()
            } else {
                Log.e("AlbBizMap", "Firestore connection failed, using hardcoded only")
            }
        }
    }

    private fun loadBusinesses() {
        Log.d("AlbBizMap", "Starting business flow collection...")
        repository.getActiveBusinesses()
            .onEach { firestoreBusinesses ->
                Log.d("AlbBizMap", "Received ${firestoreBusinesses.size} businesses from Firestore")
                if (firestoreBusinesses.isNotEmpty()) {
                    // Replace hardcoded with real data
                    _businesses.value = firestoreBusinesses
                    _filteredBusinesses.value = firestoreBusinesses
                    Log.d("AlbBizMap", "Replaced with real Firestore data")
                } else {
                    Log.d("AlbBizMap", "Firestore empty, keeping hardcoded businesses")
                }
                _isLoading.value = false
            }
            .catch { e ->
                Log.e("AlbBizMap", "Error loading from Firestore: ${e.message}")
                _error.value = e.message
                // Keep hardcoded businesses on error
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterBusinesses()
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
        filterBusinesses()
    }

    fun sortByNearMe() {
        val location = _userLocation.value
        if (location == null) {
            android.util.Log.d("AlbBizMap", "No user location available")
            return
        }

        val userGeoPoint = com.google.firebase.firestore.GeoPoint(
            location.latitude,
            location.longitude
        )

        _filteredBusinesses.value = _filteredBusinesses.value.sortedBy { business ->
            business.location?.let { businessLocation ->
                val businessGeoPoint = com.google.firebase.firestore.GeoPoint(
                    businessLocation.latitude,
                    businessLocation.longitude
                )
                repository.calculateDistance(userGeoPoint, businessGeoPoint)
            } ?: Double.MAX_VALUE
        }
    }

    private fun filterBusinesses() {
        val query = _searchQuery.value.lowercase()
        val category = _selectedCategory.value.lowercase()

        _filteredBusinesses.value = _businesses.value.filter { business ->
            val matchesSearch = query.isEmpty() ||
                    business.name.lowercase().contains(query) ||
                    business.category.lowercase().contains(query) ||
                    business.address.lowercase().contains(query) ||
                    business.description.lowercase().contains(query)

            val matchesCategory = category.isEmpty() ||
                    business.category.lowercase().contains(category)

            matchesSearch && matchesCategory
        }
    }

    fun addTestBusinessToFirestore(context: Context) {
        viewModelScope.launch {
            Log.d("AlbBizMap", "Adding test business to Firestore...")
            val testBusiness = Business(
                name = "Test Business ${System.currentTimeMillis()}",
                description = "Added from app",
                category = "Test",
                address = "Tirana, Albania",
                location = GeoPoint(41.3275, 19.8187),
                ownerId = "test-user",
                isActive = true,
                rating = 4.5,
                reviewCount = 10
            )
            repository.addBusiness(testBusiness)
                .onSuccess {
                    Log.d("AlbBizMap", "Test business added to Firestore!")
                }
                .onFailure {
                    Log.e("AlbBizMap", "Failed to add: ${it.message}")
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10000L
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _userLocation.value = LatLng(location.latitude, location.longitude)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            _error.value = "Location error: ${e.message}"
        }
    }

    fun stopLocationUpdates(context: Context) {
        locationCallback?.let {
            LocationServices.getFusedLocationProviderClient(context)
                .removeLocationUpdates(it)
        }
    }
}