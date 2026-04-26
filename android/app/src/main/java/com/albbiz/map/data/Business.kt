// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import com.google.firebase.firestore.GeoPoint

data class Business(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
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
    val isPremium: Boolean = false,
    val ownerId: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "category" to category,
            "description" to description,
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
            "isPremium" to isPremium,
            "ownerId" to ownerId
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Business {
            return Business(
                id = id,
                name = map["name"] as? String ?: "",
                category = map["category"] as? String ?: "",
                description = map["description"] as? String ?: "",
                address = map["address"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                email = map["email"] as? String ?: "",
                website = map["website"] as? String ?: "",
                isOpen24Hours = map["isOpen24Hours"] as? Boolean ?: false,
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
                isPremium = map["isPremium"] as? Boolean ?: false,
                ownerId = map["ownerId"] as? String ?: ""
            )
        }
    }
}