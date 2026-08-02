// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.utils

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

// Resolves a free-text address (ideally "street, city, country" combined, so the
// device's geocoding backend has enough to disambiguate) into coordinates, so the
// Add/Edit Business forms never have to ask the user to type raw latitude/longitude
// — modern directory apps (Yelp, Google Business Profile, etc.) all do this behind
// the scenes instead of exposing coordinates as a user-facing field.
//
// Uses the classic synchronous Geocoder.getFromLocationName — it's deprecated in
// favor of an async-callback overload, but that overload only exists on API 33+
// and this app's minSdk is 24, so the classic form is what actually works across
// the app's supported range. Running it under Dispatchers.IO is the correct way to
// use a blocking/network-backed API like this from a coroutine, deprecation aside.
object GeocodingUtils {

    @Suppress("DEPRECATION")
    suspend fun geocodeAddress(
        context: Context,
        address: String,
        city: String,
        country: String
    ): LatLng? = withContext(Dispatchers.IO) {
        val query = listOf(address, city, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        if (query.isBlank()) return@withContext null

        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(query, 1)
            results?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            // No network, no geocoding backend on this device, malformed query, etc.
            // — caller treats a null return as "couldn't resolve" and shows the user
            // a retry-able error rather than silently saving a wrong/default location.
            null
        }
    }
}
