// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReviewRepository {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AlbBizMap-ReviewRepo"
        private const val REPORT_THRESHOLD = 3 // ← auto-hide after 3 reports
    }

    // LISTEN TO REVIEWS IN REAL TIME
    fun getReviews(businessId: String): Flow<List<Review>> = callbackFlow {
        val ref = db.collection("businesses")
            .document(businessId)
            .collection("reviews")

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening for reviews", error)
                close(error)
                return@addSnapshotListener
            }

            val reviews = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.let { Review.fromMap(doc.id, it) }
            } ?: emptyList()

            // Filter out hidden reviews (3+ reports) before sending to UI
            val visibleReviews = reviews
                .filter { it.reportCount < REPORT_THRESHOLD }
                .sortedByDescending { it.createdAt }

            trySend(visibleReviews)
        }

        awaitClose { listener.remove() }
    }

    // ADD A NEW REVIEW
    suspend fun addReview(businessId: String, review: Review): Result<String> {
        return try {
            val ref = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document()

            val finalReview = review.copy(id = ref.id)
            ref.set(finalReview.toMap()).await()
            updateBusinessStats(businessId)
            Result.success(ref.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding review", e)
            Result.failure(e)
        }
    }

    // REPORT A REVIEW
    suspend fun reportReview(
        businessId: String,
        reviewId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val reviewRef = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)

            val snapshot = reviewRef.get().await()
            val review = snapshot.data?.let { Review.fromMap(reviewId, it) }
                ?: return Result.failure(Exception("Review not found"))

            // Check if user already reported this review
            if (userId in review.reportedBy) {
                return Result.failure(Exception("You have already reported this review"))
            }

            // Increment reportCount and add userId to reportedBy list
            reviewRef.update(
                mapOf(
                    "reportCount" to FieldValue.increment(1),
                    "reportedBy" to FieldValue.arrayUnion(userId)
                )
            ).await()

            Log.d(TAG, "Review $reviewId reported by $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error reporting review", e)
            Result.failure(e)
        }
    }

    // UPDATE BUSINESS RATING + REVIEW COUNT
    private suspend fun updateBusinessStats(businessId: String) {
        try {
            val businessRef = db.collection("businesses").document(businessId)
            val reviewsRef = businessRef.collection("reviews")

            val snapshot = reviewsRef.get().await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { Review.fromMap(doc.id, it) }
            }.filter { it.reportCount < REPORT_THRESHOLD } // only count visible reviews

            val avgRating = if (reviews.isNotEmpty()) {
                reviews.map { it.rating }.average()
            } else 0.0

            val count = reviews.size

            businessRef.update(
                mapOf(
                    "rating" to avgRating,
                    "reviewCount" to count
                )
            ).await()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating business stats", e)
        }
    }
}