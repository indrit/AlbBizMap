// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.R
import com.albbiz.map.data.Business
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.ui.theme.TierBronze
import com.albbiz.map.ui.theme.TierGold
import com.albbiz.map.ui.theme.TierSilver
import com.albbiz.map.viewmodel.BillingViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    business: Business,
    onBackClick: () -> Unit,
    billingViewModel: BillingViewModel = viewModel()
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: ""

    val currentTier = when {
        business.isSponsored -> "Sponsored"
        business.isFeatured -> "Featured"
        business.isPremium -> "Premium"
        else -> "Free"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        String.format(strings.upgradeTitleTemplate, business.name),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.back, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeTontRed)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MeTontRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        strings.choosePlan,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // FREE PLAN
            PlanCard(
                title = strings.freeTierName,
                price = "$0",
                period = strings.forever,
                accentColor = MeTontGrey,
                isCurrentPlan = currentTier == "Free",
                features = listOf(
                    PlanFeature(strings.planFeatureNameCategory, true),
                    PlanFeature(strings.planFeatureLocationOnMap, true),
                    PlanFeature(strings.planFeature100CharDesc, true),
                    PlanFeature(strings.planFeature1Photo, true)
                ),
                buttonText = if (currentTier == "Free") strings.currentPlanButton else strings.notAvailableDash,
                onButtonClick = {}
            )

            // PREMIUM PLAN
            PlanCard(
                title = strings.premium,
                price = "$2.99",
                period = strings.perMonth,
                accentColor = TierBronze,
                isCurrentPlan = currentTier == "Premium",
                features = listOf(
                    PlanFeature(strings.planFeatureEverythingPremium, true),
                    PlanFeature(strings.planFeatureUp6Photos, true),
                    PlanFeature(strings.planFeaturePhoneNumber, true),
                    PlanFeature(strings.planFeatureEmailWebsite, true),
                    PlanFeature(strings.planFeaturePremiumBadge, true)
                ),
                buttonText = if (currentTier == "Premium") strings.currentPlanButton else strings.requestUpgrade,
                onButtonClick = {
                    billingViewModel.launchBillingFlow(context as Activity, "premium_subscription", business.id)
                }
            )

            // FEATURED PLAN
            PlanCard(
                title = strings.featured2,
                price = "$9.99",
                period = strings.perMonth,
                accentColor = TierSilver,
                isCurrentPlan = currentTier == "Featured",
                features = listOf(
                    PlanFeature(strings.planFeatureEverythingPremium, true),
                    PlanFeature(strings.planFeatureUp10Photos, true),
                    PlanFeature(strings.planFeatureFeaturedBadge, true),
                    PlanFeature(strings.planFeatureFeaturedDiscoveryRow, true)
                ),
                buttonText = if (currentTier == "Featured") strings.currentPlanButton else strings.requestFeatured,
                onButtonClick = {
                    billingViewModel.launchBillingFlow(context as Activity, "featured_subscription", business.id)
                }
            )

            // SPONSORED PLAN
            PlanCard(
                title = strings.sponsored,
                price = "$19.99",
                period = strings.perMonth,
                accentColor = TierGold,
                isCurrentPlan = currentTier == "Sponsored",
                features = listOf(
                    PlanFeature(strings.planFeatureEverythingPremium, true),
                    PlanFeature(strings.planFeatureUp14Photos, true),
                    PlanFeature(strings.planFeatureHighlightedMapPin, true),
                    PlanFeature(strings.planFeatureTopSearchResults, true),
                    PlanFeature(strings.planFeatureSponsoredBadge, true)
                ),
                buttonText = if (currentTier == "Sponsored") strings.currentPlanButton else strings.requestSponsorship,
                onButtonClick = {
                    billingViewModel.launchBillingFlow(context as Activity, "sponsored_subscription", business.id)
                }
            )
        }
    }
}

data class PlanFeature(val text: String, val included: Boolean)

@Composable
private fun PlanCard(
    title: String,
    price: String,
    period: String,
    accentColor: Color,
    isCurrentPlan: Boolean,
    features: List<PlanFeature>,
    buttonText: String,
    onButtonClick: () -> Unit,
    badgeIcon: Int? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (badgeIcon != null) {
                        Image(
                            painter = painterResource(id = badgeIcon),
                            contentDescription = "$title badge",
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor
                        ) {}
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                if (isCurrentPlan) {
                    Surface(
                        color = MeTontGrey.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "CURRENT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MeTontGrey,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    price,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    period,
                    style = MaterialTheme.typography.bodySmall,
                    color = MeTontGrey,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            features.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (feature.included) Icons.Default.CheckCircle
                        else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (feature.included) Color(0xFF4CAF50)
                        else Color(0xFFE0E0E0),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        feature.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (feature.included) Color.Black else MeTontGrey
                    )
                }
            }

            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                enabled = !isCurrentPlan,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFEEEEEE),
                    disabledContentColor = MeTontGrey
                )
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
