// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.net.Uri
import android.util.Log
import com.albbiz.map.ui.CurrentLanguage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReviewRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
    suspend fun addReview(
        businessId: String,
        review: Review,
        photoUris: List<Uri> = emptyList()
    ): Result<String> {
        return try {
            val ref = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document()

            // Same upload-then-write shape as StoriesRepository.addStory — the
            // doc ID is generated up front (via .document(), before .set()) so
            // uploaded photos can live under a path keyed by the review's real
            // ID rather than a temp one.
            val photoUrls = photoUris.mapIndexed { index, uri ->
                val ref2 = storage.reference.child("reviews/$businessId/${ref.id}/photo_$index.jpg")
                ref2.putFile(uri).await()
                ref2.downloadUrl.await().toString()
            }

            val finalReview = review.copy(id = ref.id, photos = photoUrls)
            ref.set(finalReview.toMap()).await()
            updateBusinessStats(businessId)
            Result.success(ref.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding review", e)
            Result.failure(e)
        }
    }

    // EDIT A REVIEW — Firestore rules already let a review's own author update
    // any field on it, so no rules change was needed for this.
    suspend fun updateReview(
        businessId: String,
        reviewId: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        return try {
            val reviewRef = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)
            reviewRef.update(
                mapOf(
                    "rating" to rating,
                    "comment" to comment
                )
            ).await()
            updateBusinessStats(businessId) // rating changed, average needs recomputing
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating review", e)
            Result.failure(e)
        }
    }

    // DELETE A REVIEW
    suspend fun deleteReview(businessId: String, reviewId: String): Result<Unit> {
        return try {
            val reviewRef = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)

            val snapshot = reviewRef.get().await()
            val review = snapshot.data?.let { Review.fromMap(reviewId, it) }

            reviewRef.delete().await()

            // Best-effort cleanup of the review's own uploaded photos, same
            // non-fatal pattern as EventsRepository.deleteEvent. Replies other
            // users left under this review are NOT cascade-deleted here — a
            // reply's delete rule only allows its own author, so batch-deleting
            // someone else's reply would fail the whole operation. They're left
            // as orphaned documents nothing in the UI ever surfaces again,
            // rather than risking a partial/failed delete.
            review?.photos?.forEach { url ->
                try {
                    storage.getReferenceFromUrl(url).delete().await()
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting review photo", e)
                }
            }

            updateBusinessStats(businessId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting review", e)
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
                    ?: throw Exception(CurrentLanguage.strings().reviewNotFound)

                if (userId in review.reportedBy) {
                    throw Exception(CurrentLanguage.strings().alreadyReportedReview)
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
                    ?: throw Exception(CurrentLanguage.strings().reviewNotFound)

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

    // EDIT A REPLY (author only, enforced by Firestore rules)
    suspend fun updateReply(
        businessId: String,
        reviewId: String,
        replyId: String,
        comment: String
    ): Result<Unit> {
        return try {
            val replyRef = db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)
                .collection("replies")
                .document(replyId)
            replyRef.update("comment", comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reply", e)
            Result.failure(e)
        }
    }

    // DELETE A REPLY (author only). No photos or business stats involved here,
    // unlike deleteReview — replies don't carry either.
    suspend fun deleteReply(
        businessId: String,
        reviewId: String,
        replyId: String
    ): Result<Unit> {
        return try {
            db.collection("businesses")
                .document(businessId)
                .collection("reviews")
                .document(reviewId)
                .collection("replies")
                .document(replyId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reply", e)
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
                    ?: throw Exception(CurrentLanguage.strings().replyNotFound)

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