// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class BusinessRepository {
    private val firestoreService = FirestoreService()
    private val db = FirebaseFirestore.getInstance()

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

    suspend fun updateBusiness(business: Business): Result<String> {
        return firestoreService.updateBusiness(business)
    }

    suspend fun submitClaim(claim: ClaimRequest): Result<String> {
        return firestoreService.submitClaimRequest(claim)
    }

    // ── FAVORITES SYSTEM ──────────────────────────────────────
    suspend fun toggleFavorite(userId: String, businessId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)
            if (isFavorite) {
                userRef.update("favorites", FieldValue.arrayUnion(businessId)).await()
            } else {
                userRef.update("favorites", FieldValue.arrayRemove(businessId)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // If the user document doesn't exist yet, create it
            try {
                db.collection("users").document(userId).set(
                    mapOf("favorites" to if (isFavorite) listOf(businessId) else emptyList<String>())
                ).await()
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    suspend fun getFavoriteIds(userId: String): Result<List<String>> {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            val favorites = snapshot.get("favorites") as? List<*>
            Result.success(favorites?.filterIsInstance<String>() ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBusinessPhoto(
        businessId: String,
        imageUri: Uri
    ): Result<String> {
        return try {
            val url = firestoreService.uploadImage(businessId, imageUri, 0)
            firestoreService.updateBusinessPhotos(businessId, listOf(url))
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUserAdmin(userId: String): Boolean {
        return firestoreService.isUserAdmin(userId)
    }

    fun getClaimRequests(): Flow<List<ClaimRequest>> {
        return firestoreService.getClaimRequests()
    }

    suspend fun approveClaim(claim: ClaimRequest): Result<Unit> {
        return firestoreService.approveClaim(claim)
    }

    suspend fun rejectClaim(claimId: String): Result<Unit> {
        return firestoreService.rejectClaim(claimId)
    }

    suspend fun seedBusinessesFromJson(context: android.content.Context): Result<Int> {
        return firestoreService.seedBusinessesFromJson(context)
    }
}
