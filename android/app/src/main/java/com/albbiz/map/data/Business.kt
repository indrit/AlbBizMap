// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.GeoPoint

data class Business(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val location: GeoPoint? = null,
    val ownerId: String = "",
    val isSponsored: Boolean = false,
    val sponsoredUntil: Long? = null,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val photos: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val isOpen24Hours: Boolean = false,
    val workingHours: Map<String, String> = emptyMap()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "category" to category,
            "address" to address,
            "phone" to phone,
            "email" to email,
            "website" to website,
            "location" to location,
            "ownerId" to ownerId,
            "isSponsored" to isSponsored,
            "sponsoredUntil" to sponsoredUntil,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "photos" to photos,
            "createdAt" to createdAt,
            "isActive" to isActive,
            "isOpen24Hours" to isOpen24Hours,
            "workingHours" to workingHours
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Business {
            return Business(
                id = id,
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = map["category"] as? String ?: "",
                address = map["address"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                email = map["email"] as? String ?: "",
                website = map["website"] as? String ?: "",
                location = map["location"] as? GeoPoint,
                ownerId = map["ownerId"] as? String ?: "",
                isSponsored = map["isSponsored"] as? Boolean ?: false,
                sponsoredUntil = map["sponsoredUntil"] as? Long,
                rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                reviewCount = (map["reviewCount"] as? Number)?.toInt() ?: 0,
                photos = (map["photos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isActive = map["isActive"] as? Boolean ?: true,
                isOpen24Hours = map["isOpen24Hours"] as? Boolean ?: false,
                workingHours = (map["workingHours"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value.toString() } ?: emptyMap()
            )
        }
    }
}

enum class BusinessCategory(val displayName: String, val icon: ImageVector) {
    RESTAURANT("Restaurant", Icons.Default.Restaurant),
    CAFE("Cafe", Icons.Default.Coffee),
    SHOP("Shop", Icons.Default.ShoppingCart),
    HOTEL("Hotel", Icons.Default.Hotel),
    PHARMACY("Pharmacy", Icons.Default.LocalPharmacy),
    GAS_STATION("Gas Station", Icons.Default.LocalGasStation),
    BANK("Bank", Icons.Default.AccountBalance),
    HOSPITAL("Hospital", Icons.Default.LocalHospital),
    OTHER("Other", Icons.Default.Business);

    companion object {
        fun fromString(value: String): BusinessCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}