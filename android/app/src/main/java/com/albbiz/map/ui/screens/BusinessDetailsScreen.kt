// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.data.Business
import com.albbiz.map.data.Review
import com.albbiz.map.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.data.ClaimRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    business: Business,
    currentUserId: String,
    onWriteReviewClick: () -> Unit,
    onEditClick: () -> Unit,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val reviews by reviewViewModel.reviews.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    val error by reviewViewModel.error.collectAsState()
    val reportMessage by reviewViewModel.reportMessage.collectAsState()

    LaunchedEffect(business.id) {
        reviewViewModel.loadReviews(business.id)
    }

    // Show report feedback as a snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(reportMessage) {
        reportMessage?.let {
            snackbarHostState.showSnackbar(it)
            reviewViewModel.clearReportMessage()
        }
    }

    val avgRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else 0.0

    val reviewCount = reviews.size

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = business.name,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = business.category,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = business.location?.let {
                                String.format("Location: %.5f, %.5f", it.latitude, it.longitude)
                            } ?: "Location not available"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (reviewCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", avgRating),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "($reviewCount reviews)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "No reviews yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = business.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Show Claim button only if business has no owner
                        if (business.ownerId.isEmpty()) {
                            var showClaimDialog by remember { mutableStateOf(false) }
                            var claimReason by remember { mutableStateOf("") }
                            var claimEmail by remember { mutableStateOf("") }
                            var isSubmittingClaim by remember { mutableStateOf(false) }
                            val claimRepository = remember { BusinessRepository() }
                            val claimScope = rememberCoroutineScope()

                            OutlinedButton(
                                onClick = { showClaimDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Flag, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Claim this Business")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (showClaimDialog) {
                                AlertDialog(
                                    onDismissRequest = { showClaimDialog = false },
                                    title = { Text("Claim this Business") },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                "Fill in your details to claim ownership of ${business.name}.",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            OutlinedTextField(
                                                value = claimEmail,
                                                onValueChange = { claimEmail = it },
                                                label = { Text("Your Email *") },
                                                placeholder = { Text("email@example.com") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = claimReason,
                                                onValueChange = { claimReason = it },
                                                label = { Text("Why are you the owner? *") },
                                                placeholder = { Text("e.g. I registered this business in 2020...") },
                                                minLines = 3,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                if (claimEmail.isBlank() || claimReason.isBlank()) return@Button
                                                isSubmittingClaim = true
                                                claimScope.launch {
                                                    val claim = ClaimRequest(
                                                        businessId = business.id,
                                                        businessName = business.name,
                                                        userId = currentUserId,
                                                        userName = "User", // TODO: replace with real username
                                                        userEmail = claimEmail.trim(),
                                                        reason = claimReason.trim()
                                                    )
                                                    claimRepository.submitClaim(claim)
                                                        .onSuccess {
                                                            isSubmittingClaim = false
                                                            showClaimDialog = false
                                                        }
                                                        .onFailure {
                                                            isSubmittingClaim = false
                                                        }
                                                }
                                            },
                                            enabled = !isSubmittingClaim &&
                                                    claimEmail.isNotBlank() &&
                                                    claimReason.isNotBlank()
                                        ) {
                                            Text(if (isSubmittingClaim) "Submitting..." else "Submit Claim")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showClaimDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }


                        // Show Edit button only if current user is the owner
                        if (currentUserId == business.ownerId) {
                            OutlinedButton(
                                onClick = onEditClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Business")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = onWriteReviewClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Write a Review")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (error != null) {
                item {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (reviews.isEmpty()) {
                item {
                    Text(
                        text = "Be the first to review this business.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(reviews) { review ->
                    ReviewItem(
                        review = review,
                        currentUserId = currentUserId,
                        onReportClick = {
                            reviewViewModel.reportReview(
                                businessId = business.id,
                                reviewId = review.id,
                                userId = currentUserId
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(
    review: Review,
    currentUserId: String,
    onReportClick: () -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    val alreadyReported = currentUserId in review.reportedBy

    // Confirm dialog before reporting
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Review") },
            text = { Text("Are you sure you want to report this review as inappropriate?") },
            confirmButton = {
                TextButton(onClick = {
                    onReportClick()
                    showReportDialog = false
                }) {
                    Text("Report", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.userName,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = review.rating.toString())
                }
            }

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Don't show report button on your own reviews
                if (review.userId != currentUserId) {
                    IconButton(
                        onClick = { showReportDialog = true },
                        enabled = !alreadyReported
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = "Report review",
                            tint = if (alreadyReported) Color.Gray else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}