// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val error by reviewViewModel.error.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Write a Review",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Rate this business",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                IconButton(onClick = { rating = i }) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Star $i",
                        tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray
                    )
                }
            }
        }

        Text(
            text = if (rating == 0) "Tap a star to select a rating" else "Rating: $rating / 5",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp),
            label = { Text("Write your review") },
            placeholder = { Text("Share your experience...") },
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (rating == 0) {
                    Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (comment.isBlank()) {
                    Toast.makeText(context, "Please write a review", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                reviewViewModel.addReview(
                    businessId = businessId,
                    rating = rating,
                    comment = comment.trim(),
                    userId = userId,
                    userName = userName,
                    onSuccess = {
                        Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
                        onReviewSubmitted()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && rating > 0 && comment.isNotBlank()
        ) {
            Text(if (isLoading) "Submitting..." else "Submit Review")
        }

        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}