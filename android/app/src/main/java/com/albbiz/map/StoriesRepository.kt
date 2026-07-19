// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class StoriesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storiesRef = db.collection("stories")
    private val storage = FirebaseStorage.getInstance()

    // ── GET ACTIVE STORIES (not expired) ─────────────────────────────
    fun getActiveStories(): Flow<List<Story>> = callbackFlow {
        val now = System.currentTimeMillis()
        val listener = storiesRef
            .whereGreaterThan("expiresAt", now)
            .orderBy("expiresAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val stories = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Story.fromMap(doc.id, it) }
                } ?: emptyList()
                trySend(stories)
            }
        awaitClose { listener.remove() }
    }.catch { e ->
        Log.e("AlbBizMap", "StoriesRepo: Error getting stories", e)
        emit(emptyList())
    }

    // ── ADD STORY ─────────────────────────────────────────────────────
    suspend fun addStory(story: Story, photoUris: List<Uri>): Result<String> {
        return try {
            val storyRef = storiesRef.document()
            val storyId = storyRef.id

            // Upload photos to Firebase Storage
            val photoUrls = photoUris.mapIndexed { index, uri ->
                val filename = "stories/$storyId/photo_$index.jpg"
                val ref = storage.reference.child(filename)
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }

            val finalStory = story.copy(
                id = storyId,
                photos = photoUrls
            )

            storyRef.set(finalStory.toMap()).await()
            Result.success(storyId)
        } catch (e: Exception) {
            Log.e("AlbBizMap", "StoriesRepo: Error adding story", e)
            Result.failure(e)
        }
    }

    // ── MARK STORY AS VIEWED ──────────────────────────────────────────
    suspend fun markStoryViewed(storyId: String, userId: String): Result<Unit> {
        return try {
            storiesRef.document(storyId)
                .update("viewedBy", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AlbBizMap", "StoriesRepo: Error marking story viewed", e)
            Result.failure(e)
        }
    }

    // ── DELETE STORY ──────────────────────────────────────────────────
    suspend fun deleteStory(storyId: String): Result<Unit> {
        return try {
            storiesRef.document(storyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AlbBizMap", "StoriesRepo: Error deleting story", e)
            Result.failure(e)
        }
    }

    // ── DELETE EXPIRED STORIES ────────────────────────────────────────
    // Only ever attempts to delete the current user's own expired stories.
    // This used to query every expired story app-wide and try to delete
    // all of them, but Firestore's security rules correctly block a user
    // from deleting someone else's document — so any story that wasn't
    // yours failed with PERMISSION_DENIED. Worse, since a single failed
    // delete() throws mid-loop, one other person's story appearing before
    // yours in the results could silently stop your own from being
    // cleaned up too, depending on document order. Filtering the query to
    // just this user's stories means every document it returns is one
    // it's actually allowed to delete, so the loop never aborts partway.
    // Note: this is a compound query (userId == X AND expiresAt < now) —
    // if it ever fails with a "requires an index" error, Firestore's own
    // error message includes a direct link to create that index in the
    // Firebase Console.
    suspend fun deleteExpiredStories(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.success(Unit)
        return try {
            val now = System.currentTimeMillis()
            val expired = storiesRef
                .whereEqualTo("userId", userId)
                .whereLessThan("expiresAt", now)
                .get()
                .await()
            expired.documents.forEach { it.reference.delete().await() }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AlbBizMap", "StoriesRepo: Error deleting expired stories", e)
            Result.failure(e)
        }
    }
}