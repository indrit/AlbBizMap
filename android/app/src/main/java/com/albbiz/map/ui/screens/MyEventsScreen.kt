// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.albbiz.map.data.Event
import com.albbiz.map.data.EventsRepository
import com.albbiz.map.ui.LocalAppStrings
import com.albbiz.map.ui.MeTontGrey
import com.albbiz.map.ui.MeTontRed
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// The events a logged-in user submitted — reachable from the "My Events" card on
// the Profile screen. Mirrors MyBusinessesScreen.kt's structure, but (unlike
// businesses, which have no in-app delete yet) exposes a delete button per row,
// since that's the explicit ask this was built for.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(
    onBackClick: () -> Unit,
    onAddEventClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val user = FirebaseAuth.getInstance().currentUser
    val eventsRepository = remember { EventsRepository() }
    var ownedEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var eventPendingDelete by remember { mutableStateOf<Event?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(user?.uid) {
        val uid = user?.uid
        if (uid == null) {
            isLoading = false
        } else {
            eventsRepository.getEventsByOrganizer(uid).collect { list ->
                ownedEvents = list.sortedBy { it.date }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.myEvents,
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MeTontRed)
                    }
                }
                ownedEvents.isEmpty() -> {
                    // ── EMPTY STATE ───────────────────────────────
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(40.dp),
                                color = MeTontRed.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Event,
                                        null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MeTontRed.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Text(
                                strings.noEventsYet,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MeTontGrey,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                strings.noEventsYetSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MeTontGrey.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddEventClick,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MeTontRed,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.submitEventButton, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {
                    // ── OWNED EVENTS LIST ──────────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                "${ownedEvents.size} ${if (ownedEvents.size == 1) "event" else "events"}",
                                color = MeTontGrey,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(ownedEvents, key = { it.id }) { event ->
                            MyEventRow(
                                event = event,
                                onDeleteClick = { eventPendingDelete = event }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onAddEventClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MeTontRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MeTontRed)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.submitEventButton)
                            }
                        }
                    }
                }
            }
        }

        // ── DELETE CONFIRMATION ────────────────────────────────────
        val eventToDelete = eventPendingDelete
        if (eventToDelete != null) {
            AlertDialog(
                onDismissRequest = { if (!isDeleting) eventPendingDelete = null },
                shape = RoundedCornerShape(20.dp),
                title = { Text(strings.deleteEventConfirmTitle, fontWeight = FontWeight.Bold, color = Color.Black) },
                text = { Text(strings.deleteEventConfirmMessage, color = MeTontGrey) },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            isDeleting = true
                            scope.launch {
                                eventsRepository.deleteEvent(eventToDelete)
                                    .onSuccess {
                                        Toast.makeText(context, strings.eventDeleted, Toast.LENGTH_SHORT).show()
                                    }
                                    .onFailure {
                                        Toast.makeText(context, strings.eventDeleteFailed, Toast.LENGTH_SHORT).show()
                                    }
                                isDeleting = false
                                eventPendingDelete = null
                            }
                        }
                    ) {
                        Text(strings.deleteEvent, color = MeTontRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = { eventPendingDelete = null }
                    ) {
                        Text(strings.cancel, color = MeTontGrey)
                    }
                }
            )
        }
    }
}

@Composable
private fun MyEventRow(
    event: Event,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!event.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .background(MeTontRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MeTontRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Event, null, tint = MeTontRed.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    SimpleDateFormat("EEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                        .format(Date(event.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MeTontGrey
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = MeTontGrey
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        event.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MeTontGrey,
                        maxLines = 1
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Delete", tint = MeTontRed)
            }
        }
    }
}
