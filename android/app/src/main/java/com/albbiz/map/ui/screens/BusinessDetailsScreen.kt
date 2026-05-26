// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.data.Business
import com.albbiz.map.data.JobPosting
import com.albbiz.map.data.Promotion
import com.albbiz.map.data.Review
import com.albbiz.map.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    business: Business,
    currentUserId: String,
    onWriteReviewClick: () -> Unit,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    mapViewModel: MapViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val reviews by reviewViewModel.reviews.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    val favoriteIds by mapViewModel.favoriteIds.collectAsState()
    val isFavorite = favoriteIds.contains(business.id)

    LaunchedEffect(business.id) {
        reviewViewModel.loadReviews(business.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(business.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { mapViewModel.toggleFavorite(business.id) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // ── BADGES SECTION ──────────────────────────────────
                if (business.isVerified || business.isAlbanianOwned || business.isPremium || business.isFeatured || business.isSponsored) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (business.isVerified) DetailBadgeChip("Verified", Color(0xFF2196F3), Icons.Default.Verified)
                        if (business.isAlbanianOwned) DetailBadgeChip("Albanian Owned", Color(0xFFE41E20), Icons.Default.Flag)
                        if (business.isPremium) DetailBadgeChip("Premium", Color(0xFFFFAA00), Icons.Default.Star)
                        if (business.isFeatured) DetailBadgeChip("Featured", Color(0xFF9C27B0), Icons.Default.LocalFireDepartment)
                        if (business.isSponsored) DetailBadgeChip("Sponsored", Color(0xFF4CAF50), Icons.Default.Campaign)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = business.category, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                    Text(text = " ${String.format(Locale.getDefault(), "%.1f", business.rating)} (${business.reviewCount} reviews)", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = business.description, style = MaterialTheme.typography.bodyMedium)
                
                if (business.isPremium && business.longDescription.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = business.longDescription, style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                DetailRowItem(Icons.Default.LocationOn, business.address)

                // ── PREMIUM FIELDS ──────────────────────────────────
                if (business.isPremium) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    if (business.phone.isNotEmpty()) {
                        ClickableDetailRowItem(Icons.Default.Phone, business.phone) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${business.phone}")))
                        }
                    }
                    if (business.email.isNotEmpty()) {
                        ClickableDetailRowItem(Icons.Default.Email, business.email) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${business.email}"))
                            context.startActivity(intent)
                        }
                    }
                    if (business.website.isNotEmpty()) {
                        ClickableDetailRowItem(Icons.Default.Language, business.website) {
                            val url = if (!business.website.startsWith("http")) "https://${business.website}" else business.website
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }

                    if (business.photos.isNotEmpty()) {
                        Text("Photos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(150.dp).padding(vertical = 8.dp)) {
                            items(business.photos) { url ->
                                AsyncImage(
                                    model = url, contentDescription = null,
                                    modifier = Modifier
                                        .width(200.dp)
                                        .fillMaxHeight()
                                        .aspectRatio(16 / 9f)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    if (business.workingHours.isNotEmpty()) {
                        Text("Working Hours", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                        business.workingHours.forEach { (day, hours) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(day, style = MaterialTheme.typography.bodySmall)
                                Text(hours, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } else {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            "Upgrade to Premium to unlock contact info, website, and photos!",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // ── PROMOTIONS SECTION ───────────────────────────────
                if (business.promotions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Promotions & Deals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    business.promotions.forEach { promotion ->
                        DetailPromotionItem(promotion)
                    }
                }

                // ── JOB BOARD SECTION ────────────────────────────────
                if (business.jobs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Job Postings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    business.jobs.forEach { job ->
                        DetailJobItem(job)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentUserId == business.ownerId) {
                        Button(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("Edit")
                        }
                    }
                    Button(onClick = onWriteReviewClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.RateReview, null); Spacer(Modifier.width(8.dp)); Text("Review")
                    }
                }
            }

            item { Text("Recent Reviews", style = MaterialTheme.typography.titleLarge) }

            if (isLoading) {
                item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else if (reviews.isEmpty()) {
                item { Text("No reviews yet. Be the first!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(reviews) { review -> DetailReviewItem(review) }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun DetailPromotionItem(promotion: Promotion) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        border = BorderStroke(1.dp, Color(0xFFFBC02D))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(promotion.title, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
            Text(promotion.description, style = MaterialTheme.typography.bodySmall)
            if (promotion.discountCode != null) {
                Surface(
                    color = Color(0xFFFBC02D),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        "Code: ${promotion.discountCode}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailJobItem(job: JobPosting) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(job.title, fontWeight = FontWeight.Bold)
                DetailBadgeChip("Job: " + job.type, Color(0xFF673AB7), Icons.Default.Work)
            }
            Text(job.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (job.salary != null) {
                Text("Salary: ${job.salary}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun DetailRowItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ClickableDetailRowItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailReviewItem(review: Review) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.userName, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                repeat(review.rating) { Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp)) }
            }
            Text(review.comment, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
            Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(review.createdAt)), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DetailBadgeChip(label: String, color: Color, icon: ImageVector) {
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
