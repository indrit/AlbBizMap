// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage { EN, SQ }

data class AppStrings(
    val appName: String,
    val welcomeTitle: String,
    val welcomeDesc: String,
    val getStarted: String,
    val searchPlaceholder: String,
    val addBusiness: String,
    val listView: String,
    val favorites: String,
    val profile: String,
    val nearMe: String,
    val featured: String,
    val recentlyAdded: String,
    val topRated: String,
    val noResults: String,
    val getDirections: String,
    val upgradePremium: String,
    val editBusiness: String,
    val writeReview: String,
    val recentReviews: String,
    val category: String,
    val promotions: String,
    val jobs: String
)

val EnglishStrings = AppStrings(
    appName = "AlbBizMap",
    welcomeTitle = "Welcome to Albanian Business App",
    welcomeDesc = "Register your business, or explore the Albanian business map in your area and beyond.",
    getStarted = "Get Started",
    searchPlaceholder = "Search businesses...",
    addBusiness = "Add My Business",
    listView = "List View",
    favorites = "My Favorites",
    profile = "Profile",
    nearMe = "Near Me",
    featured = "Featured Businesses",
    recentlyAdded = "Recently Added",
    topRated = "Top Rated",
    noResults = "No businesses found",
    getDirections = "Get Directions",
    upgradePremium = "Upgrade to Premium to see full contact details!",
    editBusiness = "Edit Business",
    writeReview = "Write a Review",
    recentReviews = "Recent Reviews",
    category = "Category",
    promotions = "Promotions & Deals",
    jobs = "Job Postings"
)

val AlbanianStrings = AppStrings(
    appName = "AlbBizMap",
    welcomeTitle = "Mirësevini në AlbBizMap",
    welcomeDesc = "Regjistroni biznesin tuaj, ose eksploroni hartën e bizneseve shqiptare në zonën tuaj dhe më gjerë.",
    getStarted = "Fillo Tani",
    searchPlaceholder = "Kërko biznese...",
    addBusiness = "Shto Biznesin Tim",
    listView = "Lista",
    favorites = "Të Preferuarat",
    profile = "Profili",
    nearMe = "Pranë Meje",
    featured = "Bizneset e Rëndësishme",
    recentlyAdded = "Shtuar Së Fundmi",
    topRated = "Më të Vlerësuarit",
    noResults = "Nuk u gjet asnjë biznes",
    getDirections = "Drejtimet",
    upgradePremium = "Kaloni në Premium për të parë detajet e kontaktit!",
    editBusiness = "Ndrysho Biznesin",
    writeReview = "Shkruaj një Vlerësim",
    recentReviews = "Vlerësimet e Fundit",
    category = "Kategoria",
    promotions = "Promocione dhe Oferta",
    jobs = "Oferta Pune"
)

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }

@Composable
fun ProvideAppStrings(language: AppLanguage, content: @Composable () -> Unit) {
    val strings = if (language == AppLanguage.SQ) AlbanianStrings else EnglishStrings
    CompositionLocalProvider(LocalAppStrings provides strings) {
        content()
    }
}
