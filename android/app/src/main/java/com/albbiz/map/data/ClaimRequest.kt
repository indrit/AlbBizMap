// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

data class ClaimRequest(
    val id: String = "",
    val businessId: String = "",
    val businessName: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val reason: String = "",
    val status: String = "pending", // pending, approved, rejected
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "businessId" to businessId,
            "businessName" to businessName,
            "userId" to userId,
            "userName" to userName,
            "userEmail" to userEmail,
            "reason" to reason,
            "status" to status,
            "createdAt" to createdAt
        )
    }
}