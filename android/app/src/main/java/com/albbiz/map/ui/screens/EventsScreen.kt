// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.albbiz.map.data.Event
import com.albbiz.map.data.EventsRepository
import com.albbiz.map.ui.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onBackClick: () -> Unit,
    onAddEventClick: () -> Unit
) {
    val repository = remember { EventsRepository() }
    val events by repository.getEvents().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LocalAppStrings.current.communityEventsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEventClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text(LocalAppStrings.current.noEventsFound, style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventItem(event)
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (event.imageUrl != null) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (event.isPromoted) {
                        Surface(color = Color(0xFFFFC107), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                LocalAppStrings.current.promoted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    val date = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(event.date))
                    Text(date, style = MaterialTheme.typography.bodyMedium)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(event.locationName, style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(Modifier.height(12.dp))
                Text(event.description, style = MaterialTheme.typography.bodySmall)
                
                if (event.websiteUrl != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { /* TODO: Open URL */ }, contentPadding = PaddingValues(0.dp)) {
                        Text(LocalAppStrings.current.viewEventWebsite, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
