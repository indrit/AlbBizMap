// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.data.Business
import com.albbiz.map.data.JobPosting
import com.albbiz.map.data.Promotion
import com.albbiz.map.data.Review
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import com.albbiz.map.ui.theme.TierBronze
import com.albbiz.map.ui.theme.TierSilver
import com.albbiz.map.ui.theme.TierGold
import com.albbiz.map.utils.AuthGate
import com.albbiz.map.data.Reply
import androidx.compose.foundation.layout.PaddingValues
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    business: Business,
    currentUserId: String,
    onWriteReviewClick: () -> Unit,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onNavigateToAuth: () -> Unit,
    mapViewModel: MapViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val reviews by reviewViewModel.reviews.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    val favoriteIds by mapViewModel.favoriteIds.collectAsState()
    val isFavorite = favoriteIds.contains(business.id)
    val currentBusiness = business

    LaunchedEffect(business.id) {
        reviewViewModel.loadReviews(business.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        business.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        AuthGate.requireLogin(
                            onNotLoggedIn = onNavigateToAuth,
                            action = {
                                val shareText = "Check out ${business.name} on MeTont!\n${business.category} • ${business.address}"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            }
                        )
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        AuthGate.requireLogin(
                            onNotLoggedIn = onNavigateToAuth,
                            action = { mapViewModel.toggleFavorite(business.id) }
                        )
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeTontRed)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                //.background(Color(0xFFF5F5F5))
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // ── MAIN INFO CARD ────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Badges
                        if (business.isVerified || business.isAlbanianOwned ||
                            business.isPremium || business.isFeatured || business.isSponsored
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (business.isVerified) DetailBadgeChip(strings.verified, Color(0xFF2196F3), Icons.Default.Verified)
                                if (business.isAlbanianOwned) DetailBadgeChip(strings.albanianOwned, MeTontRed, Icons.Default.Flag)
                                if (business.isPremium) DetailBadgeChip(strings.premium, TierBronze, Icons.Default.Star)
                                if (business.isFeatured) DetailBadgeChip(strings.featured2, TierSilver, Icons.Default.LocalFireDepartment)
                                if (business.isSponsored) DetailBadgeChip(strings.sponsored, TierGold, Icons.Default.Campaign)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Category
                        Text(
                            business.category,
                            color = MeTontRed,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                " ${String.format(Locale.getDefault(), "%.1f", business.rating)} (${business.reviewCount} reviews)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MeTontGrey
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        Text(
                            business.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )

                        if (business.isPremium && business.longDescription.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                business.longDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Address
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = MeTontRed
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                business.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        }
                    }
                }
            }

            // ── CONTACT CARD (Premium) ────────────────────────────
            item {
                if (business.isPremium) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Contact",
                                fontWeight = FontWeight.Bold,
                                color = MeTontRed,
                                fontSize = 14.sp
                            )

                            if (business.phone.isNotEmpty()) {
                                ClickableDetailRowItem(Icons.Default.Phone, business.phone) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${business.phone}"))
                                    )
                                }
                            }
                            if (business.email.isNotEmpty()) {
                                ClickableDetailRowItem(Icons.Default.Email, business.email) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${business.email}"))
                                    )
                                }
                            }
                            if (business.website.isNotEmpty()) {
                                ClickableDetailRowItem(Icons.Default.Language, business.website) {
                                    val url = if (!business.website.startsWith("http"))
                                        "https://${business.website}" else business.website
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }

                            // Photos
                            if (business.photos.isNotEmpty()) {
                                Text(
                                    strings.photos,
                                    fontWeight = FontWeight.Bold,
                                    color = MeTontRed,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.height(150.dp).padding(vertical = 8.dp)
                                ) {
                                    items(business.photos) { url ->
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(200.dp)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            // Working Hours
                            if (business.workingHours.isNotEmpty()) {
                                Text(
                                    strings.workingHours,
                                    fontWeight = FontWeight.Bold,
                                    color = MeTontRed,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                business.workingHours.forEach { (day, hours) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(day, style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                                        Text(hours, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Upgrade card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        border = BorderStroke(1.dp, Color(0xFFFFAA00))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = Color(0xFFFFAA00),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    strings.upgradePremiumTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8F00)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                strings.upgradePremium,
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onUpgradeClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFAA00),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(strings.viewPlans, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── PROMOTIONS CARD ───────────────────────────────────
            if (business.promotions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                strings.promotions,
                                fontWeight = FontWeight.Bold,
                                color = MeTontRed,
                                fontSize = 14.sp
                            )
                            business.promotions.forEach { promotion ->
                                DetailPromotionItem(promotion)
                            }
                        }
                    }
                }
            }

            // ── JOBS CARD ─────────────────────────────────────────
            if (business.jobs.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                strings.jobs,
                                fontWeight = FontWeight.Bold,
                                color = MeTontRed,
                                fontSize = 14.sp
                            )
                            business.jobs.forEach { job ->
                                DetailJobItem(job)
                            }
                        }
                    }
                }
            }

            // ── ACTION BUTTONS ────────────────────────────────────
            item {
                val currentUserId2 = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val isLiked = currentUserId2 != null && currentUserId2 in business.likedBy

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ── LIKE BUTTON ───────────────────────────────────
                    OutlinedButton(
                        onClick = {
                            com.albbiz.map.utils.AuthGate.requireLogin(
                                onNotLoggedIn = onNavigateToAuth,
                                action = { mapViewModel.toggleFavorite(business.id) }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isLiked) MeTontRed else MeTontGrey
                        ),
                        border = BorderStroke(1.dp, if (isLiked) MeTontRed else MeTontGrey)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                            contentDescription = "Like",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${business.likeCount}", fontWeight = FontWeight.Bold)
                    }

                    if (currentUserId == business.ownerId) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                            border = BorderStroke(1.dp, MeTontRed)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.editBusiness, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = onWriteReviewClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeTontRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.RateReview, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.writeReview, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // ── REVIEWS SECTION ───────────────────────────────────
            item {
                Text(
                    strings.recentReviews,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MeTontRed)
                    }
                }
            } else if (reviews.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                strings.noReviewsYet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MeTontGrey
                            )
                        }
                    }
                }
            } else {
                items(reviews) { review ->
                    DetailReviewItem(
                        review = review,
                        businessId = business.id,
                        onNavigateToAuth = onNavigateToAuth
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun DetailPromotionItem(promotion: Promotion) {
    Surface(
        color = Color(0xFFFFF9C4),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFFBC02D))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                promotion.title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )
            Text(
                promotion.description,
                style = MaterialTheme.typography.bodySmall,
                color = MeTontGrey
            )
            if (promotion.discountCode != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFBC02D),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "Code: ${promotion.discountCode}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DetailJobItem(job: JobPosting) {
    Surface(
        color = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    job.title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = Color(0xFF673AB7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        job.type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                job.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MeTontGrey
            )
            if (job.salary != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "💰 ${job.salary}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7)
                )
            }
        }
    }
}

@Composable
fun DetailRowItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MeTontRed)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
    }
}

@Composable
fun ClickableDetailRowItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MeTontRed)
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = MeTontRed,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DetailReviewItem(
    review: Review,
    businessId: String,
    onNavigateToAuth: () -> Unit,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val currentUserId = firebaseUser?.uid
    val currentUserName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.email?.substringBefore("@") ?: ""
    val isLiked = currentUserId != null && currentUserId in review.likedBy
    val repliesMap by reviewViewModel.replies.collectAsState()
    val replies = repliesMap[review.id] ?: emptyList()

    var showReplies by remember { mutableStateOf(false) }
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(showReplies, replies.size) {
        if (showReplies) {
            reviewViewModel.loadReplies(businessId, review.id)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // ── REVIEWER HEADER ───────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MeTontRed.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            review.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            color = MeTontRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.userName, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(review.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MeTontGrey
                    )
                }
                Row {
                    repeat(review.rating) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── REVIEW COMMENT ────────────────────────────────────
            Text(
                review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── ACTION ROW (like, reply) ───────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Like button
                IconButton(
                    onClick = {
                        AuthGate.requireLogin(
                            onNotLoggedIn = onNavigateToAuth,
                            action = {
                                currentUserId?.let { uid ->
                                    reviewViewModel.toggleLike(businessId, review.id, uid)
                                }
                            }
                        )
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Like",
                        tint = if (isLiked) MeTontRed else MeTontGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    "${review.likedBy.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MeTontGrey
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Reply button
                TextButton(
                    onClick = {
                        AuthGate.requireLogin(
                            onNotLoggedIn = onNavigateToAuth,
                            action = { showReplyInput = !showReplyInput }
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        Icons.Default.Reply,
                        null,
                        tint = MeTontGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Reply",
                        style = MaterialTheme.typography.labelSmall,
                        color = MeTontGrey
                    )
                }

                // View replies toggle
                if (replies.isNotEmpty() || showReplies) {
                    TextButton(
                        onClick = { showReplies = !showReplies },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            if (showReplies) "Hide replies"
                            else "View ${replies.size} ${if (replies.size == 1) "reply" else "replies"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeTontRed
                        )
                    }
                }
            }

            // ── REPLY INPUT ───────────────────────────────────────
            if (showReplyInput) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Write a reply...", color = MeTontGrey, fontSize = 12.sp)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank() && currentUserId != null) {
                                reviewViewModel.addReply(
                                    businessId = businessId,
                                    reviewId = review.id,
                                    comment = replyText.trim(),
                                    userId = currentUserId,
                                    userName = currentUserName,
                                    onSuccess = {
                                        replyText = ""
                                        showReplyInput = false
                                        showReplies = true
                                        reviewViewModel.loadReplies(businessId, review.id)
                                    }
                                )
                            }
                        },
                        enabled = replyText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            null,
                            tint = if (replyText.isNotBlank()) MeTontRed else MeTontGrey
                        )
                    }
                }
            }

            // ── REPLIES LIST ──────────────────────────────────────
            if (showReplies && replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(8.dp))

                replies.forEach { reply ->
                    val isReplyLiked = currentUserId != null && currentUserId in reply.likedBy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = MeTontGrey.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    reply.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                    color = MeTontGrey,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    reply.userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                                Text(
                                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(reply.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeTontGrey
                                )
                            }
                            Text(
                                reply.comment,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.8f)
                            )
                            // Reply like button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        AuthGate.requireLogin(
                                            onNotLoggedIn = onNavigateToAuth,
                                            action = {
                                                currentUserId?.let { uid ->
                                                    reviewViewModel.toggleReplyLike(
                                                        businessId, review.id, reply.id, uid
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isReplyLiked) Icons.Default.ThumbUp
                                        else Icons.Default.ThumbUpOffAlt,
                                        contentDescription = "Like reply",
                                        tint = if (isReplyLiked) MeTontRed else MeTontGrey,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    "${reply.likedBy.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MeTontGrey
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBadgeChip(label: String, color: Color, icon: ImageVector) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}