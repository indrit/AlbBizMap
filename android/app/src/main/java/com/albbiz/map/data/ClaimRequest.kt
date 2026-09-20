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
    // "claim": user doesn't yet own this listing, wants ownerId reassigned to them.
    // "verification": user already owns this listing, just wants isVerified set —
    // approving still calls the same ownerId-reassign code, but it's a no-op since
    // userId already equals the business's current ownerId.
    val type: String = "claim",
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
            "type" to type,
            "createdAt" to createdAt
        )
    }
}