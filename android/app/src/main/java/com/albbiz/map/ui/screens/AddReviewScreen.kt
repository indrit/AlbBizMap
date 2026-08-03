// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.viewmodel.ReviewViewModel

private const val MAX_REVIEW_PHOTOS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    businessId: String,
    onReviewSubmitted: () -> Unit,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val error by reviewViewModel.error.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val remaining = MAX_REVIEW_PHOTOS - selectedPhotos.size
        if (uris.size > remaining) {
            Toast.makeText(context, strings.maxPhotosPerReview, Toast.LENGTH_SHORT).show()
            selectedPhotos = selectedPhotos + uris.take(remaining)
        } else {
            selectedPhotos = selectedPhotos + uris
        }
    }

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

            // ── PHOTOS CARD (Optional) ─────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.photos,
                            fontWeight = FontWeight.Bold,
                            color = MeTontRed,
                            fontSize = 14.sp
                        )
                        Text(
                            "${selectedPhotos.size}/$MAX_REVIEW_PHOTOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MeTontGrey
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedPhotos.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedPhotos) { uri ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedPhotos = selectedPhotos - uri },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(22.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (selectedPhotos.size < MAX_REVIEW_PHOTOS) {
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                        ) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.addPhotosButton)
                        }
                    }
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
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    // Defense-in-depth: this screen is only ever supposed to be reached
                    // through MainActivity's gated onWriteReviewClick, which already
                    // checks the user is logged in before opening it. But this check
                    // costs nothing and means a review can never silently submit with an
                    // empty userId if some future code path reaches this screen another way.
                    if (firebaseUser == null) {
                        Toast.makeText(context, strings.loginRequiredForReview, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val userId = firebaseUser.uid
                    val userName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                        ?: firebaseUser.email?.substringBefore("@") ?: "User"
                    reviewViewModel.addReview(
                        businessId = businessId,
                        rating = rating,
                        comment = comment.trim(),
                        userId = userId,
                        userName = userName,
                        photoUris = selectedPhotos,
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