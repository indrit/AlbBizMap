// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class EventsRepository {
    private val firestoreService = FirestoreService()
    private val storage = FirebaseStorage.getInstance()

    fun getEvents(): Flow<List<Event>> {
        return firestoreService.getEvents()
            .catch { e ->
                Log.e("AlbBizMap", "EventsRepo: Error in flow", e)
                emit(emptyList())
            }
    }

    suspend fun addEvent(event: Event, imageUri: Uri?): Result<String> {
        return try {
            // Upload image if provided
            val imageUrl = if (imageUri != null) {
                val filename = "events/${event.id}/image.jpg"
                val ref = storage.reference.child(filename)
                ref.putFile(imageUri).await()
                ref.downloadUrl.await().toString()
            } else null

            val finalEvent = event.copy(imageUrl = imageUrl)
            firestoreService.addEvent(finalEvent)
        } catch (e: Exception) {
            Log.e("AlbBizMap", "EventsRepo: Error adding event", e)
            Result.failure(e)
        }
    }

    fun getEventsByOrganizer(organizerId: String): Flow<List<Event>> {
        return firestoreService.getEventsByOrganizer(organizerId)
            .catch { e ->
                Log.e("AlbBizMap", "EventsRepo: Error fetching organizer events", e)
                emit(emptyList())
            }
    }

    // Deletes the Firestore doc and, if the event had a photo, its Storage file too.
    // Storage delete failures are logged but don't block the Firestore delete from
    // succeeding — an orphaned image is a much smaller problem than a stuck event
    // the owner can no longer remove.
    suspend fun deleteEvent(event: Event): Result<Unit> {
        return try {
            if (!event.imageUrl.isNullOrBlank()) {
                try {
                    storage.getReferenceFromUrl(event.imageUrl).delete().await()
                } catch (e: Exception) {
                    Log.e("AlbBizMap", "EventsRepo: Error deleting event image", e)
                }
            }
            firestoreService.deleteEvent(event.id)
        } catch (e: Exception) {
            Log.e("AlbBizMap", "EventsRepo: Error deleting event", e)
            Result.failure(e)
        }
    }
}