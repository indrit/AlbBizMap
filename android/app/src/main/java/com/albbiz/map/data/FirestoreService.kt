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
                    Log.d(TAG, "Firestore: Snapshot is null")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                Log.d(TAG, "Firestore: Got ${snapshot.documents.size} documents")

                val businesses = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data == null) {
                            Log.w(TAG, "Firestore: Doc ${doc.id} has no data")
                            return@mapNotNull null
                        }

                        Business.fromMap(doc.id, data)
                    } catch (e: Exception) {
                        Log.e(TAG, "Firestore: Error parsing doc ${doc.id}", e)
                        null
                    }
                }

                val sortedBusinesses = businesses.sortedWith(
                    compareByDescending<Business> { it.isSponsored }
                        .thenByDescending { it.rating }
                )

                Log.d(TAG, "Firestore: Successfully parsed ${sortedBusinesses.size} businesses")
                trySend(sortedBusinesses)
            }

        awaitClose {
            Log.d(TAG, "Firestore: Removing listener")
            listener.remove()
        }
    }

    suspend fun addBusiness(business: Business): Result<String> {
        return try {
            val auth = Firebase.auth
            var currentUser = auth.currentUser
            
            // Critical check: Ensure we are logged in before writing
            if (currentUser == null) {
                Log.d(TAG, "Firestore: No user found, attempting silent sign-in...")
                auth.signInAnonymously().await()
                currentUser = auth.currentUser
            }

            if (currentUser == null) {
                return Result.failure(Exception("Authentication failed"))
            }

            Log.d(TAG, "Firestore: Adding business ${business.name} for user ${currentUser.uid}")
            
            val docRef = if (business.id.isEmpty()) {
                businessesRef.document()
            } else {
                businessesRef.document(business.id)
            }

            // Ensure the business document has the correct ownerId
            val finalBusiness = business.copy(
                id = docRef.id,
                ownerId = currentUser.uid
            )

            docRef.set(finalBusiness.toMap()).await()
            Log.d(TAG, "Firestore: Business added with ID ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore: Error adding business", e)
            Result.failure(e)
        }
    }

    suspend fun uploadImage(businessId: String, uri: Uri, index: Int): String {
        val filename = "businesses/$businessId/image_$index.jpg"
        val ref = storage.reference.child(filename)

        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun testConnection(): Boolean {
        return try {
            Log.d(TAG, "Firestore: Testing connection...")
            val snapshot = businessesRef.limit(1).get().await()
            Log.d(TAG, "Firestore: Connection test successful, found ${snapshot.size()} docs")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore: Connection test failed", e)
            false
        }
    }
}