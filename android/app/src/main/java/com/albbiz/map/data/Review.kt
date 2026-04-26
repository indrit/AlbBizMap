// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

data class Review(
    val id: String = "",
    val businessId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val photos: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val ownerReply: String? = null,
    val ownerReplyAt: Long? = null,
    val reportCount: Int = 0,            // ← NEW: tracks how many reports
    val reportedBy: List<String> = emptyList() // ← NEW: tracks who reported
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "businessId" to businessId,
            "userId" to userId,
            "userName" to userName,
            "rating" to rating,
            "comment" to comment,
            "photos" to photos,
            "createdAt" to createdAt,
            "ownerReply" to ownerReply,
            "ownerReplyAt" to ownerReplyAt,
            "reportCount" to reportCount,
            "reportedBy" to reportedBy
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Review {
            return Review(
                id = id,
                businessId = map["businessId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                rating = (map["rating"] as? Number)?.toInt() ?: 0,
                comment = map["comment"] as? String ?: "",
                photos = (map["photos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                ownerReply = map["ownerReply"] as? String,
                ownerReplyAt = (map["ownerReplyAt"] as? Number)?.toLong(),
                reportCount = (map["reportCount"] as? Number)?.toInt() ?: 0,
                reportedBy = (map["reportedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}