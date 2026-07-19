// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
class MapViewModel : ViewModel() {

    private val repository = BusinessRepository()

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    // Whether we've already done the one-time "animate camera to the user's location"
    // move this session. Deliberately NOT a StateFlow/Compose state — this ViewModel
    // outlives MapScreen's composition (it's hoisted at the Activity/NavHost level), so
    // storing this flag here means it survives navigating away from and back to the map,
    // instead of resetting (and re-triggering the animation) every single time, which is
    // what a plain `remember` in MapScreen was doing before.
    var hasMovedToInitialLocation: Boolean = false

    // ── DISCOVERY FLOWS ───────────────────────────────────────────
    val featured: StateFlow<List<Business>> = _businesses
        .mapLatest { list -> list.filter { it.isFeatured || it.isSponsored }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val recentlyAdded: StateFlow<List<Business>> = _businesses
        .mapLatest { list -> list.sortedByDescending { it.id }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val topRated: StateFlow<List<Business>> = _businesses
        .mapLatest { list -> list.sortedByDescending { it.rating }.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Both combine() blocks below re-run on every _userLocation tick (every 5s while
    // location updates are active — see startLocationUpdates). SharingStarted.Lazily
    // meant that once started (the first time the map screen was ever visited), this
    // kept recomputing forever in the background — filtering + sorting the whole
    // business list, calculating distance TWICE per business (once to filter, again
    // in the sort comparator) — even while the user was on a completely unrelated
    // screen. That's a steady stream of allocations every 5 seconds for the rest of
    // the app session, which is exactly the kind of thing that shows up later as GC
    // pause pressure landing at an unrelated, unlucky moment (confirmed via System
    // Trace: HeapTaskDaemon doing multiple seconds of concurrent mark-compact GC,
    // main thread stalling on ART's ClassLinker/InternTable locks it holds mid-GC).
    // WhileSubscribed(5000) stops the recomputation once nobody's actually collecting
    // it (e.g. navigated away from the map), instead of running unconditionally for
    // the whole app lifetime. The distance is also now computed once per business and
    // reused for both the filter and the sort, instead of twice.
    val nearMe = combine(
        _businesses, _userLocation
    ) { list: List<Business>, location: LatLng? ->
        if (location == null) emptyList<Business>()
        else {
            val userPoint = GeoPoint(location.latitude, location.longitude)
            list.map { business -> business to repository.calculateDistance(userPoint, business.location ?: GeoPoint(0.0, 0.0)) }
                .filter { (_, distance) -> distance <= 50.0 }
                .sortedWith(
                    compareByDescending<Pair<Business, Double>> { it.first.isSponsored }
                        .thenByDescending { it.first.isFeatured }
                        .thenBy { it.second }
                ).take(10).map { it.first }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Business>())


    val topPicks = combine(
        _businesses, _userLocation
    ) { list: List<Business>, location: LatLng? ->
        if (location == null) emptyList<Business>()
        else {
            val userPoint = GeoPoint(location.latitude, location.longitude)
            list.map { business -> business to repository.calculateDistance(userPoint, business.location ?: GeoPoint(0.0, 0.0)) }
                .filter { (business, distance) -> (business.isSponsored || business.isFeatured || business.isPremium) && distance <= 50.0 }
                .sortedWith(
                    compareByDescending<Pair<Business, Double>> { it.first.isSponsored }
                        .thenByDescending { it.first.isFeatured }
                        .thenByDescending { it.first.isPremium }
                        .thenBy { it.second }
                ).take(10).map { it.first }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Business>())

    val filteredBusinesses: StateFlow<List<Business>> = combine(
        _businesses,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { business ->
            val matchesQuery = query.isEmpty() ||
                    business.name.contains(query, ignoreCase = true) ||
                    business.category.contains(query, ignoreCase = true) ||
                    business.address.contains(query, ignoreCase = true)
            val matchesCategory = category.isEmpty() ||
                    business.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadBusinesses()
        loadFavorites()
    }

    private fun loadBusinesses() {
        _isLoading.value = true
        repository.getActiveBusinesses()
            .onEach { list ->
                _businesses.value = list
                _isLoading.value = false
            }
            .catch { _isLoading.value = false }
            .launchIn(viewModelScope)
    }

    // Tracked so toggleFavorite() (below) can wait for this one-shot initial fetch to
    // land before applying its own optimistic update on top of it.
    private var loadFavoritesJob: Job? = null

    private fun loadFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        loadFavoritesJob = viewModelScope.launch {
            repository.getFavoriteIds(userId)
                .onSuccess { ids ->
                    _favoriteIds.value = ids.toSet()
                }
                .onFailure { e ->
                    android.util.Log.e("MapViewModel", "Error loading favorites", e)
                }
        }
    }
    fun toggleFavorite(businessId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            // If the user taps a favorite icon before the initial getFavoriteIds()
            // fetch (started once, from init) has resolved, this optimistic update
            // could otherwise get silently clobbered when that fetch finally lands
            // and overwrites _favoriteIds wholesale with the (now-stale) server
            // snapshot. Waiting here means this always applies on top of the real
            // starting state instead of racing it. join() returns immediately if the
            // load already finished.
            loadFavoritesJob?.join()

            val current = _favoriteIds.value.toMutableSet()
            val isCurrentlyFavorite = businessId in current
            if (isCurrentlyFavorite) {
                current.remove(businessId)
            } else {
                current.add(businessId)
            }
            _favoriteIds.value = current
            repository.toggleFavorite(userId, businessId, isCurrentlyFavorite)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    fun getBusinessById(id: String): Business? {
        return _businesses.value.find { it.id == id }
    }

    fun startLocationUpdates(context: Context) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        // ── GET LAST KNOWN LOCATION IMMEDIATELY ───────────────────
        try {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _userLocation.value = LatLng(it.latitude, it.longitude)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("MapViewModel", "Location permission missing", e)
        }

        // ── CONTINUOUS UPDATES ────────────────────────────────────
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10_000L
        ).apply {
            setMinUpdateIntervalMillis(5_000L)
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _userLocation.value = LatLng(location.latitude, location.longitude)
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            android.util.Log.e("MapViewModel", "Location permission missing", e)
        }
    }
}