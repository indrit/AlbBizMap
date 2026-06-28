// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import com.google.firebase.firestore.GeoPoint

data class Business(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "", // Short description (free - 100 chars)
    val longDescription: String = "", // Extended description (premium)
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val isOpen24Hours: Boolean = false,
    val workingHours: Map<String, String> = emptyMap(),
    val location: GeoPoint? = null,
    val photos: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val isActive: Boolean = true,
    val isSponsored: Boolean = false,
    val sponsoredUntil: Long? = null,
    val isPremium: Boolean = false,
    val premiumUntil: Long? = null,
    val ownerId: String = "",
    val isVerified: Boolean = false,
    val isAlbanianOwned: Boolean = false,
    val isFeatured: Boolean = false,
    val promotions: List<Promotion> = emptyList(),
    val jobs: List<JobPosting> = emptyList(),
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "category" to category,
            "description" to description,
            "longDescription" to longDescription,
            "address" to address,
            "phone" to phone,
            "email" to email,
            "website" to website,
            "isOpen24Hours" to isOpen24Hours,
            "workingHours" to workingHours,
            "location" to location,
            "photos" to photos,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "isActive" to isActive,
            "isSponsored" to isSponsored,
            "sponsoredUntil" to sponsoredUntil,
            "isPremium" to isPremium,
            "premiumUntil" to premiumUntil,
            "ownerId" to ownerId,
            "isVerified" to isVerified,
            "isAlbanianOwned" to isAlbanianOwned,
            "isFeatured" to isFeatured,
            "promotions" to promotions.map { it.toMap() },
            "jobs" to jobs.map { it.toMap() },
            "jobs" to jobs.map { it.toMap() },
            "likeCount" to likeCount,
            "likedBy" to likedBy
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Business {
            return Business(
                id = id,
                name = map["name"] as? String ?: "",
                category = map["category"] as? String ?: "",
                description = map["description"] as? String ?: "",
                longDescription = map["longDescription"] as? String ?: "",
                address = map["address"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                email = map["email"] as? String ?: "",
                website = map["website"] as? String ?: "",
                isOpen24Hours = map["isOpen24Hours"] as? Boolean ?: false,
                likeCount = (map["likeCount"] as? Number)?.toInt() ?: 0,
                likedBy = (map["likedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                workingHours = (map["workingHours"] as? Map<*, *>)
                    ?.entries
                    ?.associate { it.key.toString() to it.value.toString() }
                    ?: emptyMap(),
                location = map["location"] as? GeoPoint,
                photos = (map["photos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                reviewCount = (map["reviewCount"] as? Number)?.toInt() ?: 0,
                isActive = map["isActive"] as? Boolean ?: true,
                isSponsored = map["isSponsored"] as? Boolean ?: false,
                sponsoredUntil = (map["sponsoredUntil"] as? Number)?.toLong(),
                isPremium = map["isPremium"] as? Boolean ?: false,
                premiumUntil = (map["premiumUntil"] as? Number)?.toLong(),
                ownerId = map["ownerId"] as? String ?: "",
                isVerified = map["isVerified"] as? Boolean ?: false,
                isAlbanianOwned = map["isAlbanianOwned"] as? Boolean ?: false,
                isFeatured = map["isFeatured"] as? Boolean ?: false,
                promotions = (map["promotions"] as? List<*>)?.mapNotNull {
                    (it as? Map<*, *>)?.let { pMap ->
                        val stringKeyedMap = pMap.entries.associate { entry -> entry.key.toString() to entry.value }
                        Promotion.fromMap(stringKeyedMap)
                    }
                } ?: emptyList(),
                jobs = (map["jobs"] as? List<*>)?.mapNotNull {
                    (it as? Map<*, *>)?.let { jMap ->
                        val stringKeyedMap = jMap.entries.associate { entry -> entry.key.toString() to entry.value }
                        JobPosting.fromMap(stringKeyedMap)
                    }
                } ?: emptyList()
            )
        }
    }
}

data class Promotion(
    val title: String = "",
    val description: String = "",
    val discountCode: String? = null,
    val expiryDate: Long? = null
) {
    fun toMap() = mapOf(
        "title" to title,
        "description" to description,
        "discountCode" to discountCode,
        "expiryDate" to expiryDate
    )
    companion object {
        fun fromMap(map: Map<String, Any?>) = Promotion(
            title = map["title"] as? String ?: "",
            description = map["description"] as? String ?: "",
            discountCode = map["discountCode"] as? String,
            expiryDate = (map["expiryDate"] as? Number)?.toLong()
        )
    }
}

data class JobPosting(
    val title: String = "",
    val description: String = "",
    val type: String = "Full-time", // Full-time, Part-time, Contract
    val salary: String? = null,
    val postedAt: Long = System.currentTimeMillis()
) {
    fun toMap() = mapOf(
        "title" to title,
        "description" to description,
        "type" to type,
        "salary" to salary,
        "postedAt" to postedAt
    )
    companion object {
        fun fromMap(map: Map<String, Any?>) = JobPosting(
            title = map["title"] as? String ?: "",
            description = map["description"] as? String ?: "",
            type = map["type"] as? String ?: "Full-time",
            salary = map["salary"] as? String,
            postedAt = (map["postedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
