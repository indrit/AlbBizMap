// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade Your Listing") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose Your Plan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Help your business stand out in the Albanian community",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── FREE PLAN ─────────────────────────────────────────
            PlanCard(
                title = "Free",
                price = "$0",
                period = "forever",
                color = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = MaterialTheme.colorScheme.outline,
                isCurrentPlan = true,
                features = listOf(
                    PlanFeature("Business name & category", true),
                    PlanFeature("Location on map", true),
                    PlanFeature("100 character description", true),
                    PlanFeature("1 photo", true),
                    PlanFeature("Phone number", false),
                    PlanFeature("Email & website", false),
                    PlanFeature("Extended description", false),
                    PlanFeature("Multiple photos", false),
                    PlanFeature("Hours of operation", false),
                    PlanFeature("Premium badge", false)
                ),
                buttonText = "Current Plan",
                onButtonClick = {}
            )

            // ── PREMIUM PLAN ──────────────────────────────────────
            PlanCard(
                title = "Premium",
                price = "$2.99",
                period = "per month",
                color = Color(0xFFFFF8E1),
                borderColor = Color(0xFFFFAA00),
                isCurrentPlan = false,
                features = listOf(
                    PlanFeature("Business name & category", true),
                    PlanFeature("Location on map", true),
                    PlanFeature("100 character description", true),
                    PlanFeature("1 photo", true),
                    PlanFeature("Phone number", true),
                    PlanFeature("Email & website", true),
                    PlanFeature("Extended description", true),
                    PlanFeature("Multiple photos", true),
                    PlanFeature("Hours of operation", true),
                    PlanFeature("Premium badge", true)
                ),
                buttonText = "Request Upgrade",
                onButtonClick = {
                    // Send email request to admin
                    val subject = "Premium Upgrade Request - AlbBizMap"
                    val body = "Hello,\n\nI would like to upgrade my business listing to Premium ($2.99/month).\n\nAccount email: $userEmail\n\nPlease contact me to complete the upgrade.\n\nThank you!"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:admin@albbizmap.com")
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send upgrade request"))
                }
            )

            // ── SPONSORED PLAN ────────────────────────────────────
            PlanCard(
                title = "Sponsored",
                price = "$19.99",
                period = "per month",
                color = Color(0xFFE8F5E9),
                borderColor = Color(0xFF4CAF50),
                isCurrentPlan = false,
                features = listOf(
                    PlanFeature("Everything in Premium", true),
                    PlanFeature("Highlighted map pin", true),
                    PlanFeature("Top of search results", true),
                    PlanFeature("Sponsored badge", true),
                    PlanFeature("Featured in discovery section", true),
                    PlanFeature("Priority customer support", true)
                ),
                buttonText = "Request Sponsorship",
                onButtonClick = {
                    val subject = "Sponsored Listing Request - AlbBizMap"
                    val body = "Hello,\n\nI would like to upgrade my business listing to Sponsored ($19.99/month).\n\nAccount email: $userEmail\n\nPlease contact me to complete the upgrade.\n\nThank you!"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:admin@albbizmap.com")
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send sponsorship request"))
                }
            )

            // ── FEATURED PLAN ─────────────────────────────────────
            PlanCard(
                title = "Featured",
                price = "$9.99",
                period = "per month",
                color = Color(0xFFF3E5F5),
                borderColor = Color(0xFF9C27B0),
                isCurrentPlan = false,
                features = listOf(
                    PlanFeature("Everything in Premium", true),
                    PlanFeature("Featured badge", true),
                    PlanFeature("Featured in discovery row", true),
                    PlanFeature("Highlighted in list view", true)
                ),
                buttonText = "Request Featured",
                onButtonClick = {
                    val subject = "Featured Listing Request - AlbBizMap"
                    val body = "Hello,\n\nI would like to upgrade my business listing to Featured ($9.99/month).\n\nAccount email: $userEmail\n\nPlease contact me to complete the upgrade.\n\nThank you!"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:admin@albbizmap.com")
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send featured request"))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── NOTE ──────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Payments are currently processed manually. We will contact you within 24 hours of your request.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class PlanFeature(val text: String, val included: Boolean)

@Composable
private fun PlanCard(
    title: String,
    price: String,
    period: String,
    color: Color,
    borderColor: Color,
    isCurrentPlan: Boolean,
    features: List<PlanFeature>,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Plan header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isCurrentPlan) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "CURRENT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Price
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            HorizontalDivider()

            // Features list
            features.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (feature.included) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (feature.included) Color(0xFF4CAF50) else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = feature.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (feature.included) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Button
            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCurrentPlan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = borderColor
                )
            ) {
                Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}