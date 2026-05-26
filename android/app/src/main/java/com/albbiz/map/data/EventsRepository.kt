// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class EventsRepository {
    private val firestoreService = FirestoreService()

    fun getEvents(): Flow<List<Event>> {
        return firestoreService.getEvents()
            .catch { e ->
                android.util.Log.e("AlbBizMap", "EventsRepo: Error in flow", e)
                emit(emptyList())
            }
    }
}
