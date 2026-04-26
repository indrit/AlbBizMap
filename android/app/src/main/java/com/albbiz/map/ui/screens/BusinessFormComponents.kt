// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun WorkingHoursEditor(
    hours: Map<String, String>,
    onHoursChanged: (Map<String, String>) -> Unit
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEach { day ->
            var openTime by remember { mutableStateOf(hours["${day}_open"] ?: "09:00") }
            var closeTime by remember { mutableStateOf(hours["${day}_close"] ?: "18:00") }
            var isClosed by remember { mutableStateOf(hours["${day}_closed"] == "true") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(day, modifier = Modifier.width(40.dp))

                if (isClosed) {
                    Text(
                        "Closed",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = {
                                openTime = it
                                onHoursChanged(hours + ("${day}_open" to it))
                            },
                            label = { Text("Open") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = {
                                closeTime = it
                                onHoursChanged(hours + ("${day}_close" to it))
                            },
                            label = { Text("Close") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                TextButton(onClick = {
                    isClosed = !isClosed
                    onHoursChanged(hours + ("${day}_closed" to isClosed.toString()))
                }) {
                    Text(if (isClosed) "Open" else "Close")
                }
            }
        }
    }
}

@Composable
fun LocationPickerDialog(
    initialLocation: LatLng,
    onLocationSelected: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf(initialLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter coordinates or pick from map")
                Text(
                    "Lat: ${selectedLocation.latitude}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Lng: ${selectedLocation.longitude}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = { onLocationSelected(selectedLocation) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}