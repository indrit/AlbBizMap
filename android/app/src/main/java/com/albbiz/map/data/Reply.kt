package com.albbiz.map.data

data class Reply(
    val id: String = "",
    val reviewId: String = "",
    val userId: String = "",
    val userName: String = "",
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likedBy: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "reviewId" to reviewId,
            "userId" to userId,
            "userName" to userName,
            "comment" to comment,
            "createdAt" to createdAt,
            "likedBy" to likedBy
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Reply {
            return Reply(
                id = id,
                reviewId = map["reviewId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                comment = map["comment"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                likedBy = (map["likedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}