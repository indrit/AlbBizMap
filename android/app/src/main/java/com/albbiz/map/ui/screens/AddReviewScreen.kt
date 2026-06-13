// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    businessId: String,
    userId: String,
    userName: String,
    onReviewSubmitted: () -> Unit,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val error by reviewViewModel.error.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.writeReview,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onReviewSubmitted) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MeTontRed
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                //.background(Color(0xFFF5F5F5))
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── RATING CARD ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        strings.rateThisBusiness,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Star rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = { rating = i },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Filled.Star
                                    else Icons.Outlined.Star,
                                    contentDescription = "Star $i",
                                    tint = if (i <= rating) Color(0xFFFFC107) else MeTontGrey,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    // Rating label
                    Surface(
                        color = if (rating > 0) MeTontRed.copy(alpha = 0.1f)
                        else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (rating == 0) strings.tapStarToRate
                            else when (rating) {
                                1 -> "⭐ Poor"
                                2 -> "⭐⭐ Fair"
                                3 -> "⭐⭐⭐ Good"
                                4 -> "⭐⭐⭐⭐ Very Good"
                                else -> "⭐⭐⭐⭐⭐ Excellent!"
                            },
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            color = if (rating > 0) MeTontRed else MeTontGrey,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── REVIEW CARD ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        strings.writeReviewLabel,
                        fontWeight = FontWeight.Bold,
                        color = MeTontRed,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        placeholder = { Text(strings.shareExperience, color = MeTontGrey) },
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeTontRed,
                            cursorColor = MeTontRed
                        )
                    )

                    // Character count
                    Text(
                        "${comment.length} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MeTontGrey,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // ── SUBMIT BUTTON ─────────────────────────────────────
            Button(
                onClick = {
                    if (rating == 0) {
                        Toast.makeText(context, strings.pleaseSelectRating, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (comment.isBlank()) {
                        Toast.makeText(context, strings.pleaseWriteReview, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    reviewViewModel.addReview(
                        businessId = businessId,
                        rating = rating,
                        comment = comment.trim(),
                        userId = userId,
                        userName = userName,
                        onSuccess = {
                            Toast.makeText(context, strings.reviewSubmitted, Toast.LENGTH_SHORT).show()
                            onReviewSubmitted()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeTontRed,
                    contentColor = Color.White
                ),
                enabled = !isLoading && rating > 0 && comment.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.submitting)
                } else {
                    Icon(Icons.Filled.Star, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.submitReview, fontWeight = FontWeight.Bold)
                }
            }

            // Error message
            error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}