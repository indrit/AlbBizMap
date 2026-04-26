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
    OTHER("Other", Icons.Default.Business)
}
