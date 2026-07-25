// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.albbiz.map.data.Story
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
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
    val context = LocalContext.current

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
                            // This is free text typed by whoever posted the story
                            // (see the "e.g. Tirana, Albania" field in AddStoryScreen) —
                            // not a geocoded point, so there's no lat/lng to navigate to
                            // precisely. A maps search on that text is the right fallback
                            // here; it's the same thing typing it into Maps' search bar
                            // would do. ACTION_VIEW with a plain https://maps.google.com
                            // search URL (rather than the google.navigation: scheme used
                            // elsewhere in the app) works with whatever maps app — or
                            // browser — is available, instead of requiring Google Maps
                            // specifically.
                            Text(
                                "📍 ${currentStory.location}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier.clickable {
                                    try {
                                        val query = Uri.encode(currentStory.location)
                                        val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Couldn't open maps", Toast.LENGTH_SHORT).show()
                                    }
                                }
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

                // Close button
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }

        // ── BOTTOM SECTION ────────────────────────────────────────────
        // The app renders edge-to-edge (see enableEdgeToEdge() in MainActivity), so
        // without accounting for it, content here draws underneath the system
        // navigation bar — fine on gesture-nav phones where that bar is a thin
        // strip, but on 3-button nav it's tall enough to sit right on top of this
        // button. navigationBarsPadding() adds exactly however much space that bar
        // actually needs on this device, instead of guessing a fixed value that's
        // either not enough on some phones or wastes space on others.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Caption — a semi-transparent pill behind the text instead of relying
            // only on the gradient scrim above, so it stays legible over any photo
            // regardless of how bright or busy that particular photo is. Bumped up
            // from 14sp Medium to 20sp Bold so it reads as the story's headline
            // rather than a small afterthought caption.
            // Only shown when there's a photo — a photo-less story already shows
            // this same text as the big centered headline earlier (see "No photo —
            // show colored background with text" above). Showing it again here too
            // was the duplicate text visible in the screenshot: same caption once
            // as the giant centered version, once again as this pill.
            if (currentStory.text.isNotBlank() && photos.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.35f)
                ) {
                    Text(
                        currentStory.text,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // View Business button (shown whenever the story is linked to a business)
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