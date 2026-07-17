// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.albbiz.map.data.Event
import com.albbiz.map.data.EventsRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onBackClick: () -> Unit,
    onAddEventClick: () -> Unit
) {
    val repository = remember { EventsRepository() }
    // getEvents() must be remember()'d too, not just the repository — calling it
    // fresh inline on every recomposition hands collectAsState() a brand-new Flow
    // object each time, and it treats a new Flow instance as a reason to cancel the
    // old collection and start over. That was tearing down and reattaching a live
    // Firestore listener on every recomposition of this screen instead of once.
    val eventsFlow = remember { repository.getEvents() }
    val events by eventsFlow.collectAsState(initial = emptyList())
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.communityEventsTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEventClick,
                containerColor = MeTontRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add Event")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                //.background(Color(0xFFFFF8F0))
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (events.isEmpty()) {
                // ── EMPTY STATE ───────────────────────────────────
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MeTontRed.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MeTontRed.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            strings.noEventsFound,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MeTontGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Be the first to add a community event!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MeTontGrey.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "${events.size} upcoming ${if (events.size == 1) "event" else "events"}",
                            color = MeTontGrey,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(events) { event ->
                        EventItem(event)
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    val strings = LocalAppStrings.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── EVENT IMAGE ───────────────────────────────────────
            if (event.imageUrl != null) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder if no image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MeTontRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Event,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MeTontRed.copy(alpha = 0.4f)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // ── TITLE + PROMOTED BADGE ────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    if (event.isPromoted) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFC107),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                strings.promoted,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 3.dp
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // ── CATEGORY CHIP ─────────────────────────────────
                Surface(
                    color = MeTontRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        event.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MeTontRed,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── DATE ──────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MeTontRed
                    )
                    Spacer(Modifier.width(6.dp))
                    val date = SimpleDateFormat(
                        "EEEE, MMM dd, yyyy",
                        Locale.getDefault()
                    ).format(Date(event.date))
                    Text(
                        date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey
                    )
                }

                Spacer(Modifier.height(6.dp))

                // ── LOCATION ──────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MeTontRed
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        event.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── DESCRIPTION ───────────────────────────────────
                Text(
                    event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 3
                )

                // ── WEBSITE BUTTON ────────────────────────────────
                if (event.websiteUrl != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { /* TODO: Open URL */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MeTontRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MeTontRed
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Language,
                            null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            strings.viewEventWebsite,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}