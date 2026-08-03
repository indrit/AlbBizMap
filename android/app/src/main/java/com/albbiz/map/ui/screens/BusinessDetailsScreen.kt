// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.launch
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
    // Takes the blocked action so it can be resumed automatically right after login,
    // instead of dropping the user back with nothing having happened.
    onNavigateToAuth: (() -> Unit) -> Unit,
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
                        val shareAction: () -> Unit = {
                            val shareText = "Check out ${business.name} on MeTont!\n${business.category} • ${business.address}"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        }
                        AuthGate.requireLogin(
                            onNotLoggedIn = { onNavigateToAuth(shareAction) },
                            action = shareAction
                        )
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val favoriteAction: () -> Unit = { mapViewModel.toggleFavorite(business.id) }
                        AuthGate.requireLogin(
                            onNotLoggedIn = { onNavigateToAuth(favoriteAction) },
                            action = favoriteAction
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
            // ── MAIN INFO CARD (name + badges, above the photos) ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Name — previously only shown in the top app bar title;
                        // now also shown here so it (and the badges) read above
                        // the photo gallery instead of the name being separated
                        // from its badges by the whole photo section.
                        Text(
                            business.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

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
                                // Only the highest tier badge, not one chip per true flag —
                                // matches the same highest-tier-wins pattern already used for
                                // the list view's badge, the map pin color, and the profile
                                // tier icon. Previously each flag rendered its own chip, so a
                                // business with all three tier flags set (the only way to reach
                                // Sponsored's 14-photo cap, since a business's plan is
                                // cumulative — see maxPhotos) showed Premium + Featured +
                                // Sponsored stacked together, which read as confusing/redundant
                                // rather than "this business is Sponsored."
                                when {
                                    business.isSponsored -> DetailBadgeChip(strings.sponsored, TierGold, Icons.Default.Campaign)
                                    business.isFeatured -> DetailBadgeChip(strings.featured2, TierSilver, Icons.Default.LocalFireDepartment)
                                    business.isPremium -> DetailBadgeChip(strings.premium, TierBronze, Icons.Default.Star)
                                }
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

                        // Same highest-tier-wins fix as the badges above — this was
                        // isPremium-only, so a Featured/Sponsored business without isPremium
                        // itself set to true never showed its extended description, even
                        // though Featured/Sponsored are supposed to include everything
                        // Premium has.
                        if ((business.isPremium || business.isFeatured || business.isSponsored) &&
                            business.longDescription.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                business.longDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Address — tappable for turn-by-turn directions, same
                        // google.navigation: pattern already used on the map pin
                        // preview and List View cards. This was the one place a
                        // business's address showed up without any way to act on
                        // it — the main profile page itself.
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.clickable {
                                val location = business.location
                                if (location == null) {
                                    Toast.makeText(context, strings.noLocationSetToast, Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                try {
                                    val uri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}&mode=d")
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, uri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, strings.googleMapsNotInstalled, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
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
                                color = MeTontGrey,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                    }
                }
            }

            // ── PHOTOS (main pager + thumbnail strip) ─────────────
            // Big photo on top with the next one peeking in on the right edge
            // (via the pager's contentPadding/pageSpacing), plus a row of
            // smaller thumbnails underneath that jump the pager on tap.
            // Shown for every business regardless of tier — previously this
            // whole section lived inside the Contact card and was hidden
            // behind isPremium, so even a free business's one uploaded photo
            // never actually displayed anywhere. Placed below the name/badges
            // card so the name and badges read above the photos.
            if (business.photos.isNotEmpty()) {
                item {
                    val pagerState = rememberPagerState(pageCount = { business.photos.size })
                    val photoScope = rememberCoroutineScope()
                    var fullScreenOpen by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(end = 48.dp),
                                pageSpacing = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                AsyncImage(
                                    model = business.photos[page],
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { fullScreenOpen = true },
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Google-Maps-style affordances layered on the hero photo
                            // itself (photo count + an explicit expand hint) instead of
                            // repeating the same images again in a second grid below —
                            // that's what made the old "More Photos" section look like a
                            // duplicate row of the same pictures.
                            if (business.photos.size > 1) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 12.dp, bottom = 12.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${pagerState.currentPage + 1}/${business.photos.size}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            IconButton(
                                onClick = { fullScreenOpen = true },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 8.dp, top = 8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    Icons.Default.Fullscreen,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (business.photos.size > 1) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(business.photos) { index, url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .then(
                                                if (pagerState.currentPage == index) {
                                                    Modifier.border(2.dp, MeTontRed, RoundedCornerShape(10.dp))
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .clickable {
                                                photoScope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    if (fullScreenOpen) {
                        FullScreenPhotoViewer(
                            photos = business.photos,
                            startPage = pagerState.currentPage,
                            onDismiss = { fullScreenOpen = false }
                        )
                    }
                }
            }

            // ── CONTACT CARD (Premium+) ───────────────────────────
            // Was gated behind isPremium alone, which hid it for a business
            // that's Featured or Sponsored but never had isPremium itself set
            // to true — even though Featured/Sponsored are supposed to include
            // everything Premium has. Same highest-tier-wins flags used
            // elsewhere (Business.maxPhotos, UserProfileScreen's badge).
            item {
                if (business.isPremium || business.isFeatured || business.isSponsored) {
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

                            // Working Hours
                            if (business.workingHours.isNotEmpty()) {
                                Text(
                                    strings.workingHours,
                                    fontWeight = FontWeight.Bold,
                                    color = MeTontRed,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                // business.workingHours is a flat map of "Mon_open"/"Mon_close"/
                                // "Mon_closed" style keys (see WorkingHoursEditor) — iterating
                                // it directly used to print each raw key/value as its own row
                                // ("Mon_close  20:00"). Group by day instead and render one
                                // friendly "Open - Close" (or "Closed") row per day, same
                                // defaults and formatting the editor itself uses.
                                val weekdayOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                weekdayOrder.forEach { day ->
                                    val hasAnyEntry = business.workingHours.containsKey("${day}_open") ||
                                        business.workingHours.containsKey("${day}_close") ||
                                        business.workingHours.containsKey("${day}_closed")
                                    if (hasAnyEntry) {
                                        val isClosed = business.workingHours["${day}_closed"] == "true"
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(day, style = MaterialTheme.typography.bodySmall, color = MeTontGrey)
                                            if (isClosed) {
                                                Text(
                                                    "Closed",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MeTontRed
                                                )
                                            } else {
                                                val openDisplay = formatTimeDisplay(business.workingHours["${day}_open"] ?: "09:00")
                                                val closeDisplay = formatTimeDisplay(business.workingHours["${day}_close"] ?: "18:00")
                                                Text(
                                                    "$openDisplay - $closeDisplay",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── PROMOTIONS CARD ───────────────────────────────────
            // Moved above the Upgrade card so a business's active deals sit
            // with the rest of its profile data (name, contact, hours) —
            // previously this rendered below the Upgrade card, which buried
            // it under an unrelated sales pitch.
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

            // ── UPGRADE CARD ───────────────────────────────────────
            // Its own item now, shown for any business not already on the
            // top plan — regardless of whether the Contact card above is
            // also showing. Previously this only rendered in the else-branch
            // of the Contact card's isPremium check, so a business already on
            // Premium (or Featured) had no way to see this at all, meaning no
            // way to upgrade further to Featured/Sponsored from this screen.
            if (!business.isSponsored) {
                item {
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
                    // This is the public like counter (business.likedBy /
                    // likeCount) — separate from the favorite/bookmark heart icon
                    // in the top bar above, which is a personal saved-list and
                    // uses toggleFavorite instead. This button used to call
                    // toggleFavorite too, which meant the icon and count you see
                    // here never actually changed when tapped, since nothing was
                    // writing to likedBy/likeCount.
                    OutlinedButton(
                        onClick = {
                            val likeAction: () -> Unit = { mapViewModel.toggleBusinessLike(business.id) }
                            com.albbiz.map.utils.AuthGate.requireLogin(
                                onNotLoggedIn = { onNavigateToAuth(likeAction) },
                                action = likeAction
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
    val strings = LocalAppStrings.current
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
                        "${strings.promotionCodePrefix}${promotion.discountCode}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            if (promotion.expiryDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val formatted = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(Date(promotion.expiryDate))
                Text(
                    "${strings.promotionExpiresPrefix}$formatted",
                    style = MaterialTheme.typography.labelSmall,
                    color = MeTontGrey
                )
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
    onNavigateToAuth: (() -> Unit) -> Unit,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val currentUserId = firebaseUser?.uid
    val currentUserName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.email?.substringBefore("@") ?: ""
    val isLiked = currentUserId != null && currentUserId in review.likedBy
    val isOwnReview = currentUserId != null && currentUserId == review.userId
    val repliesMap by reviewViewModel.replies.collectAsState()
    val replies = repliesMap[review.id] ?: emptyList()

    var showReplies by remember { mutableStateOf(false) }
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var fullScreenPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editRating by remember { mutableIntStateOf(review.rating) }
    var editComment by remember { mutableStateOf(review.comment) }
    var isSubmittingEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var replyPendingEdit by remember { mutableStateOf<Reply?>(null) }
    var editReplyComment by remember { mutableStateOf("") }
    var isSubmittingReplyEdit by remember { mutableStateOf(false) }
    var replyPendingDelete by remember { mutableStateOf<Reply?>(null) }
    var isDeletingReply by remember { mutableStateOf(false) }

    // Keyed on showReplies alone (not replies.size): loadReplies() attaches a live
    // Firestore listener that updates `replies` itself, so keying on its own output
    // size caused this effect to retrigger on every emission — restarting the
    // listener in a loop and piling up concurrent collectors on the same query.
    LaunchedEffect(showReplies) {
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

            // ── REVIEW PHOTOS ──────────────────────────────────────
            // Small thumbnails only — same "tap for the real thing" pattern as
            // the business's own photo section (hero + full-screen viewer)
            // rather than showing them at full size inline in the feed.
            if (review.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(review.photos) { index, url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { fullScreenPhotoIndex = index },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── ACTION ROW (like, reply) ───────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Like button
                IconButton(
                    onClick = {
                        val likeAction: () -> Unit = {
                            currentUserId?.let { uid ->
                                reviewViewModel.toggleLike(businessId, review.id, uid)
                            }
                        }
                        AuthGate.requireLogin(
                            onNotLoggedIn = { onNavigateToAuth(likeAction) },
                            action = likeAction
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
                        val replyAction: () -> Unit = { showReplyInput = !showReplyInput }
                        AuthGate.requireLogin(
                            onNotLoggedIn = { onNavigateToAuth(replyAction) },
                            action = replyAction
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

                // Edit/delete — only the review's own author sees these
                if (isOwnReview) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            editRating = review.rating
                            editComment = review.comment
                            showEditDialog = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = MeTontGrey, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = MeTontGrey, modifier = Modifier.size(16.dp))
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
                            Text(strings.writeReplyPlaceholder, color = MeTontGrey, fontSize = 12.sp)
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
                                        // No need to call loadReplies() again here — it's a live
                                        // Firestore listener (started by the LaunchedEffect above
                                        // once showReplies flips true) that will pick up the new
                                        // reply on its own.
                                        showReplies = true
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
                                        val replyLikeAction: () -> Unit = {
                                            currentUserId?.let { uid ->
                                                reviewViewModel.toggleReplyLike(
                                                    businessId, review.id, reply.id, uid
                                                )
                                            }
                                        }
                                        AuthGate.requireLogin(
                                            onNotLoggedIn = { onNavigateToAuth(replyLikeAction) },
                                            action = replyLikeAction
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

                                // Edit/delete — only the reply's own author sees these
                                if (currentUserId != null && currentUserId == reply.userId) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            replyPendingEdit = reply
                                            editReplyComment = reply.comment
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = MeTontGrey, modifier = Modifier.size(13.dp))
                                    }
                                    IconButton(
                                        onClick = { replyPendingDelete = reply },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = MeTontGrey, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fullScreenPhotoIndex?.let { index ->
        FullScreenPhotoViewer(
            photos = review.photos,
            startPage = index,
            onDismiss = { fullScreenPhotoIndex = null }
        )
    }

    // ── EDIT REVIEW DIALOG ─────────────────────────────────────────
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmittingEdit) showEditDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(strings.editReviewTitle, fontWeight = FontWeight.Bold, color = MeTontRed)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = { editRating = i },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (i <= editRating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (i <= editRating) Color(0xFFFFC107) else MeTontGrey,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = editComment,
                        onValueChange = { editComment = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editRating == 0 || editComment.isBlank() || isSubmittingEdit) return@Button
                        isSubmittingEdit = true
                        reviewViewModel.updateReview(
                            businessId = businessId,
                            reviewId = review.id,
                            rating = editRating,
                            comment = editComment.trim(),
                            onSuccess = {
                                isSubmittingEdit = false
                                showEditDialog = false
                                Toast.makeText(context, strings.reviewUpdated, Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                isSubmittingEdit = false
                                Toast.makeText(context, strings.reviewUpdateFailed, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isSubmittingEdit && editRating > 0 && editComment.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MeTontRed)
                ) { Text(strings.save, color = Color.White) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = !isSubmittingEdit
                ) {
                    Text(strings.cancel, color = MeTontGrey)
                }
            }
        )
    }

    // ── DELETE REVIEW CONFIRMATION ──────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirm = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text(strings.deleteReviewConfirmTitle, fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text(strings.deleteReviewConfirmMessage, color = MeTontGrey) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isDeleting) return@TextButton
                        isDeleting = true
                        reviewViewModel.deleteReview(
                            businessId = businessId,
                            reviewId = review.id,
                            onSuccess = {
                                isDeleting = false
                                showDeleteConfirm = false
                                Toast.makeText(context, strings.reviewDeleted, Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                isDeleting = false
                                Toast.makeText(context, strings.reviewDeleteFailed, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isDeleting
                ) {
                    Text(strings.deleteReviewButton, color = MeTontRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    enabled = !isDeleting
                ) {
                    Text(strings.cancel, color = MeTontGrey)
                }
            }
        )
    }

    // ── EDIT REPLY DIALOG ────────────────────────────────────────────
    replyPendingEdit?.let { editingReply ->
        AlertDialog(
            onDismissRequest = { if (!isSubmittingReplyEdit) replyPendingEdit = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(strings.editReplyTitle, fontWeight = FontWeight.Bold, color = MeTontRed)
            },
            text = {
                OutlinedTextField(
                    value = editReplyComment,
                    onValueChange = { editReplyComment = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MeTontRed,
                        cursorColor = MeTontRed
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editReplyComment.isBlank() || isSubmittingReplyEdit) return@Button
                        isSubmittingReplyEdit = true
                        reviewViewModel.updateReply(
                            businessId = businessId,
                            reviewId = review.id,
                            replyId = editingReply.id,
                            comment = editReplyComment.trim(),
                            onSuccess = {
                                isSubmittingReplyEdit = false
                                replyPendingEdit = null
                                Toast.makeText(context, strings.replyUpdated, Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                isSubmittingReplyEdit = false
                                Toast.makeText(context, strings.replyUpdateFailed, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isSubmittingReplyEdit && editReplyComment.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MeTontRed)
                ) { Text(strings.save, color = Color.White) }
            },
            dismissButton = {
                TextButton(
                    onClick = { replyPendingEdit = null },
                    enabled = !isSubmittingReplyEdit
                ) {
                    Text(strings.cancel, color = MeTontGrey)
                }
            }
        )
    }

    // ── DELETE REPLY CONFIRMATION ─────────────────────────────────────
    replyPendingDelete?.let { deletingReply ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingReply) replyPendingDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text(strings.deleteReplyConfirmTitle, fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text(strings.deleteReplyConfirmMessage, color = MeTontGrey) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isDeletingReply) return@TextButton
                        isDeletingReply = true
                        reviewViewModel.deleteReply(
                            businessId = businessId,
                            reviewId = review.id,
                            replyId = deletingReply.id,
                            onSuccess = {
                                isDeletingReply = false
                                replyPendingDelete = null
                                Toast.makeText(context, strings.replyDeleted, Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                isDeletingReply = false
                                Toast.makeText(context, strings.replyDeleteFailed, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isDeletingReply
                ) {
                    Text(strings.deleteReplyButton, color = MeTontRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { replyPendingDelete = null },
                    enabled = !isDeletingReply
                ) {
                    Text(strings.cancel, color = MeTontGrey)
                }
            }
        )
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

// Full-bleed swipeable gallery opened by tapping the hero photo or the
// expand icon on the business detail screen — mirrors the "tap a place's
// photo, get a full-screen swipeable viewer" pattern from Google Maps,
// rather than repeating the same images a second time in a grid.
@Composable
private fun FullScreenPhotoViewer(
    photos: List<String>,
    startPage: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val pagerState = rememberPagerState(
            initialPage = startPage,
            pageCount = { photos.size }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = photos[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }

            if (photos.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${pagerState.currentPage + 1}/${photos.size}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}