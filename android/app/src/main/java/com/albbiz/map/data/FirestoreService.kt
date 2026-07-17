// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
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

    fun getBusinessesByOwner(ownerId: String): Flow<List<Business>> = callbackFlow {
        val listener = businessesRef
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore: Error listening for owner businesses", error)
                    close(error)
                    return@addSnapshotListener
                }
                val businesses = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Business.fromMap(doc.id, doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(businesses)
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
    suspend fun toggleBusinessLike(userId: String, businessId: String): Result<Unit> {
        return try {
            val businessRef = businessesRef.document(businessId)
            // A plain get()-then-update() reads a stale snapshot: two rapid taps (or
            // two devices) can both read "not liked" before either write lands, so
            // both then run arrayUnion + increment(1) — likeCount ends up incremented
            // twice while likedBy (a set) only actually gained the one user, leaving
            // the count and the array permanently out of sync. Doing the read and the
            // decision inside a transaction makes Firestore retry the whole
            // read-decide-write atomically if another write lands in between, so only
            // one of two racing calls actually toggles the like.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(businessRef)
                val likedBy = (snapshot.get("likedBy") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()

                if (userId in likedBy) {
                    transaction.update(
                        businessRef,
                        mapOf(
                            "likedBy" to FieldValue.arrayRemove(userId),
                            "likeCount" to FieldValue.increment(-1)
                        )
                    )
                } else {
                    transaction.update(
                        businessRef,
                        mapOf(
                            "likedBy" to FieldValue.arrayUnion(userId),
                            "likeCount" to FieldValue.increment(1)
                        )
                    )
                }
                null
            }.await()
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

    suspend fun addEvent(event: Event): Result<String> {
        return try {
            val ref = eventsRef.document()
            val finalEvent = event.copy(id = ref.id)
            ref.set(finalEvent.toMap()).await()
            Log.d(TAG, "Event added: ${ref.id}")
            Result.success(ref.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding event", e)
            Result.failure(e)
        }
    }

    // ── ADMIN FUNCTIONS ───────────────────────────────────────
    suspend fun isUserAdmin(userId: String): Boolean {
        return try {
            val doc = usersRef.document(userId).get().await()
            doc.getBoolean("isAdmin") ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun getClaimRequests(): Flow<List<ClaimRequest>> = callbackFlow {
        val listener = db.collection("claim_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val claims = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let {
                        ClaimRequest(
                            id = doc.id,
                            businessId = it["businessId"] as? String ?: "",
                            businessName = it["businessName"] as? String ?: "",
                            userId = it["userId"] as? String ?: "",
                            userName = it["userName"] as? String ?: "",
                            userEmail = it["userEmail"] as? String ?: "",
                            reason = it["reason"] as? String ?: "",
                            status = it["status"] as? String ?: "pending",
                            createdAt = (it["createdAt"] as? Number)?.toLong() ?: 0L
                        )
                    }
                } ?: emptyList()
                trySend(claims)
            }
        awaitClose { listener.remove() }
    }

    suspend fun approveClaim(claim: ClaimRequest): Result<Unit> {
        return try {
            // Update business ownerId and set isVerified
            businessesRef.document(claim.businessId).update(
                mapOf(
                    "ownerId" to claim.userId,
                    "isVerified" to true
                )
            ).await()

            // Update claim status to approved
            db.collection("claim_requests").document(claim.id)
                .update("status", "approved").await()

            Log.d(TAG, "Claim approved for business: ${claim.businessId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error approving claim", e)
            Result.failure(e)
        }
    }

    suspend fun rejectClaim(claimId: String): Result<Unit> {
        return try {
            db.collection("claim_requests").document(claimId)
                .update("status", "rejected").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedBusinessesFromJson(context: android.content.Context): Result<Int> {
        return try {
            val jsonString = context.assets.open("sample_businesses.json")
                .bufferedReader().use { it.readText() }

            val jsonArray = org.json.JSONArray(jsonString)
            var count = 0

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val docRef = businessesRef.document()

                val business = mapOf(
                    "id" to docRef.id,
                    "name" to obj.getString("name"),
                    "category" to obj.getString("category"),
                    "description" to obj.getString("description"),
                    "address" to obj.getString("address"),
                    "phone" to obj.getString("phone"),
                    "email" to obj.getString("email"),
                    "website" to obj.getString("website"),
                    "location" to com.google.firebase.firestore.GeoPoint(
                        obj.getDouble("latitude"),
                        obj.getDouble("longitude")
                    ),
                    "isActive" to obj.getBoolean("isActive"),
                    "isVerified" to obj.getBoolean("isVerified"),
                    "isAlbanianOwned" to obj.getBoolean("isAlbanianOwned"),
                    "isPremium" to obj.getBoolean("isPremium"),
                    "isSponsored" to obj.getBoolean("isSponsored"),
                    "isFeatured" to obj.getBoolean("isFeatured"),
                    "rating" to 0.0,
                    "reviewCount" to 0,
                    "ownerId" to "",
                    "photos" to emptyList<String>(),
                    "workingHours" to emptyMap<String, String>(),
                    "isOpen24Hours" to false,
                    "longDescription" to "",
                    "promotions" to emptyList<String>(),
                    "jobs" to emptyList<String>()
                )

                docRef.set(business).await()
                count++
            }

            Log.d(TAG, "Seeded $count businesses successfully")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding businesses", e)
            Result.failure(e)
        }
    }
}
