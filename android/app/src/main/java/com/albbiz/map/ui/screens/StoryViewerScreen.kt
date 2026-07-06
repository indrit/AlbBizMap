// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.data.Story
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.albbiz.map.ui.theme.TierGold
import com.albbiz.map.viewmodel.StoriesViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StoryViewerScreen(
    stories: List<Story>,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    onBusinessClick: (String) -> Unit = {},
    storiesViewModel: StoriesViewModel = viewModel()
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()

    // Current story index
    var currentStoryIndex by remember { mutableStateOf(initialIndex) }
    val currentStory = stories.getOrNull(currentStoryIndex)

    // Current photo index within story
    var currentPhotoIndex by remember { mutableStateOf(0) }
    val photos = currentStory?.photos ?: emptyList()

    // Progress for current photo (0f to 1f)
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 5000),
        label = "story_progress"
    )

    // Auto advance every 5 seconds
    LaunchedEffect(currentStoryIndex, currentPhotoIndex) {
        progress = 0f
        delay(100)
        progress = 1f
        delay(5000)
        // Move to next photo or next story
        if (currentPhotoIndex < photos.size - 1) {
            currentPhotoIndex++
        } else if (currentStoryIndex < stories.size - 1) {
            currentStoryIndex++
            currentPhotoIndex = 0
        } else {
            onClose()
        }
    }

    // Mark story as viewed
    LaunchedEffect(currentStory?.id) {
        currentStory?.id?.let { storyId ->
            storiesViewModel.markStoryViewed(storyId)
        }
    }

    if (currentStory == null) {
        onClose()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── STORY PHOTO ───────────────────────────────────────────────
        val currentPhoto = photos.getOrNull(currentPhotoIndex)
        if (currentPhoto != null) {
            AsyncImage(
                model = currentPhoto,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // No photo — show colored background with text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (currentStory.type) {
                            "sponsored" -> TierGold
                            "community" -> Color(0xFF2196F3)
                            "business" -> MeTontRed
                            else -> Color(0xFF1A1A1A)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    currentStory.text,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        // ── DARK GRADIENT OVERLAY ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // ── TAP LEFT/RIGHT TO NAVIGATE ────────────────────────────────
        Row(modifier = Modifier.fillMaxSize()) {
            // Tap left → previous
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (currentPhotoIndex > 0) {
                                currentPhotoIndex--
                            } else if (currentStoryIndex > 0) {
                                currentStoryIndex--
                                currentPhotoIndex = 0
                            }
                        }
                    }
            )
            // Tap right → next
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (currentPhotoIndex < photos.size - 1) {
                                currentPhotoIndex++
                            } else if (currentStoryIndex < stories.size - 1) {
                                currentStoryIndex++
                                currentPhotoIndex = 0
                            } else {
                                onClose()
                            }
                        }
                    }
            )
        }

        // ── TOP SECTION ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 12.dp, end = 12.dp)
        ) {
            // Progress bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                photos.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(
                                    when {
                                        index < currentPhotoIndex -> 1f
                                        index == currentPhotoIndex -> animatedProgress
                                        else -> 0f
                                    }
                                )
                                .background(Color.White)
                        )
                    }
                }
                // If no photos, show single progress bar
                if (photos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User/business info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = when (currentStory.type) {
                        "sponsored" -> TierGold.copy(alpha = 0.3f)
                        "community" -> Color(0xFF2196F3).copy(alpha = 0.3f)
                        "business" -> MeTontRed.copy(alpha = 0.3f)
                        else -> Color.White.copy(alpha = 0.2f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (currentStory.businessName ?: currentStory.userName)
                                .firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        currentStory.businessName ?: currentStory.userName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStory.location.isNotBlank()) {
                            Text(
                                "📍 ${currentStory.location}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            SimpleDateFormat("MMM dd", Locale.getDefault())
                                .format(Date(currentStory.createdAt)),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Sponsored badge
                if (currentStory.isSponsored) {
                    Surface(
                        color = TierGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "Sponsored",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = TierGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Close button
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }

        // ── BOTTOM SECTION ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Caption
            if (currentStory.text.isNotBlank()) {
                Text(
                    currentStory.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // View Business button (for business/sponsored stories)
            if (currentStory.businessId != null) {
                Button(
                    onClick = {
                        currentStory.businessId.let { onBusinessClick(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MeTontRed
                    )
                ) {
                    Icon(Icons.Default.Business, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Business", fontWeight = FontWeight.Bold)
                }
            }

            // Story counter (e.g. "2 of 5")
            if (stories.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${currentStoryIndex + 1} of ${stories.size}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}