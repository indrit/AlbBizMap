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
}