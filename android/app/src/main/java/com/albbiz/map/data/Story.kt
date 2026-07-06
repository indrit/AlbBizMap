// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

data class Story(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val businessId: String? = null,
    val businessName: String? = null,
    val type: String = "user", // business, community, sponsored, user
    val category: String = "",
    val location: String = "",
    val photos: List<String> = emptyList(),
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000, // 24hrs
    val viewedBy: List<String> = emptyList(),
    val isSponsored: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to userName,
            "businessId" to businessId,
            "businessName" to businessName,
            "type" to type,
            "category" to category,
            "location" to location,
            "photos" to photos,
            "text" to text,
            "createdAt" to createdAt,
            "expiresAt" to expiresAt,
            "viewedBy" to viewedBy,
            "isSponsored" to isSponsored
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Story {
            return Story(
                id = id,
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                businessId = map["businessId"] as? String,
                businessName = map["businessName"] as? String,
                type = map["type"] as? String ?: "user",
                category = map["category"] as? String ?: "",
                location = map["location"] as? String ?: "",
                photos = (map["photos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                text = map["text"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                expiresAt = (map["expiresAt"] as? Number)?.toLong() ?: System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                viewedBy = (map["viewedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                isSponsored = map["isSponsored"] as? Boolean ?: false
            )
        }
    }
}