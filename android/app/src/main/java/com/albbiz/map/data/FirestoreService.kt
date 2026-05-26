// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val businessesRef = db.collection("businesses")
    private val usersRef = db.collection("users")
    private val eventsRef = db.collection("events")
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "AlbBizMap"
    }

    fun getActiveBusinesses(): Flow<List<Business>> = callbackFlow {
        Log.d(TAG, "Firestore: Starting to listen for businesses")

        val listener = businessesRef
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore: Error listening for businesses", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val businesses = snapshot.documents.mapNotNull { doc ->
                    try {
                        Business.fromMap(doc.id, doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                }

                val sortedBusinesses = businesses.sortedWith(
                    compareByDescending<Business> { it.isSponsored }
                        .thenByDescending { it.rating }
                )

                trySend(sortedBusinesses)
            }

        awaitClose { listener.remove() }
    }

    suspend fun addBusiness(business: Business): Result<String> {
        return try {
            val auth = Firebase.auth
            var currentUser = auth.currentUser
            
            if (currentUser == null) {
                auth.signInAnonymously().await()
                currentUser = auth.currentUser
            }

            if (currentUser == null) return Result.failure(Exception("Authentication failed"))

            val docRef = if (business.id.isEmpty()) businessesRef.document() else businessesRef.document(business.id)
            val finalBusiness = business.copy(id = docRef.id, ownerId = currentUser.uid)

            docRef.set(finalBusiness.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore: Error adding business", e)
            Result.failure(e)
        }
    }

    // FAVORITES SYSTEM
    suspend fun toggleFavorite(userId: String, businessId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            val favRef = usersRef.document(userId).collection("favorites").document(businessId)
            if (isFavorite) {
                favRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()
            } else {
                favRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFavoriteIds(userId: String): Flow<Set<String>> = callbackFlow {
        val listener = usersRef.document(userId).collection("favorites")
            .addSnapshotListener { snapshot, _ ->
                val ids = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    // ── EVENTS SYSTEM (Section 12) ─────────────────────────────
    fun getEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsRef
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Event.fromMap(doc.id, it) }
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    suspend fun uploadImage(businessId: String, uri: Uri, index: Int): String {
        val filename = "businesses/$businessId/image_$index.jpg"
        val ref = storage.reference.child(filename)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun testConnection(): Boolean {
        return try {
            businessesRef.limit(1).get().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateBusiness(business: Business): Result<String> {
        return try {
            businessesRef.document(business.id).set(business.toMap()).await()
            Result.success(business.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitClaimRequest(claim: ClaimRequest): Result<String> {
        return try {
            val ref = db.collection("claim_requests").document()
            val finalClaim = claim.copy(id = ref.id)
            ref.set(finalClaim.toMap()).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBusinessPhotos(businessId: String, photos: List<String>) {
        businessesRef.document(businessId).update("photos", photos).await()
    }
}
