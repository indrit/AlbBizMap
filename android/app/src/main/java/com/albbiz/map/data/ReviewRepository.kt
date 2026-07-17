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

            // The read (checking reportedBy) and the write (increment + arrayUnion)
            // need to happen atomically — otherwise two near-simultaneous reports (or
            // one report double-fired by a rapid double-tap) can both read "not yet
            // reported" before either write lands, and reportCount gets incremented
            // twice for what should count as a single report from that user. A
            // transaction makes Firestore retry the whole read-check-write as one
            // unit if it detects a conflicting write in between.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(reviewRef)
                val review = snapshot.data?.let { Review.fromMap(reviewId, it) }
                    ?: throw Exception("Review not found")

                if (userId in review.reportedBy) {
                    throw Exception("You have already reported this review")
                }

                transaction.update(
                    reviewRef,
                    mapOf(
                        "reportCount" to FieldValue.increment(1),
                        "reportedBy" to FieldValue.arrayUnion(userId)
                    )
                )
                null
            }.await()

            Log.d(TAG, "Review $reviewId reported by $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error reporting review", e)
            Result.failure(e)
        }
    }
    suspend fun toggleLike(
        businessId: String,
        reviewId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val reviewRef = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)

            // Same TOCTOU concern as reportReview: without a transaction, two rapid
            // taps (like-then-unlike, or the same tap double-firing) can both read
            // the same stale "not liked" state and both decide to arrayUnion,
            // leaving the review liked when the user actually meant to end up
            // unliked. The transaction makes the read-then-decide-then-write atomic.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(reviewRef)
                val review = snapshot.data?.let { Review.fromMap(reviewId, it) }
                    ?: throw Exception("Review not found")

                if (userId in review.likedBy) {
                    transaction.update(reviewRef, "likedBy", FieldValue.arrayRemove(userId))
                } else {
                    transaction.update(reviewRef, "likedBy", FieldValue.arrayUnion(userId))
                }
                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
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

    // GET REPLIES FOR A REVIEW
    fun getReplies(businessId: String, reviewId: String): Flow<List<Reply>> = callbackFlow {
        val ref = db.collection("businesses")
            .document(businessId)
            .collection("reviews")
            .document(reviewId)
            .collection("replies")

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val replies = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.let { Reply.fromMap(doc.id, it) }
            }?.sortedBy { it.createdAt } ?: emptyList()
            trySend(replies)
        }
        awaitClose { listener.remove() }
    }

    // ADD A REPLY
    suspend fun addReply(
        businessId: String,
        reviewId: String,
        reply: Reply
    ): Result<String> {
        return try {
            val ref = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)
                .collection("replies")
                .document()

            val finalReply = reply.copy(id = ref.id)
            ref.set(finalReply.toMap()).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding reply", e)
            Result.failure(e)
        }
    }

    // TOGGLE LIKE ON A REPLY
    suspend fun toggleReplyLike(
        businessId: String,
        reviewId: String,
        replyId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val replyRef = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)
                .collection("replies")
                .document(replyId)

            // Same TOCTOU concern as Review.toggleLike above.
            db.runTransaction { transaction ->
                val snapshot = transaction.get(replyRef)
                val reply = snapshot.data?.let { Reply.fromMap(replyId, it) }
                    ?: throw Exception("Reply not found")

                if (userId in reply.likedBy) {
                    transaction.update(replyRef, "likedBy", FieldValue.arrayRemove(userId))
                } else {
                    transaction.update(replyRef, "likedBy", FieldValue.arrayUnion(userId))
                }
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling reply like", e)
            Result.failure(e)
        }
    }
}