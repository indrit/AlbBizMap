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

    // A second, independent copy of search/category state for the List View screen.
    // MapScreen and BusinessListScreen both share this one MapViewModel instance (it's
    // hoisted once at the top of the app so the businesses list itself doesn't get
    // refetched every time you switch screens), but search text and category filter
    // were both wired to the SAME _searchQuery/_selectedCategory above — so typing in
    // one screen's search bar instantly showed up in the other's, and neither ever
    // reset when you left that screen, because they were literally the same value.
    // Keeping List View's copy fully separate fixes both: they can no longer bleed
    // into each other, and each can be reset independently when its screen closes.
    private val _listSearchQuery = MutableStateFlow("")
    val listSearchQuery: StateFlow<String> = _listSearchQuery

    // Multi-select, same reasoning as Country/City below — a business matches if
    // its category is ANY of the selected ones (e.g. Restaurant OR Cafe). Only
    // List View's copy is multi-select; the map's own _selectedCategory above stays
    // single-select since that wasn't asked for and touching it risks changing
    // behavior nobody requested there.
    private val _listSelectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val listSelectedCategories: StateFlow<Set<String>> = _listSelectedCategories

    // Location filters — List View only (the map itself is already a location filter
    // by nature, so these don't apply there). Kept as their own pair rather than
    // folded into search, since the UI shows them as a distinct filter section with
    // its own chips, not something you type. Sets rather than single values — a
    // business matches if its country is ANY of the selected countries (e.g. USA
    // OR UK), same OR logic for city, so a user can pick several of each at once.
    private val _listSelectedCountries = MutableStateFlow<Set<String>>(emptySet())
    val listSelectedCountries: StateFlow<Set<String>> = _listSelectedCountries

    private val _listSelectedCities = MutableStateFlow<Set<String>>(emptySet())
    val listSelectedCities: StateFlow<Set<String>> = _listSelectedCities

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

    // startLocationUpdates is now called from both the splash screen (to start the
    // fetch as early as possible, in parallel with the splash video) and MapScreen's
    // own effect (kept as a fallback in case that ever changes) — this guards against
    // registering two separate FusedLocationProviderClient callbacks for the same
    // session, which would otherwise both fire on every location update.
    private var locationUpdatesStarted = false

    // ── DISCOVERY FLOWS ───────────────────────────────────────────
    val featured: StateFlow<List<Business>> = _businesses
        .mapLatest { list -> list.filter { it.isFeatured || it.isSponsored }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val recentlyAdded: StateFlow<List<Business>> = _businesses
        .mapLatest { list -> list.sortedByDescending { it.id }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Renamed from topRated — the map screen's "Most Favorited Worldwide" section
    // was labeled as favorites but was actually sorting by star rating (a
    // different field entirely from likeCount). Now sorts by actual like count
    // so the section does what its label promises. (BusinessListScreen's
    // separate "Top Rated" discovery row is unrelated to this and still
    // correctly sorts by rating — not touched.)
    val mostFavorited: StateFlow<List<Business>> = _businesses
        .mapLatest { list -> list.sortedByDescending { it.likeCount }.take(10) }
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


    // "Top Recommended" on the map screen — Sponsored and Featured businesses
    // ONLY (not Premium: per the actual plan copy in SubscriptionScreen.kt,
    // Premium's benefits are profile fields like phone/email/hours, not
    // placement — Featured and Sponsored are the two tiers that explicitly
    // promise "featured in discovery row" / "top of search results").
    // Deliberately no distance cutoff, unlike nearMe above: these businesses
    // paid for visibility, and with the app still early and listings possibly
    // spread across many countries, a hard local radius could leave this
    // section empty for most users and mean a paying business gets shown to
    // almost no one. Distance is used only as a tiebreaker within each tier —
    // closer paid businesses still rank first, but nothing is excluded purely
    // for being far away. Falls back to Double.MAX_VALUE when location isn't
    // available yet, so the tier sort still works before a location fix lands.
    val topPicks = combine(
        _businesses, _userLocation
    ) { list: List<Business>, location: LatLng? ->
        val userPoint = location?.let { GeoPoint(it.latitude, it.longitude) }
        list.filter { it.isSponsored || it.isFeatured }
            .map { business ->
                val distance = userPoint?.let {
                    repository.calculateDistance(it, business.location ?: GeoPoint(0.0, 0.0))
                } ?: Double.MAX_VALUE
                business to distance
            }
            .sortedWith(
                compareByDescending<Pair<Business, Double>> { it.first.isSponsored }
                    .thenByDescending { it.first.isFeatured }
                    .thenBy { it.second }
            ).take(10).map { it.first }
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

    // Same filter logic as filteredBusinesses above, just driven by List View's own
    // independent search/category state instead of the map's.
    val listFilteredBusinesses: StateFlow<List<Business>> = combine(
        _businesses,
        _listSearchQuery,
        _listSelectedCategories,
        _listSelectedCountries,
        _listSelectedCities
    ) { list, query, categories, countries, cities ->
        list.filter { business ->
            val matchesQuery = query.isEmpty() ||
                    business.name.contains(query, ignoreCase = true) ||
                    business.category.contains(query, ignoreCase = true) ||
                    business.address.contains(query, ignoreCase = true)
            val matchesCategory = categories.isEmpty() ||
                    categories.any { business.category.equals(it, ignoreCase = true) }
            val matchesCountry = countries.isEmpty() ||
                    countries.any { business.country.equals(it, ignoreCase = true) }
            val matchesCity = cities.isEmpty() ||
                    cities.any { business.city.equals(it, ignoreCase = true) }
            matchesQuery && matchesCategory && matchesCountry && matchesCity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Distinct country/city values to populate the Location filter chips —
    // derived from whatever's currently loaded rather than a hardcoded list, so it
    // stays correct as businesses are added from new places. City options narrow to
    // whichever countries are currently selected (any of them, not just one — falls
    // back to all cities when no country is selected), matching the two-level way
    // this is usually shown in other directory apps.
    val availableCountries: StateFlow<List<String>> = _businesses.mapLatest { list ->
        list.map { it.country }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val availableCities: StateFlow<List<String>> = combine(
        _businesses, _listSelectedCountries
    ) { list, countries ->
        list.filter { countries.isEmpty() || countries.any { c -> it.country.equals(c, ignoreCase = true) } }
            .map { it.city }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
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

    // Public like counter — separate from favorites above. This wraps
    // BusinessRepository.toggleBusinessLike, which does its add/remove +
    // increment/decrement inside a Firestore transaction (see the comment on
    // FirestoreService.toggleBusinessLike for why a plain get-then-update isn't
    // safe here). No optimistic local state needed the way favorites has one:
    // _businesses is already a live snapshot listener (see loadBusinesses), so
    // once the transaction commits, the updated likedBy/likeCount on the business
    // document flows back through that listener on its own.
    fun toggleBusinessLike(businessId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            repository.toggleBusinessLike(userId, businessId)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    fun onListSearchQueryChange(query: String) {
        _listSearchQuery.value = query
    }

    fun onListCategoryToggle(category: String) {
        _listSelectedCategories.value = _listSelectedCategories.value.let { current ->
            if (current.any { it.equals(category, ignoreCase = true) }) {
                current.filterNot { it.equals(category, ignoreCase = true) }.toSet()
            } else {
                current + category
            }
        }
    }

    fun onListCategoryClearAll() {
        _listSelectedCategories.value = emptySet()
    }

    // Toggles one country in/out of the selected set — tapping "USA" then "UK"
    // selects both, tapping either again deselects just that one.
    fun onListCountryToggle(country: String) {
        _listSelectedCountries.value = _listSelectedCountries.value.let { current ->
            if (current.any { it.equals(country, ignoreCase = true) }) {
                current.filterNot { it.equals(country, ignoreCase = true) }.toSet()
            } else {
                current + country
            }
        }
    }

    // The "All" chip — clears every selected country. Also clears cities, since a
    // city selected under a now-cleared country (e.g. "Toronto" with no country
    // filter left) would otherwise sit there silently constraining results.
    fun onListCountryClearAll() {
        _listSelectedCountries.value = emptySet()
        _listSelectedCities.value = emptySet()
    }

    fun onListCityToggle(city: String) {
        _listSelectedCities.value = _listSelectedCities.value.let { current ->
            if (current.any { it.equals(city, ignoreCase = true) }) {
                current.filterNot { it.equals(city, ignoreCase = true) }.toSet()
            } else {
                current + city
            }
        }
    }

    fun onListCityClearAll() {
        _listSelectedCities.value = emptySet()
    }

    // Called when List View closes, so reopening it always starts from a clean
    // search box and "All" category/country/city instead of remembering whatever
    // was typed/selected last time — same "reset on close" behavior the map's own
    // search already had, now applied to List View's independent copy too.
    fun resetListFilters() {
        _listSearchQuery.value = ""
        _listSelectedCategories.value = emptySet()
        _listSelectedCountries.value = emptySet()
        _listSelectedCities.value = emptySet()
    }

    fun getBusinessById(id: String): Business? {
        return _businesses.value.find { it.id == id }
    }

    fun startLocationUpdates(context: Context) {
        if (locationUpdatesStarted) return
        locationUpdatesStarted = true

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