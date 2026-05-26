// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val date: Long = 0,
    val category: String = "Cultural", // Cultural, Concert, Festival, Community
    val imageUrl: String? = null,
    val organizerId: String = "",
    val isPromoted: Boolean = false,
    val websiteUrl: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "locationName" to locationName,
            "date" to date,
            "category" to category,
            "imageUrl" to imageUrl,
            "organizerId" to organizerId,
            "isPromoted" to isPromoted,
            "websiteUrl" to websiteUrl
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Event {
            return Event(
                id = id,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                locationName = map["locationName"] as? String ?: "",
                date = (map["date"] as? Number)?.toLong() ?: 0L,
                category = map["category"] as? String ?: "Cultural",
                imageUrl = map["imageUrl"] as? String,
                organizerId = map["organizerId"] as? String ?: "",
                isPromoted = map["isPromoted"] as? Boolean ?: false,
                websiteUrl = map["websiteUrl"] as? String
            )
        }
    }
}
