// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.net.Uri
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class BusinessRepository {
    private val firestoreService = FirestoreService()

    fun getActiveBusinesses(): Flow<List<Business>> {
        return firestoreService.getActiveBusinesses()
            .catch { e ->
                android.util.Log.e("AlbBizMap", "Repository: Error in flow", e)
                emit(emptyList())
            }
    }

    suspend fun addBusiness(business: Business): Result<String> {
        return firestoreService.addBusiness(business)
    }

    suspend fun uploadBusinessImages(
        businessId: String,
        imageUris: List<Uri>
    ): Result<List<String>> {
        return try {
            val urls = imageUris.mapIndexed { index, uri ->
                firestoreService.uploadImage(businessId, uri, index)
            }
            Result.success(urls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection() = firestoreService.testConnection()

    fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val R = 6371 // Earth's radius in km
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)
        val dLat = Math.toRadians(point2.latitude - point1.latitude)
        val dLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}