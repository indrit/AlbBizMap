// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class BusinessCategory(
    val displayName: String,
    val icon: ImageVector
) {
    RESTAURANT("Restaurant", Icons.Default.Restaurant),
    CAFE("Cafe", Icons.Default.Coffee),
    MARKET("Market", Icons.Default.ShoppingCart),
    CONTRACTOR("Contractor", Icons.Default.Construction),
    LAWYER("Lawyer", Icons.Default.Gavel),
    DENTIST("Dentist", Icons.Default.MedicalServices),
    BARBER("Barber", Icons.Default.ContentCut),
    BEAUTY_SALON("Beauty Salon", Icons.Default.Face),
    AUTO_SHOP("Auto Shop", Icons.Default.DirectionsCar),
    OTHER("Other", Icons.Default.Business);

    companion object {
        // Android's AddBusinessScreen writes the raw enum name to Firestore
        // ("RESTAURANT", "BEAUTY_SALON"). The iOS app's BusinessCategory uses
        // its display string as the rawValue instead ("Restaurant",
        // "Beauty Salon") — same category, different stored format. Since
        // both apps write to the same Firestore project, a business added on
        // one platform has to still resolve back to a category when opened
        // on the other, or its category silently gets wiped the moment that
        // edit form is saved. Tries exact enum-name match first (the common
        // case), then case-insensitive name, then case-insensitive display
        // name — the last one is what actually recovers the iOS format,
        // spaces and all.
        fun fromStored(value: String): BusinessCategory? {
            if (value.isBlank()) return null
            return entries.find { it.name == value }
                ?: entries.find { it.name.equals(value, ignoreCase = true) }
                ?: entries.find { it.displayName.equals(value, ignoreCase = true) }
        }
    }
}
