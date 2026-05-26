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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = BusinessRepository()

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses

    private val _filteredBusinesses = MutableStateFlow<List<Business>>(emptyList())
    val filteredBusinesses: StateFlow<List<Business>> = _filteredBusinesses

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedCategory = MutableStateFlow("")

    // ── DISCOVERY FLOWS (Section 9) ──────────────────────────────
    val recentlyAdded = _businesses.map { list ->
        list.sortedByDescending { it.id }.take(10) 
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val topRated = _businesses.map { list ->
        list.sortedByDescending { it.rating }.take(10)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val featured = _businesses.map { list ->
        list.filter { it.isFeatured || it.isSponsored || it.isPremium }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val nearMe = combine(_businesses, _userLocation) { list, location ->
        if (location == null) emptyList()
        else {
            val userPoint = GeoPoint(location.latitude, location.longitude)
            list.sortedBy { repository.calculateDistance(userPoint, it.location ?: GeoPoint(0.0, 0.0)) }.take(10)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var locationCallback: LocationCallback? = null

    init {
        Log.d("AlbBizMap", "ViewModel initialized")
        testFirestoreAndLoad()
        loadFavorites()
    }

    private fun testFirestoreAndLoad() {
        viewModelScope.launch {
            val connected = repository.testConnection()
            if (connected) {
                loadBusinesses()
            } else {
                _isLoading.value = false
                Log.e("AlbBizMap", "Firestore connection failed")
            }
        }
    }

    private fun loadBusinesses() {
        repository.getActiveBusinesses()
            .onEach { firestoreBusinesses ->
                _businesses.value = firestoreBusinesses
                filterBusinesses()
                _isLoading.value = false
            }
            .catch { e ->
                Log.e("AlbBizMap", "Error loading from Firestore: ${e.message}")
                _error.value = e.message
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun loadFavorites() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getFavoriteIds(userId).onSuccess { ids ->
                _favoriteIds.value = ids.toSet()
            }
        }
    }

    fun toggleFavorite(businessId: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val currentlyFavorite = _favoriteIds.value.contains(businessId)
        viewModelScope.launch {
            repository.toggleFavorite(userId, businessId, !currentlyFavorite).onSuccess {
                val newFavorites = _favoriteIds.value.toMutableSet()
                if (currentlyFavorite) newFavorites.remove(businessId) else newFavorites.add(businessId)
                _favoriteIds.value = newFavorites
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterBusinesses()
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
        filterBusinesses()
    }

    private fun filterBusinesses() {
        val query = _searchQuery.value.trim().lowercase()
        val category = _selectedCategory.value.lowercase()
        
        _filteredBusinesses.value = _businesses.value.filter { business ->
            val matchesQuery = if (query.isEmpty()) true else {
                val searchTerms = query.split(" ").filter { it.isNotEmpty() }
                val searchableText = (business.name + " " + 
                                    business.category + " " + 
                                    business.address + " " + 
                                    business.description).lowercase()
                searchTerms.all { term -> searchableText.contains(term) }
            }
            
            val matchesCategory = if (category.isEmpty()) true else business.category.lowercase().contains(category)
            
            matchesQuery && matchesCategory
        }
    }

    fun sortByNearMe() {
        val location = _userLocation.value ?: return
        val userGeoPoint = GeoPoint(location.latitude, location.longitude)
        
        _filteredBusinesses.value = _filteredBusinesses.value.sortedBy { business ->
            business.location?.let { repository.calculateDistance(userGeoPoint, it) } ?: Double.MAX_VALUE
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

    fun getBusinessById(id: String): Business? {
        return _businesses.value.find { it.id == id }
    }
}
